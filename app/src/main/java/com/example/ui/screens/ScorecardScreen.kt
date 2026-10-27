package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.engine.CricketRulesEngine
import com.example.ui.components.DeliveryChip
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.OutlinedGoldButton
import com.example.ui.components.SegmentedFormatSelector
import com.example.ui.components.SkyCyanLiveScoreBanner
import com.example.ui.components.liquid3DEntrance
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardScreen(
    matchId: Long,
    viewModel: CricketViewModel,
    onBack: () -> Unit,
    onResumeScoring: (Long) -> Unit,
    onExportVideo: (Long) -> Unit
) {
    val context = LocalContext.current
    val tokens = LiquidGlassTheme.tokens

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.currentMatch.collectAsStateWithLifecycle()
    val deliveries by viewModel.currentMatchDeliveries.collectAsStateWithLifecycle()

    var selectedInningsTab by remember { mutableStateOf(1) }

    val innings1State = remember(match, deliveries) {
        match?.let { m ->
            CricketRulesEngine.calculateInningsState(m, 1, deliveries)
        }
    }

    val innings2State = remember(match, deliveries) {
        match?.let { m ->
            CricketRulesEngine.calculateInningsState(m, 2, deliveries)
        }
    }

    val currentInningsState = if (selectedInningsTab == 1) innings1State else innings2State
    val reversedInningsDeliveries = remember(deliveries, selectedInningsTab) {
        deliveries.filter { it.inningsNumber == selectedInningsTab }.asReversed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Scorecard", color = tokens.textPrimary, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tokens.accentGold)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = buildString {
                            appendLine("${match?.homeTeamName} vs ${match?.awayTeamName}")
                            appendLine("Result: ${match?.winnerTeam ?: "In progress"}")
                            if (innings1State != null) {
                                appendLine("1st Innings: ${innings1State.battingTeam} ${innings1State.totalRuns}/${innings1State.totalWickets} (${innings1State.formattedOvers} ov)")
                            }
                            if (innings2State != null && innings2State.legalBalls > 0) {
                                appendLine("2nd Innings: ${innings2State.battingTeam} ${innings2State.totalRuns}/${innings2State.totalWickets} (${innings2State.formattedOvers} ov)")
                            }
                            appendLine("Scored on Rajpoot Cricket Boys")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Match Scorecard"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = tokens.accentGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (match == null || currentInningsState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading scorecard...", color = tokens.textSecondary)
            }
        } else {
            val m = match!!
            val leftTeam = innings1State?.battingTeam ?: m.homeTeamName
            val rightTeam = innings2State?.battingTeam ?: m.awayTeamName
            val leftScore = innings1State?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "0/0"
            val leftOvers = innings1State?.let { "(${it.formattedOvers} ov)" } ?: "(0.0 ov)"
            val rightScore = if (innings2State != null && (innings2State.legalBalls > 0 || m.currentInnings == 2)) {
                "${innings2State.totalRuns}/${innings2State.totalWickets}"
            } else {
                "Yet to bat"
            }
            val rightOvers = if (innings2State != null && (innings2State.legalBalls > 0 || m.currentInnings == 2)) {
                "(${innings2State.formattedOvers} ov)"
            } else {
                "(${m.totalOvers} ov)"
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sky-Cyan Score Banner
                item {
                    SkyCyanLiveScoreBanner(
                        matchTitle = "${m.homeTeamName} vs ${m.awayTeamName}",
                        matchSubtitle = "${m.format.displayName} • ${m.venue}",
                        leftTeamName = leftTeam,
                        leftScore = leftScore,
                        leftOvers = leftOvers,
                        rightTeamName = rightTeam,
                        rightScore = rightScore,
                        rightOvers = rightOvers,
                        statusText = m.winnerTeam?.ifBlank { null } ?: currentInningsState.resultSummary ?: "CRR: ${currentInningsState.formattedCRR}",
                        isLive = !m.isCompleted,
                        modifier = Modifier.liquid3DEntrance()
                    )
                }

                // Actions row if match in progress or video exists
                if (!m.isCompleted || m.sourceVideoPath != null) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!m.isCompleted) {
                                GoldButton(
                                    text = "Resume Scoring",
                                    onClick = { onResumeScoring(matchId) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (m.sourceVideoPath != null) {
                                OutlinedGoldButton(
                                    text = "Export Video",
                                    onClick = { onExportVideo(matchId) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Innings Selector Tabs
                item {
                    SegmentedFormatSelector(
                        options = listOf(
                            "1st: ${innings1State?.battingTeam ?: m.homeTeamName}",
                            "2nd: ${innings2State?.battingTeam ?: m.awayTeamName}"
                        ),
                        selectedIndex = selectedInningsTab - 1,
                        onSelect = { selectedInningsTab = it + 1 }
                    )
                }

                // BATTING SCORECARD
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = tokens.glassCardBackground,
                        borderColor = tokens.glassCardBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Batter", color = tokens.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(2.5f))
                                Text("R", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("B", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("4s", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("6s", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("SR", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            }
                            Divider(color = tokens.glassCardBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            currentInningsState.battingCard.forEach { b ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(2.5f)) {
                                        Text(b.playerName, color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            b.dismissalText,
                                            color = if (b.isOut) tokens.textMuted else tokens.accentGold,
                                            fontSize = 10.sp,
                                            fontWeight = if (b.isOut) FontWeight.Normal else FontWeight.SemiBold
                                        )
                                    }
                                    Text("${b.runs}", color = tokens.textPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                                    Text("${b.ballsFaced}", color = tokens.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${b.fours}", color = tokens.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${b.sixes}", color = tokens.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text(b.formattedStrikeRate, color = tokens.accentGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                                }
                            }

                            Divider(color = tokens.glassCardBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Extras", color = tokens.textSecondary, fontSize = 12.sp)
                                Text(currentInningsState.extras.summaryText, color = tokens.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // BOWLING SCORECARD
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = tokens.glassCardBackground,
                        borderColor = tokens.glassCardBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Bowler", color = tokens.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(2.5f))
                                Text("O", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("M", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("R", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("W", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("Econ", color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            }
                            Divider(color = tokens.glassCardBorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            currentInningsState.bowlingCard.forEach { bw ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(bw.bowlerName, color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2.5f))
                                    Text(bw.formattedOvers, color = tokens.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${bw.maidens}", color = tokens.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${bw.runsConceded}", color = tokens.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${bw.wickets}", color = tokens.accentGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                                    Text(bw.formattedEconomyRate, color = tokens.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                                }
                            }
                        }
                    }
                }

                // FALL OF WICKETS
                if (currentInningsState.fallOfWickets.isNotEmpty()) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = tokens.glassCardBackground,
                            borderColor = tokens.glassCardBorder
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Fall of Wickets", color = tokens.accentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                currentInningsState.fallOfWickets.forEach { fow ->
                                    Text(
                                        text = "${fow.wicketNumber}-${fow.score} (${fow.batsmanName}, ${fow.overBall} ov)",
                                        color = tokens.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // BALL BY BALL COMMENTARY
                if (reversedInningsDeliveries.isNotEmpty()) {
                    item {
                        Text("Commentary", color = tokens.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    items(reversedInningsDeliveries, key = { it.id }) { ball ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = tokens.glassCardBackground,
                            borderColor = tokens.glassCardBorderSubtle
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${ball.overNumber}.${if (ball.isLegalDelivery) ball.legalBallInOver else "-"}",
                                    color = tokens.accentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(38.dp)
                                )
                                DeliveryChip(delivery = ball)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = ball.commentary.ifEmpty { "${ball.bowlerName} to ${ball.strikerName}, ${ball.totalBallRuns} runs." },
                                    color = tokens.textPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
