package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.OutlinedGoldButton
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.LiquidGlassTheme
import com.example.video.VideoExportState
import com.example.viewmodel.CricketViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoExportScreen(
    matchId: Long,
    viewModel: CricketViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val tokens = LiquidGlassTheme.tokens

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.currentMatch.collectAsStateWithLifecycle()
    val exportState by viewModel.videoExporter.exportState.collectAsStateWithLifecycle()
    val overlayStyle by viewModel.overlayStyleSetting.collectAsStateWithLifecycle()

    val sourceFile = remember(match) {
        match?.sourceVideoPath?.let { File(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scoreboard Video Export", color = tokens.textPrimary, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tokens.accentGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source Video Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = tokens.glassCardBackground,
                borderColor = tokens.glassCardBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = tokens.accentGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Source Video Recording", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(sourceFile?.name ?: "No video file found", color = tokens.textSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (sourceFile != null && sourceFile.exists()) {
                        val sizeMb = sourceFile.length() / (1024 * 1024)
                        Text("File size: $sizeMb MB", color = tokens.textSecondary, fontSize = 12.sp)
                        Text("Scoreboard Layout: ${overlayStyle.displayName}", color = tokens.accentGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(
                            "Record cricket footage first from the Live Match screen to export with the scoreboard overlay.",
                            color = AccentWicket,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Export Processing Status Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = tokens.glassCardBackground,
                borderColor = tokens.glassCardBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = exportState) {
                        is VideoExportState.Idle -> {
                            Text("Ready to Composite", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Media3 Transformer will permanently composite the live scores, wickets, overs, and batter/bowler figures into every frame of your video.",
                                color = tokens.textSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            GoldButton(
                                text = "Start Video Export",
                                onClick = {
                                    if (sourceFile != null && sourceFile.exists()) {
                                        viewModel.exportVideo(sourceFile)
                                    }
                                },
                                enabled = sourceFile != null && sourceFile.exists(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is VideoExportState.Exporting -> {
                            CircularProgressIndicator(
                                progress = { state.progressPercent / 100f },
                                modifier = Modifier.size(60.dp),
                                color = tokens.accentGold,
                                trackColor = tokens.glassCardBorderSubtle
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Compositing Scoreboard: ${state.progressPercent}%",
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Drawing broadcast graphics onto video frames and encoding MP4...",
                                color = tokens.textSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { state.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = tokens.accentGold,
                                trackColor = tokens.glassCardBorderSubtle
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedGoldButton(
                                text = "Cancel Export",
                                onClick = { viewModel.videoExporter.cancelExport() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is VideoExportState.Success -> {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentFour, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Export Complete!", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Video saved to device gallery and Movies folder.\n${state.exportedFile.name}",
                                color = tokens.textSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val playableUri = state.mediaStoreUri ?: try {
                                    androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        state.exportedFile
                                    )
                                } catch (_: Exception) {
                                    Uri.fromFile(state.exportedFile)
                                }

                                GoldButton(
                                    text = "Play Video",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(playableUri, "video/mp4")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Play Exported Video"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = tokens.buttonContent) }
                                )

                                OutlinedGoldButton(
                                    text = "Share",
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, playableUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Match Video"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = tokens.accentGold) }
                                )
                            }
                        }

                        is VideoExportState.Failed -> {
                            Icon(Icons.Default.Error, contentDescription = null, tint = AccentWicket, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Export Failed", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(state.error, color = tokens.textSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            GoldButton(
                                text = "Retry Export",
                                onClick = {
                                    if (sourceFile != null && sourceFile.exists()) {
                                        viewModel.exportVideo(sourceFile)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
