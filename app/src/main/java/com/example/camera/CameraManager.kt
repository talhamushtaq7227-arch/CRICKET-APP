package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.os.StatFs
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class RecordingStatus {
    object Idle : RecordingStatus()
    data class Recording(val durationMs: Long, val file: File) : RecordingStatus()
    data class Paused(val durationMs: Long, val file: File) : RecordingStatus()
    data class Completed(val file: File, val durationMs: Long) : RecordingStatus()
    data class Error(val message: String) : RecordingStatus()
}

class CameraManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var currentOutputFile: File? = null

    private var isFrontCamera = false
    private var isTorchOn = false

    private val _recordingStatus = MutableStateFlow<RecordingStatus>(RecordingStatus.Idle)
    val recordingStatus: StateFlow<RecordingStatus> = _recordingStatus.asStateFlow()

    private val _isFlashAvailable = MutableStateFlow(false)
    val isFlashAvailable: StateFlow<Boolean> = _isFlashAvailable.asStateFlow()

    private val _isFrontFacing = MutableStateFlow(false)
    val isFrontFacing: StateFlow<Boolean> = _isFrontFacing.asStateFlow()

    private var recordingStartEpochMs = 0L

    fun getRecordingElapsedMs(): Long {
        return if (recordingStartEpochMs > 0) {
            System.currentTimeMillis() - recordingStartEpochMs
        } else 0L
    }

    fun isCameraAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onError: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                startCamera(lifecycleOwner, previewView)
            } catch (e: Exception) {
                onError("Failed to initialize camera: ${e.localizedMessage ?: "Unknown error"}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return

        val cameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
            _isFlashAvailable.value = camera?.cameraInfo?.hasFlashUnit() ?: false
            _isFrontFacing.value = isFrontCamera
        } catch (exc: Exception) {
            _recordingStatus.value = RecordingStatus.Error("Failed to bind camera: ${exc.message}")
        }
    }

    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        if (activeRecording != null) return // Prevent switching during active recording
        isFrontCamera = !isFrontCamera
        startCamera(lifecycleOwner, previewView)
    }

    fun toggleTorch() {
        val cam = camera ?: return
        if (cam.cameraInfo.hasFlashUnit() && !isFrontCamera) {
            isTorchOn = !isTorchOn
            cam.cameraControl.enableTorch(isTorchOn)
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording(
        recordAudio: Boolean = true,
        onError: (String) -> Unit
    ): File? {
        val capture = videoCapture ?: run {
            onError("Camera recording is not ready")
            return null
        }

        // Check storage space (need at least 100MB)
        val availableBytes = getAvailableStorageBytes()
        if (availableBytes < 100 * 1024 * 1024) {
            onError("Insufficient storage space for video recording")
            return null
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        val videoFile = File(moviesDir, "RCB_MATCH_$timeStamp.mp4")
        currentOutputFile = videoFile

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        val pendingRecording = capture.output.prepareRecording(context, outputOptions)

        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (recordAudio && hasAudioPermission) {
            pendingRecording.withAudioEnabled()
        }

        recordingStartEpochMs = System.currentTimeMillis()

        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    _recordingStatus.value = RecordingStatus.Recording(0L, videoFile)
                }
                is VideoRecordEvent.Status -> {
                    val durationNanos = recordEvent.recordingStats.recordedDurationNanos
                    val durationMs = durationNanos / 1_000_000
                    _recordingStatus.value = RecordingStatus.Recording(durationMs, videoFile)
                }
                is VideoRecordEvent.Pause -> {
                    val durationMs = recordEvent.recordingStats.recordedDurationNanos / 1_000_000
                    _recordingStatus.value = RecordingStatus.Paused(durationMs, videoFile)
                }
                is VideoRecordEvent.Resume -> {
                    val durationMs = recordEvent.recordingStats.recordedDurationNanos / 1_000_000
                    _recordingStatus.value = RecordingStatus.Recording(durationMs, videoFile)
                }
                is VideoRecordEvent.Finalize -> {
                    val durationMs = recordEvent.recordingStats.recordedDurationNanos / 1_000_000
                    if (!recordEvent.hasError()) {
                        _recordingStatus.value = RecordingStatus.Completed(videoFile, durationMs)
                    } else {
                        _recordingStatus.value = RecordingStatus.Error("Recording error: ${recordEvent.cause?.message ?: "Unknown"}")
                    }
                    activeRecording = null
                    recordingStartEpochMs = 0L
                }
            }
        }

        return videoFile
    }

    fun pauseRecording() {
        activeRecording?.pause()
    }

    fun resumeRecording() {
        activeRecording?.resume()
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun release() {
        try {
            stopRecording()
            cameraProvider?.unbindAll()
        } catch (_: Exception) {}
    }

    private fun getAvailableStorageBytes(): Long {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        return try {
            val stat = StatFs(dir.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}
