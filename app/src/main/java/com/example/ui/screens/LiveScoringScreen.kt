package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.RecordingStatus
import com.example.data.model.ExtrasType
import com.example.data.model.PlayerEntity
import com.example.data.model.WicketType
import com.example.ui.components.CricketBallSeamSpinner
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.OutlinedGoldButton
import com.example.ui.components.ScoreboardOverlayComposable
import com.example.ui.theme.AccentExtra
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CricketViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScoringScreen(
    matchId: Long,
    viewModel: CricketViewModel,
    onBack: () -> Unit,
    onNavigateToScorecard: (Long) -> Unit,
    onNavigateToVideoExport: (Long) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.currentMatch.collectAsStateWithLifecycle()
    val state by viewModel.inningsLiveState.collectAsStateWithLifecycle()
    val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle()
    val recordingStatus by viewModel.cameraManager.recordingStatus.collectAsStateWithLifecycle()
    val isFlashAvailable by viewModel.cameraManager.isFlashAvailable.collectAsStateWithLifecycle()
    val isFrontFacing by viewModel.cameraManager.isFrontFacing.collectAsStateWithLifecycle()
    val isAudioEnabled by viewModel.cameraManager.isAudioEnabled.collectAsStateWithLifecycle()

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (hasCameraPermission && previewViewRef != null) {
            viewModel.cameraManager.bindCamera(lifecycleOwner, previewViewRef!!) {}
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(recordingStatus) {
        if (recordingStatus is RecordingStatus.Error) {
            errorMessage = (recordingStatus as RecordingStatus.Error).message
        }
    }

    // Keep screen awake while in live scoring / recording
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewModel.cameraManager.release()
        }
    }

    // Dialog states
    var showWicketDialog by remember { mutableStateOf(false) }
    var showExtrasDialog by remember { mutableStateOf(false) }
    var showChangeBowlerDialog by remember { mutableStateOf(false) }
    var showInningsSwitchDialog by remember { mutableStateOf(false) }
    var showEndMatchDialog by remember { mutableStateOf(false) }

    // Auto-prompt innings switch if 1st innings complete
    LaunchedEffect(state.isInningsComplete) {
        if (state.isInningsComplete && state.inningsNumber == 1 && !state.isMatchComplete) {
            showInningsSwitchDialog = true
        }
    }

    Scaffold(
        containerColor = CharcoalBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TOP: Camera Preview with Live Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .background(Color.Black)
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { pv ->
                                previewViewRef = pv
                                viewModel.cameraManager.bindCamera(lifecycleOwner, pv) { err ->
                                    errorMessage = err
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    previewViewRef?.let { pv ->
                                        viewModel.cameraManager.focusAndMeter(pv, offset.x, offset.y)
                                    }
                                }
                            }
                    )
                } else {
                    // Permission Explanation & Request Box
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera & Audio Access Needed",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To record live cricket video and composite real-time broadcast scoreboards, grant Camera and Microphone access.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GoldButton(
                            text = "Grant Camera Permissions",
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                )
                            }
                        )
                    }
                }

                // Top Controls Bar (Back, Torch, Switch Camera, Recording status)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    // Recording indicator & timer
                    when (val rec = recordingStatus) {
                        is RecordingStatus.Recording -> {
                            val seconds = (rec.durationMs / 1000) % 60
                            val minutes = (rec.durationMs / 60000)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xCCB71C1C))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format(Locale.US, "REC %02d:%02d", minutes, seconds),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        is RecordingStatus.Paused -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xCCF59E0B))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = "PAUSED", color = CharcoalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x99000000))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = "STANDBY", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Camera Controls Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Audio Toggle
                        IconButton(
                            onClick = {
                                if (!hasAudioPermission) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                } else {
                                    viewModel.cameraManager.toggleAudio()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                        ) {
                            Icon(
                                if (isAudioEnabled && hasAudioPermission) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Toggle Audio Recording",
                                tint = if (isAudioEnabled && hasAudioPermission) GoldLight else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isFlashAvailable && !isFrontFacing) {
                            IconButton(
                                onClick = { viewModel.cameraManager.toggleTorch() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x99000000))
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = "Torch", tint = GoldLight, modifier = Modifier.size(18.dp))
                            }
                        }

                        IconButton(
                            onClick = {
                                previewViewRef?.let { pv ->
                                    viewModel.cameraManager.toggleCamera(lifecycleOwner, pv)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                        ) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // BOTTOM OF CAMERA: Scoreboard Broadcast Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    ScoreboardOverlayComposable(state = state)
                }
            }

            // MIDDLE: Recording Action Bar & Over status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Record / Pause / Stop controls
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    when (recordingStatus) {
                        is RecordingStatus.Idle, is RecordingStatus.Completed -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentWicket)
                                    .clickable {
                                        viewModel.startCameraRecording { err -> errorMessage = err }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Record", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        is RecordingStatus.Recording -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF333344))
                                    .clickable { viewModel.cameraManager.pauseRecording() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pause", color = Color.White, fontSize = 12.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentWicket)
                                    .clickable { viewModel.stopCameraRecording() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        is RecordingStatus.Paused -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentFour)
                                    .clickable { viewModel.cameraManager.resumeRecording() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume", color = CharcoalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentWicket)
                                    .clickable { viewModel.stopCameraRecording() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        else -> {}
                    }

                    // Export Video Button if video recorded
                    if (match?.sourceVideoPath != null || recordingStatus is RecordingStatus.Completed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33D4AF37))
                                .clickable { onNavigateToVideoExport(matchId) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = GoldBright, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export Video", color = GoldBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Undo & Scorecard links
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.undoLastBall() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalElevated)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = GoldLight, modifier = Modifier.size(18.dp))
                    }

                    OutlinedGoldButton(
                        text = "Card",
                        onClick = { onNavigateToScorecard(matchId) },
                        modifier = Modifier.height(36.dp)
                    )
                }
            }

            // BOTTOM: Large Touch-Friendly Scoring Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalBlack)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active Bowler & Delivery indicator with animated CricketBallSeamSpinner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CricketBallSeamSpinner(sizeDp = 18.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bowler: ${state.currentBowler?.bowlerName ?: "Select Bowler"}",
                            color = GoldBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Ball ${state.ballsInCurrentOver}/6 (Ov ${state.legalBalls / 6}.${state.ballsInCurrentOver})",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Row 1: Primary Runs (0, 1, 2, 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreKeypadButton(text = "• 0", modifier = Modifier.weight(1f), bg = Color(0xFF1E1E28)) {
                        viewModel.scoreBall(runsOffBat = 0)
                    }
                    ScoreKeypadButton(text = "1", modifier = Modifier.weight(1f), bg = Color(0xFF28283A)) {
                        viewModel.scoreBall(runsOffBat = 1)
                    }
                    ScoreKeypadButton(text = "2", modifier = Modifier.weight(1f), bg = Color(0xFF28283A)) {
                        viewModel.scoreBall(runsOffBat = 2)
                    }
                    ScoreKeypadButton(text = "3", modifier = Modifier.weight(1f), bg = Color(0xFF28283A)) {
                        viewModel.scoreBall(runsOffBat = 3)
                    }
                }

                // Row 2: Boundaries (4, 6), Extras, WICKET
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreKeypadButton(
                        text = "FOUR 4",
                        modifier = Modifier.weight(1f),
                        bg = Color(0xFF065F46),
                        textColor = Color(0xFF6EE7B7)
                    ) {
                        viewModel.scoreBall(runsOffBat = 4)
                    }

                    ScoreKeypadButton(
                        text = "SIX 6",
                        modifier = Modifier.weight(1f),
                        bg = Color(0xFF5B21B6),
                        textColor = Color(0xFFC4B5FD)
                    ) {
                        viewModel.scoreBall(runsOffBat = 6)
                    }

                    ScoreKeypadButton(
                        text = "EXTRAS",
                        modifier = Modifier.weight(1f),
                        bg = Color(0xFF78350F),
                        textColor = Color(0xFFFDE68A)
                    ) {
                        showExtrasDialog = true
                    }

                    ScoreKeypadButton(
                        text = "WICKET",
                        modifier = Modifier.weight(1.2f),
                        bg = AccentWicket,
                        textColor = Color.White
                    ) {
                        showWicketDialog = true
                    }
                }

                // Row 3: Change Bowler & Match Status Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedGoldButton(
                        text = "Change Bowler",
                        onClick = { showChangeBowlerDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    if (state.inningsNumber == 1) {
                        OutlinedGoldButton(
                            text = "End Innings 1",
                            onClick = { showInningsSwitchDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        OutlinedGoldButton(
                            text = "End Match",
                            onClick = { showEndMatchDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Wicket Dialog
    if (showWicketDialog) {
        WicketEntryDialog(
            state = state,
            allPlayers = allPlayers,
            onDismiss = { showWicketDialog = false },
            onConfirm = { type, dismissedName, fielderName, newBatterName ->
                viewModel.scoreBall(
                    runsOffBat = 0,
                    isWicket = true,
                    wicketType = type,
                    dismissedPlayerName = dismissedName,
                    fielderPlayerName = fielderName,
                    newIncomingBatterName = newBatterName
                )
                showWicketDialog = false
            }
        )
    }

    // Extras Dialog (Wide, No-Ball, Bye, Leg-Bye)
    if (showExtrasDialog) {
        ExtrasEntryDialog(
            onDismiss = { showExtrasDialog = false },
            onConfirm = { extrasType, extrasRuns, runsOffBat ->
                viewModel.scoreBall(
                    runsOffBat = runsOffBat,
                    extrasType = extrasType,
                    extrasRuns = extrasRuns
                )
                showExtrasDialog = false
            }
        )
    }

    // Change Bowler Dialog
    if (showChangeBowlerDialog || (state.isOverComplete && !state.isInningsComplete)) {
        BowlerSelectionDialog(
            currentBowler = state.currentBowler?.bowlerName,
            allPlayers = allPlayers,
            onDismiss = { showChangeBowlerDialog = false },
            onSelect = { selectedBowler ->
                viewModel.selectedBowlerName.value = selectedBowler
                showChangeBowlerDialog = false
            }
        )
    }

    // Innings Switch Dialog
    if (showInningsSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showInningsSwitchDialog = false },
            containerColor = CharcoalDark,
            title = { Text("Complete 1st Innings?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "${state.battingTeam} finished with ${state.totalRuns}/${state.totalWickets} in ${state.formattedOvers} overs.\nTarget for 2nd innings: ${state.totalRuns + 1} runs.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                GoldButton(
                    text = "Start 2nd Innings",
                    onClick = {
                        viewModel.switchInnings()
                        showInningsSwitchDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showInningsSwitchDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // End Match Dialog
    if (showEndMatchDialog) {
        AlertDialog(
            onDismissRequest = { showEndMatchDialog = false },
            containerColor = CharcoalDark,
            title = { Text("Finalize Match?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "This will mark the match as completed and lock the final scorecards.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                GoldButton(
                    text = "Finalize",
                    onClick = {
                        viewModel.endMatchManually()
                        showEndMatchDialog = false
                        onNavigateToScorecard(matchId)
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showEndMatchDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Error Alert
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            containerColor = CharcoalDark,
            title = { Text("Camera Notice", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage!!, color = TextSecondary) },
            confirmButton = {
                GoldButton(text = "OK", onClick = { errorMessage = null })
            }
        )
    }
}

@Composable
fun ScoreKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    bg: Color = CharcoalElevated,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WicketEntryDialog(
    state: com.example.domain.engine.InningsLiveState,
    allPlayers: List<PlayerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (WicketType, String, String?, String) -> Unit
) {
    var wicketType by remember { mutableStateOf(WicketType.BOWLED) }
    var dismissedPlayer by remember {
        mutableStateOf(state.currentStriker?.playerName ?: "Striker")
    }
    var fielderName by remember { mutableStateOf("") }
    var newBatterName by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = { Text("Record Wicket / Dismissal", color = AccentWicket, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dismissal Type
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = wicketType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dismissal Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.background(CharcoalElevated)
                    ) {
                        WicketType.values().filter { it != WicketType.NONE }.forEach { wType ->
                            DropdownMenuItem(
                                text = { Text(wType.displayName, color = Color.White) },
                                onClick = {
                                    wicketType = wType
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dismissed Batsman Selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val striker = state.currentStriker?.playerName ?: "Striker"
                    val nonStriker = state.currentNonStriker?.playerName ?: "Non-striker"

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dismissedPlayer == striker) GoldPrimary else CharcoalElevated)
                            .clickable { dismissedPlayer = striker }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(striker, color = if (dismissedPlayer == striker) CharcoalBlack else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dismissedPlayer == nonStriker) GoldPrimary else CharcoalElevated)
                            .clickable { dismissedPlayer = nonStriker }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(nonStriker, color = if (dismissedPlayer == nonStriker) CharcoalBlack else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (wicketType == WicketType.CAUGHT || wicketType == WicketType.RUN_OUT || wicketType == WicketType.STUMPED) {
                    OutlinedTextField(
                        value = fielderName,
                        onValueChange = { fielderName = it },
                        label = { Text("Fielder / Catcher Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                OutlinedTextField(
                    value = newBatterName,
                    onValueChange = { newBatterName = it },
                    label = { Text("Next Incoming Batter Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            GoldButton(
                text = "Confirm Out",
                onClick = {
                    onConfirm(wicketType, dismissedPlayer, fielderName.ifEmpty { null }, newBatterName.ifEmpty { "New Batter" })
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun ExtrasEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (ExtrasType, Int, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(ExtrasType.WIDE) }
    var extraRuns by remember { mutableStateOf(1) }
    var runsOffBat by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = { Text("Record Extras", color = GoldBright, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Extras type buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(ExtrasType.WIDE, ExtrasType.NO_BALL, ExtrasType.BYE, ExtrasType.LEG_BYE).forEach { type ->
                        val isSel = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldPrimary else CharcoalElevated)
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type.displayName, color = if (isSel) CharcoalBlack else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Text("Extras penalty / runs:", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4, 5).forEach { r ->
                        val isSel = extraRuns == r
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSel) GoldPrimary else CharcoalElevated)
                                .clickable { extraRuns = r },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+$r", color = if (isSel) CharcoalBlack else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (selectedType == ExtrasType.NO_BALL) {
                    Text("Runs scored off bat on no-ball:", color = TextSecondary, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 1, 2, 3, 4, 6).forEach { r ->
                            val isSel = runsOffBat == r
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) AccentFour else CharcoalElevated)
                                    .clickable { runsOffBat = r },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$r", color = if (isSel) CharcoalBlack else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GoldButton(
                text = "Apply Extras",
                onClick = { onConfirm(selectedType, extraRuns, runsOffBat) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun BowlerSelectionDialog(
    currentBowler: String?,
    allPlayers: List<PlayerEntity>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var customBowlerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = { Text("Select Bowler for Next Over", color = GoldBright, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select from squad (excluding last bowler: $currentBowler):", color = TextSecondary, fontSize = 12.sp)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allPlayers.take(6).filter { it.fullName != currentBowler }.forEach { p ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CharcoalElevated)
                                .clickable { onSelect(p.fullName) }
                                .padding(10.dp)
                        ) {
                            Text("${p.fullName} (${p.bowlingStyle.displayName})", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = customBowlerName,
                    onValueChange = { customBowlerName = it },
                    label = { Text("Or Type Bowler Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            GoldButton(
                text = "Set Bowler",
                onClick = {
                    if (customBowlerName.isNotBlank()) onSelect(customBowlerName.trim())
                },
                enabled = customBowlerName.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextSecondary) }
        }
    )
}
