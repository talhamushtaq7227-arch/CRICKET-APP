package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.findActivity
import com.example.camera.RecordingStatus
import com.example.data.model.ExtrasType
import com.example.data.model.PlayerEntity
import com.example.data.model.WicketType
import com.example.domain.engine.InningsLiveState
import com.example.ui.components.CricketBallSeamSpinner
import com.example.ui.components.CyanPillTabs
import com.example.ui.components.DeliveryChip
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.OutlinedGoldButton
import com.example.ui.components.ScoreboardOverlayComposable
import com.example.ui.components.SkyCyanLiveScoreBanner
import com.example.ui.theme.AccentExtra
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LiquidGlassTheme
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CricketViewModel
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
    val tokens = LiquidGlassTheme.tokens

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
    // Tabs: 0 = Live Scoring, 1 = Broadcast Camera, 2 = Scoreboard, 3 = Commentary, 4 = Match Info
    var selectedMatchTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(recordingStatus) {
        if (recordingStatus is RecordingStatus.Error) {
            errorMessage = (recordingStatus as RecordingStatus.Error).message
        }
    }

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
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
    var showChangeBatsmanDialog by remember { mutableStateOf(false) }
    var showInningsSwitchDialog by remember { mutableStateOf(false) }
    var showEndMatchDialog by remember { mutableStateOf(false) }

    // Track whether the user already dismissed or handled the current completed over prompt
    var lastPromptedCompletedOverBallCount by remember { mutableIntStateOf(-1) }

    LaunchedEffect(state.isOverComplete, state.legalBalls) {
        if (state.isOverComplete && !state.isInningsComplete && state.legalBalls > 0) {
            if (lastPromptedCompletedOverBallCount != state.legalBalls) {
                lastPromptedCompletedOverBallCount = state.legalBalls
                showChangeBowlerDialog = true
            }
        }
    }

    LaunchedEffect(state.isInningsComplete) {
        if (state.isInningsComplete && state.inningsNumber == 1 && !state.isMatchComplete) {
            showInningsSwitchDialog = true
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Reference App Bar + Large Sky-Cyan Live Score Banner + Cyan Pill Tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Row 1: < Back, Centered "Live Match", Undo + More (•••)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = tokens.textPrimary
                        )
                    }

                    Text(
                        text = "Live Match",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.undoLastBall() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo Last Ball",
                                tint = tokens.accentGold,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        IconButton(
                            onClick = { onNavigateToScorecard(matchId) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Full Scorecard",
                                tint = tokens.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Row 2: Large Sky-Cyan Live Score Panel matching reference image
                val leftTeam = state.battingTeam.ifEmpty { match?.homeTeamName ?: "Rajpoot Boys" }
                val rightTeam = state.bowlingTeam.ifEmpty { match?.awayTeamName ?: "Rival XI" }
                val targetVal = state.target
                val rightScoreText = if (targetVal != null) {
                    "${(targetVal - 1).coerceAtLeast(0)}"
                } else {
                    "Yet to bat"
                }
                val rightOversText = "(${state.totalOversConfigured} ov)"
                val resSummary = state.resultSummary
                val liveStatusText: String = when {
                    resSummary != null -> resSummary
                    targetVal != null -> {
                        val needRuns = state.runsRequired ?: 0
                        "${leftTeam} need $needRuns runs in ${state.ballsRemaining} balls"
                    }
                    else -> "${leftTeam} • CRR ${state.formattedCRR} • Partnership ${state.partnership.runs} (${state.partnership.balls})"
                }

                SkyCyanLiveScoreBanner(
                    leftTeamName = leftTeam,
                    leftScore = "${state.totalRuns}/${state.totalWickets}",
                    leftOvers = "(${state.formattedOvers} ov)",
                    rightTeamName = rightTeam,
                    rightScore = rightScoreText,
                    rightOvers = rightOversText,
                    statusText = liveStatusText,
                    isLive = !state.isMatchComplete
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Reference Cyan Pill Tabs (Live, Match Info, Scoreboard, Commentary, Camera)
                val tabs = listOf("Live", "Camera", "Scoreboard", "Commentary", "Match Info")
                CyanPillTabs(
                    tabs = tabs,
                    selectedIndex = selectedMatchTab,
                    onTabSelected = { selectedMatchTab = it }
                )
            }

            // MIDDLE: Tab Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedMatchTab) {
                    0 -> LiveBatsmenAndBowlerHub(
                        state = state,
                        onSwapStrike = { viewModel.swapStrikeManually() },
                        onOpenChangeBatsman = { showChangeBatsmanDialog = true },
                        onOpenChangeBowler = { showChangeBowlerDialog = true }
                    )
                    1 -> BroadcastCameraTab(
                        state = state,
                        hasCameraPermission = hasCameraPermission,
                        hasAudioPermission = hasAudioPermission,
                        recordingStatus = recordingStatus,
                        isFlashAvailable = isFlashAvailable,
                        isFrontFacing = isFrontFacing,
                        isAudioEnabled = isAudioEnabled,
                        hasRecordedVideo = match?.sourceVideoPath != null || recordingStatus is RecordingStatus.Completed,
                        onGrantPermissions = {
                            permissionLauncher.launch(
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                            )
                        },
                        onBindCamera = { pv ->
                            previewViewRef = pv
                            viewModel.cameraManager.bindCamera(lifecycleOwner, pv) { err -> errorMessage = err }
                        },
                        onTapFocus = { x, y ->
                            previewViewRef?.let { pv -> viewModel.cameraManager.focusAndMeter(pv, x, y) }
                        },
                        onToggleAudio = {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                            } else {
                                viewModel.cameraManager.toggleAudio()
                            }
                        },
                        onToggleTorch = { viewModel.cameraManager.toggleTorch() },
                        onSwitchCamera = {
                            previewViewRef?.let { pv -> viewModel.cameraManager.toggleCamera(lifecycleOwner, pv) }
                        },
                        onStartRecording = { viewModel.startCameraRecording { err -> errorMessage = err } },
                        onPauseRecording = { viewModel.cameraManager.pauseRecording() },
                        onResumeRecording = { viewModel.cameraManager.resumeRecording() },
                        onStopRecording = { viewModel.stopCameraRecording() },
                        onExportVideo = { onNavigateToVideoExport(matchId) }
                    )
                    2 -> LiveScoreboardTab(
                        state = state,
                        onOpenFullScorecard = { onNavigateToScorecard(matchId) }
                    )
                    3 -> LiveCommentaryTab(state = state)
                    4 -> LiveMatchInfoTab(match = match, state = state)
                }
            }

            // BOTTOM: Glowing Cyan Scoring Keypad & Player Selection Actions
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                backgroundColor = tokens.glassCardElevated,
                borderColor = tokens.glassCardBorder
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Primary Runs (0, 1, 2, 3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreKeypadButton(
                            text = "• 0",
                            modifier = Modifier.weight(1f),
                            bg = Color(0xFF0D2142),
                            borderColor = Color(0x4400E5FF)
                        ) {
                            viewModel.scoreBall(runsOffBat = 0)
                        }
                        ScoreKeypadButton(
                            text = "1",
                            modifier = Modifier.weight(1f),
                            bg = Color(0xFF122C56),
                            borderColor = Color(0x6600E5FF)
                        ) {
                            viewModel.scoreBall(runsOffBat = 1)
                        }
                        ScoreKeypadButton(
                            text = "2",
                            modifier = Modifier.weight(1f),
                            bg = Color(0xFF122C56),
                            borderColor = Color(0x6600E5FF)
                        ) {
                            viewModel.scoreBall(runsOffBat = 2)
                        }
                        ScoreKeypadButton(
                            text = "3",
                            modifier = Modifier.weight(1f),
                            bg = Color(0xFF122C56),
                            borderColor = Color(0x6600E5FF)
                        ) {
                            viewModel.scoreBall(runsOffBat = 3)
                        }
                    }

                    // Row 2: Boundaries (FOUR 4, SIX 6), EXTRAS, WICKET
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreKeypadButton(
                            text = "FOUR 4",
                            modifier = Modifier.weight(1f),
                            bg = Color(0xFF004D40),
                            textColor = AccentFour,
                            borderColor = AccentFour
                        ) {
                            viewModel.scoreBall(runsOffBat = 4)
                        }

                        ScoreKeypadButton(
                            text = "SIX 6",
                            modifier = Modifier.weight(1f),
                            bg = Color(0xFF004E66),
                            textColor = GoldLight,
                            borderColor = GoldPrimary
                        ) {
                            viewModel.scoreBall(runsOffBat = 6)
                        }

                        ScoreKeypadButton(
                            text = "EXTRAS",
                            modifier = Modifier.weight(1f),
                            bg = Color(0xFF4A3200),
                            textColor = AccentExtra,
                            borderColor = AccentExtra
                        ) {
                            showExtrasDialog = true
                        }

                        ScoreKeypadButton(
                            text = "WICKET",
                            modifier = Modifier.weight(1.15f),
                            bg = AccentWicket,
                            textColor = Color.White,
                            borderColor = Color(0xFFFF8A9E)
                        ) {
                            showWicketDialog = true
                        }
                    }

                    // Row 3: Reference-Styled "Change Batsman / Bowler" Pill + Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0A2442), Color(0xFF0F355E), Color(0xFF0A2442))
                                    )
                                )
                                .border(1.5.dp, Color(0xFF29E2FF), RoundedCornerShape(21.dp))
                                .clickable { showChangeBatsmanDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = Color(0xFF29E2FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Change Batsman",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0A2442), Color(0xFF0F355E), Color(0xFF0A2442))
                                    )
                                )
                                .border(1.5.dp, Color(0xFF29E2FF), RoundedCornerShape(21.dp))
                                .clickable { showChangeBowlerDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = Color(0xFF29E2FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Change Bowler",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(Color(0xFF091B34))
                                .border(1.dp, tokens.glassCardBorder, RoundedCornerShape(21.dp))
                                .clickable {
                                    if (state.inningsNumber == 1) showInningsSwitchDialog = true
                                    else showEndMatchDialog = true
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.inningsNumber == 1) "End Inn" else "Finish",
                                color = tokens.accentGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
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
                showWicketDialog = false
                viewModel.scoreBall(
                    runsOffBat = 0,
                    isWicket = true,
                    wicketType = type,
                    dismissedPlayerName = dismissedName,
                    fielderPlayerName = fielderName,
                    newIncomingBatterName = newBatterName
                )
            }
        )
    }

    // Extras Dialog
    if (showExtrasDialog) {
        ExtrasEntryDialog(
            onDismiss = { showExtrasDialog = false },
            onConfirm = { extrasType, extrasRuns, runsOffBat ->
                showExtrasDialog = false
                viewModel.scoreBall(
                    runsOffBat = runsOffBat,
                    extrasType = extrasType,
                    extrasRuns = extrasRuns
                )
            }
        )
    }

    // Change Batsman Dialog (Select or Add Striker & Non-Striker)
    if (showChangeBatsmanDialog) {
        BatsmanSelectionDialog(
            state = state,
            allPlayers = allPlayers,
            onDismiss = { showChangeBatsmanDialog = false },
            onConfirm = { newStriker, newNonStriker ->
                showChangeBatsmanDialog = false
                viewModel.selectBatters(newStriker, newNonStriker)
            }
        )
    }

    // Change Bowler Dialog
    if (showChangeBowlerDialog) {
        BowlerSelectionDialog(
            currentBowler = state.currentBowler?.bowlerName,
            allPlayers = allPlayers,
            onDismiss = { showChangeBowlerDialog = false },
            onSelect = { selectedBowler ->
                showChangeBowlerDialog = false
                viewModel.selectBowler(selectedBowler)
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
                    text = "${state.battingTeam} scored ${state.totalRuns}/${state.totalWickets} in ${state.formattedOvers} overs.\nTarget for 2nd Innings: ${state.totalRuns + 1} runs.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                GoldButton(
                    text = "Start 2nd Innings",
                    onClick = {
                        showInningsSwitchDialog = false
                        viewModel.switchInnings()
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
                    text = "This will mark the match as completed and save the official scorecard.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                GoldButton(
                    text = "Finalize Match",
                    onClick = {
                        showEndMatchDialog = false
                        viewModel.endMatchManually()
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
private fun LiveBatsmenAndBowlerHub(
    state: InningsLiveState,
    onSwapStrike: () -> Unit,
    onOpenChangeBatsman: () -> Unit,
    onOpenChangeBowler: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val recentFourDeliveries = remember(state.recentDeliveries) {
        state.recentDeliveries.asReversed().take(4)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Batsmen Table Card (matches Middle Phone in reference image)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = tokens.glassCardBackground,
                borderColor = tokens.glassCardBorder
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Table Header Row: Batsmen | R | B | 4s | 6s | SR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Batsmen",
                                color = tokens.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tokens.chipBackground)
                                    .border(1.dp, tokens.accentGold, RoundedCornerShape(8.dp))
                                    .clickable { onSwapStrike() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Swap *", color = tokens.accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        StatHeaderCell("R", Modifier.width(30.dp))
                        StatHeaderCell("B", Modifier.width(30.dp))
                        StatHeaderCell("4s", Modifier.width(26.dp))
                        StatHeaderCell("6s", Modifier.width(26.dp))
                        StatHeaderCell("SR", Modifier.width(44.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = tokens.glassCardBorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val striker = state.currentStriker
                    BatterLiveRow(
                        name = striker?.playerName ?: "Select Striker",
                        runs = striker?.runs ?: 0,
                        balls = striker?.ballsFaced ?: 0,
                        fours = striker?.fours ?: 0,
                        sixes = striker?.sixes ?: 0,
                        sr = striker?.formattedStrikeRate ?: "0.0",
                        isOnStrike = true,
                        onClick = onOpenChangeBatsman
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = tokens.glassCardBorderSubtle.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val nonStriker = state.currentNonStriker
                    BatterLiveRow(
                        name = nonStriker?.playerName ?: "Select Non-Striker",
                        runs = nonStriker?.runs ?: 0,
                        balls = nonStriker?.ballsFaced ?: 0,
                        fours = nonStriker?.fours ?: 0,
                        sixes = nonStriker?.sixes ?: 0,
                        sr = nonStriker?.formattedStrikeRate ?: "0.0",
                        isOnStrike = false,
                        onClick = onOpenChangeBatsman
                    )
                }
            }
        }

        // 2. Bowler Table Card (matches Middle Phone in reference image)
        item {
            val bowler = state.currentBowler
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChangeBowler() },
                backgroundColor = tokens.glassCardBackground,
                borderColor = tokens.glassCardBorder
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Row: Bowler | O | M | R | W | Econ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bowler",
                            color = tokens.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        StatHeaderCell("O", Modifier.width(34.dp))
                        StatHeaderCell("M", Modifier.width(26.dp))
                        StatHeaderCell("R", Modifier.width(30.dp))
                        StatHeaderCell("W", Modifier.width(26.dp))
                        StatHeaderCell("Econ", Modifier.width(44.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = tokens.glassCardBorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(tokens.glassCardElevated)
                                    .border(1.dp, tokens.accentGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = tokens.accentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = bowler?.bowlerName ?: "Tap to Select Bowler",
                                    color = tokens.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Active Bowler",
                                    color = tokens.accentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        StatValueCell(bowler?.formattedOvers ?: "0.0", Modifier.width(34.dp))
                        StatValueCell("${bowler?.maidens ?: 0}", Modifier.width(26.dp))
                        StatValueCell("${bowler?.runsConceded ?: 0}", Modifier.width(30.dp))
                        StatValueCell("${bowler?.wickets ?: 0}", Modifier.width(26.dp))
                        StatValueCell(bowler?.formattedEconomyRate ?: "0.00", Modifier.width(44.dp))
                    }
                }
            }
        }

        // 3. Commentary Section inside Live Hub (matches Middle Phone in reference image)
        item {
            Text(
                text = "Commentary",
                color = tokens.textPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (state.recentDeliveries.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ready for first ball — tap a run button below to score.",
                            color = tokens.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(recentFourDeliveries, key = { it.id }) { ball ->
                CommentaryBallCard(
                    ball = ball,
                    overBalls = state.currentOverDeliveries
                )
            }
        }
    }
}

@Composable
private fun StatHeaderCell(text: String, modifier: Modifier = Modifier) {
    val tokens = LiquidGlassTheme.tokens
    Text(
        text = text,
        color = tokens.textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

@Composable
private fun StatValueCell(text: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    val tokens = LiquidGlassTheme.tokens
    Text(
        text = text,
        color = if (highlight) tokens.accentGoldLight else tokens.textPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

@Composable
private fun CommentaryBallCard(
    ball: com.example.data.model.BallDeliveryEntity,
    overBalls: List<com.example.data.model.BallDeliveryEntity>
) {
    val tokens = LiquidGlassTheme.tokens
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = tokens.glassCardBackground,
        borderColor = tokens.glassCardBorder
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(tokens.accentGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${ball.overNumber}.${ball.legalBallInOver}",
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${ball.bowlerName} to ${ball.strikerName}",
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val desc = ball.commentary.ifEmpty {
                when {
                    ball.isWicket -> "OUT! ${ball.wicketType.displayName} — ${ball.dismissedPlayerName ?: ball.strikerName} departs!"
                    ball.runsOffBat == 6 -> "Six runs! Magnificent strike over the boundary rope!"
                    ball.runsOffBat == 4 -> "Four runs! Excellent timing, the ball races to the boundary."
                    ball.runsOffBat == 0 && ball.extrasType == ExtrasType.NONE -> "No run. Solidly defended back to the bowler."
                    else -> "${ball.totalBallRuns} run(s) taken. Good running between the wickets."
                }
            }
            Text(
                text = desc,
                color = tokens.textSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 13.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeliveryChip(delivery = ball)
                    overBalls.takeLast(4).forEach { ob ->
                        if (ob.id != ball.id) {
                            DeliveryChip(delivery = ob)
                        }
                    }
                    Text(
                        text = "|  ${ball.overNumber}.${ball.legalBallInOver}",
                        color = tokens.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BatterLiveRow(
    name: String,
    runs: Int,
    balls: Int,
    fours: Int,
    sixes: Int,
    sr: String,
    isOnStrike: Boolean,
    onClick: () -> Unit = {}
) {
    val tokens = LiquidGlassTheme.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(tokens.glassCardElevated)
                    .border(
                        width = if (isOnStrike) 1.5.dp else 1.dp,
                        color = if (isOnStrike) tokens.accentGold else tokens.glassCardBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isOnStrike) tokens.accentGold else tokens.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isOnStrike) "$name *" else name,
                    color = tokens.textPrimary,
                    fontWeight = if (isOnStrike) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Not Out",
                    color = tokens.accentGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        StatValueCell("$runs", Modifier.width(30.dp), highlight = isOnStrike)
        StatValueCell("$balls", Modifier.width(30.dp))
        StatValueCell("$fours", Modifier.width(26.dp))
        StatValueCell("$sixes", Modifier.width(26.dp))
        StatValueCell(sr, Modifier.width(44.dp))
    }
}

@Composable
private fun BroadcastCameraTab(
    state: InningsLiveState,
    hasCameraPermission: Boolean,
    hasAudioPermission: Boolean,
    recordingStatus: RecordingStatus,
    isFlashAvailable: Boolean,
    isFrontFacing: Boolean,
    isAudioEnabled: Boolean,
    hasRecordedVideo: Boolean,
    onGrantPermissions: () -> Unit,
    onBindCamera: (PreviewView) -> Unit,
    onTapFocus: (Float, Float) -> Unit,
    onToggleAudio: () -> Unit,
    onToggleTorch: () -> Unit,
    onSwitchCamera: () -> Unit,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onExportVideo: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { pv -> onBindCamera(pv) }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset -> onTapFocus(offset.x, offset.y) }
                        }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Broadcast Pitch Camera", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Grant Camera & Microphone permissions to record live match video with broadcast scoreboard overlays.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GoldButton(text = "Enable Camera", onClick = onGrantPermissions)
                }
            }

            // Camera Top Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (val rec = recordingStatus) {
                    is RecordingStatus.Recording -> {
                        val seconds = (rec.durationMs / 1000) % 60
                        val minutes = (rec.durationMs / 60000)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xCCA8001A))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(Locale.US, "● REC %02d:%02d", minutes, seconds),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
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
                            Text("PAUSED", color = CharcoalBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x99040A18))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("BROADCAST READY", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onToggleAudio,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x99040A18))
                    ) {
                        Icon(
                            if (isAudioEnabled && hasAudioPermission) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Audio",
                            tint = if (isAudioEnabled && hasAudioPermission) GoldLight else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (isFlashAvailable && !isFrontFacing) {
                        IconButton(
                            onClick = onToggleTorch,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x99040A18))
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = "Torch", tint = GoldLight, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = onSwitchCamera,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x99040A18))
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            ) {
                ScoreboardOverlayComposable(state = state)
            }
        }

        // Camera Record / Export Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (recordingStatus) {
                    is RecordingStatus.Idle, is RecordingStatus.Completed -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentWicket)
                                .clickable { onStartRecording() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Record Match", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    is RecordingStatus.Recording -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CharcoalElevated)
                                .clickable { onPauseRecording() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentWicket)
                                .clickable { onStopRecording() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentFour)
                                .clickable { onResumeRecording() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume", color = CharcoalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentWicket)
                                .clickable { onStopRecording() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
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
            }

            if (hasRecordedVideo) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x3300E5FF))
                        .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                        .clickable { onExportVideo() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = GoldBright, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Video", color = GoldBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveScoreboardTab(
    state: InningsLiveState,
    onOpenFullScorecard: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BATTING SUMMARY", color = tokens.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text(
                    text = "Full Scorecard →",
                    color = tokens.accentGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenFullScorecard() }
                )
            }
        }

        items(state.battingCard, key = { it.playerName }) { batter ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(batter.playerName, color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(batter.dismissalText, color = if (batter.isOut) AccentWicket else tokens.accentGold, fontSize = 10.sp)
                    }
                    Text(
                        text = "${batter.runs} (${batter.ballsFaced}) • 4s:${batter.fours} 6s:${batter.sixes}",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("BOWLING SUMMARY", color = tokens.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }

        items(state.bowlingCard, key = { it.bowlerName }) { bowler ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(bowler.bowlerName, color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = "${bowler.formattedOvers}-${bowler.maidens}-${bowler.runsConceded}-${bowler.wickets} (Econ ${bowler.formattedEconomyRate})",
                        color = tokens.accentGoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveCommentaryTab(state: InningsLiveState) {
    val tokens = LiquidGlassTheme.tokens
    val reversed = remember(state.recentDeliveries) {
        state.recentDeliveries.asReversed()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (reversed.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Ball-by-ball commentary will appear here as balls are bowled.", color = tokens.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(reversed, key = { it.id }) { ball ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ball.overNumber}.${ball.legalBallInOver}",
                            color = tokens.accentGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.width(36.dp)
                        )
                        DeliveryChip(delivery = ball)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = ball.commentary.ifEmpty { "${ball.bowlerName} to ${ball.strikerName}, ${ball.totalBallRuns} run(s)" },
                            color = tokens.textPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveMatchInfoTab(
    match: com.example.data.model.MatchEntity?,
    state: InningsLiveState
) {
    val tokens = LiquidGlassTheme.tokens
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("MATCH DETAILS", color = tokens.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Fixture: ${match?.homeTeamName ?: ""} vs ${match?.awayTeamName ?: ""}", color = tokens.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Tournament: ${match?.tournamentName?.ifEmpty { "Club Fixture" } ?: ""}", color = tokens.textSecondary, fontSize = 12.sp)
                    Text("Venue: ${match?.venue ?: "RCB Stadium"}", color = tokens.textSecondary, fontSize = 12.sp)
                    Text("Format: ${match?.format?.displayName ?: "T20"} (${state.totalOversConfigured} Overs)", color = tokens.textSecondary, fontSize = 12.sp)
                    Text("Toss: ${match?.tossWinnerTeam ?: ""} elected to ${match?.tossDecision?.displayName ?: ""}", color = tokens.accentGoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ScoreKeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    bg: Color = CharcoalElevated,
    textColor: Color = Color.White,
    borderColor: Color = Color(0x4400E5FF),
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun BatsmanSelectionDialog(
    state: InningsLiveState,
    allPlayers: List<PlayerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var strikerInput by remember { mutableStateOf(state.currentStriker?.playerName ?: "") }
    var nonStrikerInput by remember { mutableStateOf(state.currentNonStriker?.playerName ?: "") }
    var selectingForStriker by remember { mutableStateOf(true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val dismissedNames = remember(state.battingCard) {
        state.battingCard.filter { it.isOut }.map { it.playerName.lowercase() }.toSet()
    }

    val availablePlayers = remember(allPlayers, dismissedNames) {
        allPlayers.filter { !dismissedNames.contains(it.fullName.lowercase()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = {
            Text("Select / Change Active Batsmen", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tap a slot below, then pick a squad player or type a new player name:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Slot selector tabs (Striker vs Non-Striker)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectingForStriker) Color(0x3300E5FF) else CharcoalElevated)
                            .border(1.5.dp, if (selectingForStriker) GoldPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { selectingForStriker = true }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("STRIKER *", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(strikerInput.ifEmpty { "Select..." }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!selectingForStriker) Color(0x3300E5FF) else CharcoalElevated)
                            .border(1.5.dp, if (!selectingForStriker) GoldPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { selectingForStriker = false }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("NON-STRIKER", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(nonStrikerInput.ifEmpty { "Select..." }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        }
                    }
                }

                // Squad list
                if (availablePlayers.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availablePlayers) { p ->
                            val isUsedInOtherSlot = if (selectingForStriker) {
                                p.fullName.equals(nonStrikerInput.trim(), ignoreCase = true)
                            } else {
                                p.fullName.equals(strikerInput.trim(), ignoreCase = true)
                            }
                            val isSelectedHere = if (selectingForStriker) {
                                p.fullName.equals(strikerInput.trim(), ignoreCase = true)
                            } else {
                                p.fullName.equals(nonStrikerInput.trim(), ignoreCase = true)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelectedHere -> Color(0x4400E5FF)
                                            isUsedInOtherSlot -> Color(0x33102447)
                                            else -> CharcoalElevated
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelectedHere) GoldPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = !isUsedInOtherSlot) {
                                        validationError = null
                                        if (selectingForStriker) {
                                            strikerInput = p.fullName
                                            selectingForStriker = false
                                        } else {
                                            nonStrikerInput = p.fullName
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${p.fullName} (#${p.jerseyNumber})",
                                        color = if (isUsedInOtherSlot) TextSecondary else Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isUsedInOtherSlot) "At other end" else p.role.displayName,
                                        color = GoldLight,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom / Direct Text Fields
                OutlinedTextField(
                    value = strikerInput,
                    onValueChange = {
                        strikerInput = it
                        validationError = null
                    },
                    label = { Text("Striker Name (or type new player)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldPrimary
                    )
                )

                OutlinedTextField(
                    value = nonStrikerInput,
                    onValueChange = {
                        nonStrikerInput = it
                        validationError = null
                    },
                    label = { Text("Non-Striker Name (or type new player)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldPrimary
                    )
                )

                if (validationError != null) {
                    Text(validationError!!, color = AccentWicket, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            GoldButton(
                text = "Save Batsmen",
                onClick = {
                    val s = strikerInput.trim()
                    val ns = nonStrikerInput.trim()
                    when {
                        s.isEmpty() || ns.isEmpty() -> {
                            validationError = "Please enter both Striker and Non-Striker names."
                        }
                        s.equals(ns, ignoreCase = true) -> {
                            validationError = "Striker and Non-Striker cannot be the same player."
                        }
                        dismissedNames.contains(s.lowercase()) || dismissedNames.contains(ns.lowercase()) -> {
                            validationError = "One of the selected players is already out in this innings."
                        }
                        else -> onConfirm(s, ns)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WicketEntryDialog(
    state: InningsLiveState,
    allPlayers: List<PlayerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (WicketType, String, String?, String) -> Unit
) {
    val striker = state.currentStriker?.playerName ?: "Striker"
    val nonStriker = state.currentNonStriker?.playerName ?: "Non-striker"

    var wicketType by remember { mutableStateOf(WicketType.BOWLED) }
    var dismissedPlayer by remember { mutableStateOf(striker) }
    var fielderName by remember { mutableStateOf("") }
    var newBatterName by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }

    val remainingBatter = if (dismissedPlayer == striker) nonStriker else striker
    val dismissedSet = remember(state.battingCard, dismissedPlayer, remainingBatter) {
        state.battingCard.filter { it.isOut }.map { it.playerName.lowercase() }.toSet() +
            setOf(dismissedPlayer.lowercase(), remainingBatter.lowercase())
    }

    val eligibleNextBatters = remember(allPlayers, dismissedSet) {
        allPlayers.filter { !dismissedSet.contains(it.fullName.lowercase()) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = { Text("Record Wicket & Select Next Batter", color = AccentWicket, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                Text("Who was dismissed?", color = TextSecondary, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dismissedPlayer == striker) AccentWicket else CharcoalElevated)
                            .clickable { dismissedPlayer = striker }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(striker, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dismissedPlayer == nonStriker) AccentWicket else CharcoalElevated)
                            .clickable { dismissedPlayer = nonStriker }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(nonStriker, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (wicketType == WicketType.CAUGHT || wicketType == WicketType.RUN_OUT || wicketType == WicketType.STUMPED) {
                    OutlinedTextField(
                        value = fielderName,
                        onValueChange = { fielderName = it },
                        label = { Text("Fielder / Wicketkeeper Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                // Squad Picker for Next Incoming Batter
                if (eligibleNextBatters.isNotEmpty()) {
                    Text("Tap Next Incoming Batter from Squad:", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(eligibleNextBatters) { p ->
                            val isSelected = newBatterName.trim().equals(p.fullName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0x4400E5FF) else CharcoalElevated)
                                    .border(1.dp, if (isSelected) GoldPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        newBatterName = p.fullName
                                        validationError = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "${p.fullName} (${p.role.displayName})",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = newBatterName,
                    onValueChange = {
                        newBatterName = it
                        validationError = null
                    },
                    label = { Text("Next Incoming Batter Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldPrimary
                    )
                )

                if (validationError != null) {
                    Text(validationError!!, color = AccentWicket, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            GoldButton(
                text = "Confirm Out",
                onClick = {
                    val incoming = newBatterName.trim().ifEmpty {
                        eligibleNextBatters.firstOrNull()?.fullName ?: "Batter ${state.totalWickets + 3}"
                    }
                    if (incoming.equals(remainingBatter, ignoreCase = true)) {
                        validationError = "$incoming is already batting at the other end."
                    } else {
                        onConfirm(wicketType, dismissedPlayer, fielderName.ifBlank { null }, incoming)
                    }
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
    var extraRuns by remember { mutableIntStateOf(1) }
    var runsOffBat by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = { Text("Record Extras", color = GoldPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            Text(
                                text = type.displayName,
                                color = if (isSel) CharcoalBlack else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
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
    var validationError by remember { mutableStateOf<String?>(null) }

    val availableBowlers = remember(allPlayers, currentBowler) {
        allPlayers.filter { !it.fullName.equals(currentBowler?.trim(), ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = { Text("Select / Add Bowler", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (!currentBowler.isNullOrBlank()) {
                        "Tap a squad bowler (excluding previous bowler: $currentBowler) or enter a new name:"
                    } else {
                        "Tap a bowler from your squad or type a new bowler name:"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                if (availableBowlers.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableBowlers) { p ->
                            val isSelected = customBowlerName.trim().equals(p.fullName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0x4400E5FF) else CharcoalElevated)
                                    .border(1.dp, if (isSelected) GoldPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSelect(p.fullName)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${p.fullName} (#${p.jerseyNumber})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = p.bowlingStyle.displayName,
                                        color = GoldLight,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = customBowlerName,
                    onValueChange = {
                        customBowlerName = it
                        validationError = null
                    },
                    label = { Text("Or Type New Bowler Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldPrimary
                    )
                )

                if (validationError != null) {
                    Text(validationError!!, color = AccentWicket, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            GoldButton(
                text = "Set Bowler",
                onClick = {
                    val chosen = customBowlerName.trim().ifEmpty {
                        availableBowlers.firstOrNull()?.fullName.orEmpty()
                    }
                    if (chosen.isEmpty()) {
                        validationError = "Please select or enter a bowler name."
                    } else if (chosen.equals(currentBowler?.trim(), ignoreCase = true)) {
                        validationError = "$chosen just bowled the previous over. Pick another bowler."
                    } else {
                        onSelect(chosen)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextSecondary) }
        }
    )
}
