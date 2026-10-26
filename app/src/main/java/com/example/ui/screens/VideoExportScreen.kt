package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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
                title = { Text("Scoreboard Video Export", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        containerColor = CharcoalBlack
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
                backgroundColor = Color(0xCC161622)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = GoldBright, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Source Video Recording", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(sourceFile?.name ?: "No video file found", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (sourceFile != null && sourceFile.exists()) {
                        val sizeMb = sourceFile.length() / (1024 * 1024)
                        Text("File size: $sizeMb MB", color = TextSecondary, fontSize = 12.sp)
                        Text("Scoreboard Layout: ${overlayStyle.displayName}", color = GoldLight, fontSize = 12.sp)
                    } else {
                        Text(
                            "Record cricket footage first from the Live Scoring screen to export with the scoreboard overlay.",
                            color = AccentWicket,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Export Processing Status Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xCC161622)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = exportState) {
                        is VideoExportState.Idle -> {
                            Text("Ready to Composite", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Media3 Transformer will permanently composite the live scores, wickets, overs, and batter/bowler figures into every frame of your video.",
                                color = TextSecondary,
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
                                color = GoldPrimary,
                                trackColor = Color(0x33D4AF37)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Compositing Scoreboard: ${state.progressPercent}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Drawing broadcast graphics onto video frames and encoding MP4...",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { state.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = GoldPrimary,
                                trackColor = Color(0x33D4AF37)
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
                            Text("Export Complete!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Video saved to device gallery and Movies folder.\n${state.exportedFile.name}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                GoldButton(
                                    text = "Play Video",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(state.mediaStoreUri ?: Uri.fromFile(state.exportedFile), "video/mp4")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Play Exported Video"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = null, tint = CharcoalBlack) }
                                )

                                OutlinedGoldButton(
                                    text = "Share",
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, state.mediaStoreUri ?: Uri.fromFile(state.exportedFile))
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Match Video"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = GoldBright) }
                                )
                            }
                        }

                        is VideoExportState.Failed -> {
                            Icon(Icons.Default.Error, contentDescription = null, tint = AccentWicket, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Export Failed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(state.error, color = TextSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
