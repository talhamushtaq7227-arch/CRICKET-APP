package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.StatFs
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
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

    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()

    private var recordingStartEpochMs = 0L
    private var orientationEventListener: OrientationEventListener? = null
    private var currentTargetRotation: Int = Surface.ROTATION_0

    init {
        setupOrientationListener()
    }

    private fun setupOrientationListener() {
        orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val rotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                if (rotation != currentTargetRotation) {
                    currentTargetRotation = rotation
                    videoCapture?.targetRotation = rotation
                }
            }
        }
    }

    fun getRecordingElapsedMs(): Long {
        return if (recordingStartEpochMs > 0) {
            System.currentTimeMillis() - recordingStartEpochMs
        } else 0L
    }

    fun isCameraAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    fun toggleAudio() {
        _isAudioEnabled.value = !_isAudioEnabled.value
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onError: (String) -> Unit
    ) {
        // Optimize preview rendering for composited overlays
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                startCamera(lifecycleOwner, previewView)
                if (orientationEventListener?.canDetectOrientation() == true) {
                    orientationEventListener?.enable()
                }
            } catch (e: Exception) {
                onError("Failed to initialize camera: ${e.localizedMessage ?: "Unknown hardware error"}")
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

        // Quality selector with ordered fallback to prevent crash on unsupported camera resolutions
        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.FHD, Quality.HD, Quality.SD),
            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        )

        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()

        videoCapture = VideoCapture.Builder(recorder)
            .setTargetRotation(currentTargetRotation)
            .build()

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
            isTorchOn = false
        } catch (exc: Exception) {
            _recordingStatus.value = RecordingStatus.Error("Failed to bind camera: ${exc.localizedMessage}")
        }
    }

    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        if (activeRecording != null) return // Lock lens toggle during recording to prevent corrupted stream
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

    fun focusAndMeter(previewView: PreviewView, x: Float, y: Float) {
        val cam = camera ?: return
        try {
            val meteringPoint = previewView.meteringPointFactory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(meteringPoint).build()
            cam.cameraControl.startFocusAndMetering(action)
        } catch (_: Exception) {}
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

        // Check storage space (minimum 50MB required for video buffer)
        val availableBytes = getAvailableStorageBytes()
        if (availableBytes < 50 * 1024 * 1024) {
            onError("Insufficient storage space for video recording (${availableBytes / (1024 * 1024)}MB available)")
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

        val shouldRecordAudio = recordAudio && _isAudioEnabled.value && hasAudioPermission
        if (shouldRecordAudio) {
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
                        // Scan file with Android MediaScanner so it is indexed immediately
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(videoFile.absolutePath),
                            arrayOf("video/mp4"),
                            null
                        )
                        _recordingStatus.value = RecordingStatus.Completed(videoFile, durationMs)
                    } else {
                        val errorMessage = when (recordEvent.error) {
                            VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE -> "Insufficient storage space to finish recording"
                            VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED -> "Video recording reached file size limit"
                            VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE -> "Camera hardware became inactive"
                            VideoRecordEvent.Finalize.ERROR_ENCODING_FAILED -> "Video encoding error occurred"
                            VideoRecordEvent.Finalize.ERROR_NO_VALID_DATA -> "No video data was recorded"
                            else -> recordEvent.cause?.localizedMessage ?: "Recording error (${recordEvent.error})"
                        }
                        _recordingStatus.value = RecordingStatus.Error(errorMessage)
                    }
                    activeRecording = null
                    recordingStartEpochMs = 0L
                }
            }
        }

        return videoFile
    }

    fun pauseRecording() {
        try {
            activeRecording?.pause()
        } catch (e: Exception) {
            _recordingStatus.value = RecordingStatus.Error("Failed to pause: ${e.message}")
        }
    }

    fun resumeRecording() {
        try {
            activeRecording?.resume()
        } catch (e: Exception) {
            _recordingStatus.value = RecordingStatus.Error("Failed to resume: ${e.message}")
        }
    }

    fun stopRecording() {
        try {
            activeRecording?.stop()
        } catch (e: Exception) {
            _recordingStatus.value = RecordingStatus.Error("Failed to stop: ${e.message}")
        } finally {
            activeRecording = null
        }
    }

    fun release() {
        try {
            orientationEventListener?.disable()
            stopRecording()
            cameraProvider?.unbindAll()
            camera = null
            videoCapture = null
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
