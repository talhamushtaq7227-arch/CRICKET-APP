package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MatchEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.OutlinedGoldButton
import com.example.ui.theme.AccentExtra
import com.example.ui.theme.AccentFour
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CricketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: CricketViewModel,
    onNavigateToMatchSetup: () -> Unit,
    onNavigateToLiveScoring: (Long) -> Unit,
    onNavigateToScorecard: (Long) -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToRankings: () -> Unit
) {
    val teamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val allMatches by viewModel.allMatches.collectAsStateWithLifecycle()
    val unfinishedMatch by viewModel.latestUnfinishedMatch.collectAsStateWithLifecycle()
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val rankings by viewModel.rankings.collectAsStateWithLifecycle()

    val topBatter = rankings.maxByOrNull { it.runs }
    val topBowler = rankings.maxByOrNull { it.wickets }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Brand Banner
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xDD12121A),
                borderColor = Color(0x66D4AF37),
                borderWidth = 1.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(GoldBright, GoldDark))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsCricket,
                                        contentDescription = "Cricket Emblem",
                                        tint = CharcoalBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "GLASS EDITION",
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = teamName,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Professional scoring engine, real camera video recording and permanent broadcast scoreboard overlay.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoldButton(
                            text = "New Match",
                            onClick = onNavigateToMatchSetup,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, tint = CharcoalBlack)
                            }
                        )
                        OutlinedGoldButton(
                            text = "Squad (${players.size})",
                            onClick = onNavigateToSquad,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Continue Unfinished Match Banner
        if (unfinishedMatch != null) {
            val match = unfinishedMatch!!
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.loadMatch(match.id)
                            onNavigateToLiveScoring(match.id)
                        },
                    backgroundColor = Color(0xEE1E1A10),
                    borderColor = GoldBright,
                    borderWidth = 1.5.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentExtra)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MATCH IN PROGRESS",
                                    color = AccentExtra,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${match.homeTeamName} vs ${match.awayTeamName}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Innings ${match.currentInnings} • ${match.format.displayName}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        GoldButton(
                            text = "Resume",
                            onClick = {
                                viewModel.loadMatch(match.id)
                                onNavigateToLiveScoring(match.id)
                            },
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }
            }
        }

        // Stats & Top Performers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Batter
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xAA161622)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Top Batter", color = TextGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = topBatter?.let { if (it.runs > 0) it.playerName else "No stats yet" } ?: "No stats yet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = topBatter?.let { if (it.runs > 0) "${it.runs} runs (HS: ${it.highestScore})" else "Start a match" } ?: "Seed initial roster",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Top Bowler
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xAA161622)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Top Bowler", color = TextGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = topBowler?.let { if (it.wickets > 0) it.playerName else "No stats yet" } ?: "No stats yet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = topBowler?.let { if (it.wickets > 0) "${it.wickets} wkts (${it.bestBowlingFigures})" else "Start bowling" } ?: "Real figures",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section Title: Recent Matches
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Matches",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "${allMatches.size} Total",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (allMatches.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x66161620)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsCricket,
                            contentDescription = null,
                            tint = GoldMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Matches Recorded Yet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set up your first match, record live video footage with your phone camera, and score ball-by-ball.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GoldButton(
                            text = "Create Match Now",
                            onClick = onNavigateToMatchSetup,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
            }
        } else {
            items(allMatches.take(5)) { match ->
                MatchCardItem(
                    match = match,
                    onClick = {
                        if (match.isCompleted) {
                            onNavigateToScorecard(match.id)
                        } else {
                            viewModel.loadMatch(match.id)
                            onNavigateToLiveScoring(match.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MatchCardItem(
    match: MatchEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(match.matchDateMillis))

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = Color(0xCC161620),
        borderColor = if (match.isCompleted) Color(0x33D4AF37) else GoldBright
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.tournamentName.ifEmpty { match.matchTitle },
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = match.homeTeamName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "vs ${match.awayTeamName}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (match.isCompleted) Color(0x3310B981) else Color(0x33F59E0B))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (match.isCompleted) "Completed" else "Live",
                        color = if (match.isCompleted) AccentFour else AccentExtra,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!match.winnerTeam.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = match.winnerTeam,
                    color = GoldBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
