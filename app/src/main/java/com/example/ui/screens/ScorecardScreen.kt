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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Scorecard", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldLight)
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
                            appendLine("Scored on Rajpoot Cricket Boys — Glass Edition")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Match Scorecard"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = GoldLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        if (match == null || currentInningsState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading scorecard...", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Match Header Card
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xDD12121A),
                        borderColor = Color(0x66D4AF37)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = match!!.tournamentName.ifEmpty { match!!.matchTitle },
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${match!!.homeTeamName} vs ${match!!.awayTeamName}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${match!!.venue} • ${match!!.format.displayName}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            if (!match!!.winnerTeam.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33D4AF37))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = match!!.winnerTeam!!,
                                        color = GoldBright,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Actions row
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (!match!!.isCompleted) {
                                    GoldButton(
                                        text = "Resume Scoring",
                                        onClick = { onResumeScoring(matchId) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (match!!.sourceVideoPath != null) {
                                    OutlinedGoldButton(
                                        text = "Export Video",
                                        onClick = { onExportVideo(matchId) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Innings Selector Tabs
                item {
                    TabRow(
                        selectedTabIndex = selectedInningsTab - 1,
                        containerColor = CharcoalDark,
                        contentColor = GoldPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedInningsTab - 1]),
                                color = GoldPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedInningsTab == 1,
                            onClick = { selectedInningsTab = 1 },
                            text = {
                                Text(
                                    text = "1st Innings: ${innings1State?.battingTeam ?: ""}",
                                    fontWeight = if (selectedInningsTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedInningsTab == 2,
                            onClick = { selectedInningsTab = 2 },
                            text = {
                                Text(
                                    text = "2nd Innings: ${innings2State?.battingTeam ?: ""}",
                                    fontWeight = if (selectedInningsTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Score Overview
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(currentInningsState.battingTeam.uppercase(), color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${currentInningsState.totalRuns}/${currentInningsState.totalWickets}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 28.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("OVERS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${currentInningsState.formattedOvers} / ${currentInningsState.totalOversConfigured}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text("CRR: ${currentInningsState.formattedCRR}", color = GoldBright, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // BATTING SCORECARD
                item {
                    Text("BATTING", color = GoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Batter", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.5f))
                                Text("R", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("B", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("4s", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("6s", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("SR", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            }
                            Divider(color = Color(0x22D4AF37), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            currentInningsState.battingCard.forEach { b ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(2.5f)) {
                                        Text(b.playerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(b.dismissalText, color = TextMuted, fontSize = 10.sp)
                                    }
                                    Text("${b.runs}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                                    Text("${b.ballsFaced}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${b.fours}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${b.sixes}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text(b.formattedStrikeRate, color = GoldLight, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                                }
                            }

                            Divider(color = Color(0x22D4AF37), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                            // Extras summary
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Extras", color = TextSecondary, fontSize = 12.sp)
                                Text(currentInningsState.extras.summaryText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // BOWLING SCORECARD
                item {
                    Text("BOWLING", color = GoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Bowler", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.5f))
                                Text("O", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("M", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("R", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("W", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("Econ", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            }
                            Divider(color = Color(0x22D4AF37), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                            currentInningsState.bowlingCard.forEach { bw ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(bw.bowlerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2.5f))
                                    Text(bw.formattedOvers, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${bw.maidens}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${bw.runsConceded}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                    Text("${bw.wickets}", color = GoldBright, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                                    Text(bw.formattedEconomyRate, color = GoldLight, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                                }
                            }
                        }
                    }
                }

                // FALL OF WICKETS
                if (currentInningsState.fallOfWickets.isNotEmpty()) {
                    item {
                        Text("FALL OF WICKETS", color = GoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                currentInningsState.fallOfWickets.forEach { fow ->
                                    Text(
                                        text = "${fow.wicketNumber}-${fow.score} (${fow.batsmanName}, ${fow.overBall} ov)",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // BALL BY BALL COMMENTARY
                val inningsDeliveries = deliveries.filter { it.inningsNumber == selectedInningsTab }
                if (inningsDeliveries.isNotEmpty()) {
                    item {
                        Text("BALL BY BALL COMMENTARY", color = GoldBright, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    items(inningsDeliveries.reversed()) { ball ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${ball.overNumber}.${if (ball.isLegalDelivery) ball.legalBallInOver else "-"}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.width(36.dp)
                            )
                            DeliveryChip(delivery = ball)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = ball.commentary.ifEmpty { "${ball.bowlerName} to ${ball.strikerName}, ${ball.totalBallRuns} runs." },
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
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
