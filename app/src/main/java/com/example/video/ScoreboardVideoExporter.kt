package com.example.video

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Effects
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.MatchEntity
import com.example.data.model.OverlayStyle
import com.example.domain.engine.CricketRulesEngine
import com.example.domain.engine.InningsLiveState
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class VideoExportState {
    object Idle : VideoExportState()
    data class Exporting(val progressPercent: Int) : VideoExportState()
    data class Success(val exportedFile: File, val mediaStoreUri: Uri?) : VideoExportState()
    data class Failed(val error: String) : VideoExportState()
}

@OptIn(UnstableApi::class)
class ScoreboardVideoExporter(private val context: Context) {

    private val _exportState = MutableStateFlow<VideoExportState>(VideoExportState.Idle)
    val exportState: StateFlow<VideoExportState> = _exportState.asStateFlow()

    private var activeTransformer: Transformer? = null
    private var progressPollingJob: Job? = null

    fun exportVideoWithScoreboard(
        scope: CoroutineScope,
        sourceVideoFile: File,
        match: MatchEntity,
        inningsNumber: Int,
        allDeliveries: List<BallDeliveryEntity>,
        overlayStyle: OverlayStyle = OverlayStyle.LOWER_THIRD,
        overlayScale: Float = 1.0f,
        muteAudio: Boolean = false
    ) {
        if (!sourceVideoFile.exists() || sourceVideoFile.length() == 0L) {
            _exportState.value = VideoExportState.Failed("Source video file does not exist or is empty")
            return
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        val exportedFile = File(outputDir, "RCB_EXPORTED_$timeStamp.mp4")

        _exportState.value = VideoExportState.Exporting(0)

        scope.launch(Dispatchers.Main) {
            try {
                val mediaItem = MediaItem.fromUri(Uri.fromFile(sourceVideoFile))

                // Build BitmapOverlay that updates scoreboard based on presentation timestamp
                val bitmapOverlay = object : BitmapOverlay() {
                    private var cachedBitmap: Bitmap? = null
                    private var cachedCanvas: Canvas? = null

                    override fun getBitmap(presentationTimeUs: Long): Bitmap {
                        val videoTimestampMs = presentationTimeUs / 1000

                        // Find state of match at this timestamp
                        val deliveriesUpToTime = allDeliveries.filter { delivery ->
                            val t = delivery.videoTimestampMs
                            t == null || t <= videoTimestampMs
                        }

                        val liveState = CricketRulesEngine.calculateInningsState(
                            match = match,
                            inningsNumber = inningsNumber,
                            deliveries = deliveriesUpToTime
                        )

                        val bmp = cachedBitmap ?: Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888).also {
                            cachedBitmap = it
                            cachedCanvas = Canvas(it)
                        }
                        bmp.eraseColor(android.graphics.Color.TRANSPARENT)
                        cachedCanvas?.let { c ->
                            ScoreboardOverlayDrawer.drawScoreboard(
                                canvas = c,
                                width = bmp.width,
                                height = bmp.height,
                                state = liveState,
                                style = overlayStyle,
                                scale = overlayScale
                            )
                        }
                        return bmp
                    }
                }

                val overlayEffect = OverlayEffect(listOf<TextureOverlay>(bitmapOverlay))
                val effects = Effects(
                    /* audioProcessors = */ emptyList(),
                    /* videoEffects = */ listOf(overlayEffect)
                )

                val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                    .setEffects(effects)
                    .setRemoveAudio(muteAudio)
                    .build()

                val transformerListener = object : Transformer.Listener {
                    override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                        progressPollingJob?.cancel()
                        scope.launch(Dispatchers.IO) {
                            if (exportedFile.exists() && exportedFile.length() > 0) {
                                val galleryUri = copyToGallery(exportedFile)
                                withContext(Dispatchers.Main) {
                                    _exportState.value = VideoExportState.Success(exportedFile, galleryUri)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    _exportState.value = VideoExportState.Failed("Export finished but output file is invalid")
                                }
                            }
                        }
                    }

                    override fun onError(
                        composition: androidx.media3.transformer.Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        progressPollingJob?.cancel()
                        _exportState.value = VideoExportState.Failed(
                            "Export failed: ${exportException.localizedMessage ?: "Processing error"}"
                        )
                    }
                }

                val transformer = Transformer.Builder(context)
                    .addListener(transformerListener)
                    .build()
                activeTransformer = transformer

                transformer.start(editedMediaItem, exportedFile.absolutePath)

                // Start polling progress
                val progressHolder = ProgressHolder()
                progressPollingJob = scope.launch(Dispatchers.Default) {
                    while (isActive) {
                        delay(250)
                        val progressState = transformer.getProgress(progressHolder)
                        if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                            withContext(Dispatchers.Main) {
                                if (_exportState.value is VideoExportState.Exporting) {
                                    _exportState.value = VideoExportState.Exporting(progressHolder.progress)
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                progressPollingJob?.cancel()
                _exportState.value = VideoExportState.Failed("Could not start export: ${e.message}")
            }
        }
    }

    fun cancelExport() {
        progressPollingJob?.cancel()
        activeTransformer?.cancel()
        activeTransformer = null
        _exportState.value = VideoExportState.Idle
    }

    private fun copyToGallery(file: File): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/RajpootCricket")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(file).use { input ->
                    input.copyTo(out)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            return uri
        } catch (e: Exception) {
            return null
        }
    }
}
