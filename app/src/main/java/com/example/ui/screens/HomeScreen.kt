package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.SportsCricket
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
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.components.LiquidGlassThemeToggle
import com.example.ui.components.OutlinedGoldButton
import com.example.ui.theme.AccentExtra
import com.example.ui.theme.AccentFour
import com.example.ui.components.CricketAnimationType
import com.example.ui.components.CricketBallSeamSpinner
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.LiquidGlassTheme
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
    val tokens = LiquidGlassTheme.tokens
    val themeMode by viewModel.glassThemeSetting.collectAsStateWithLifecycle()
    val teamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val allMatches by viewModel.allMatches.collectAsStateWithLifecycle()
    val unfinishedMatch by viewModel.latestUnfinishedMatch.collectAsStateWithLifecycle()
    val rankings by viewModel.rankings.collectAsStateWithLifecycle()

    val topBatter = rankings.maxByOrNull { it.runs }
    val topBowler = rankings.maxByOrNull { it.wickets }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Brand Banner with iOS Liquid Glass Theme Toggle
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = tokens.glassCardElevated,
                borderColor = tokens.accentGold,
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CricketBallSeamSpinner(sizeDp = 34.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (tokens.isDark) "DARK LIQUID GLASS" else "WHITE LIQUID GLASS",
                                    color = tokens.accentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = teamName,
                                color = tokens.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // iOS Liquid Glass Tactile Theme Switcher
                        LiquidGlassThemeToggle(
                            currentMode = themeMode,
                            onToggle = { newMode -> viewModel.setGlassTheme(newMode) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (tokens.isDark) CharcoalBlack else Color.White
                                )
                            }
                        )
                        OutlinedGoldButton(
                            text = "Squad (${players.size})",
                            onClick = onNavigateToSquad,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cricket Interactive Animation FX Bar
                    Text(
                        text = "CRICKET FX & CELEBRATIONS",
                        color = tokens.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CricketFxPill(
                            label = "FOUR 4",
                            color = AccentFour,
                            onClick = { viewModel.triggerCricketAnimation(CricketAnimationType.Four) },
                            modifier = Modifier.weight(1f)
                        )
                        CricketFxPill(
                            label = "SIX 6",
                            color = AccentSix,
                            onClick = { viewModel.triggerCricketAnimation(CricketAnimationType.Six) },
                            modifier = Modifier.weight(1f)
                        )
                        CricketFxPill(
                            label = "WICKET!",
                            color = AccentWicket,
                            onClick = { viewModel.triggerCricketAnimation(CricketAnimationType.Wicket("BOWLED! STUMPS SHATTERED")) },
                            modifier = Modifier.weight(1.1f)
                        )
                        CricketFxPill(
                            label = "50 RUNS",
                            color = tokens.accentGold,
                            onClick = { viewModel.triggerCricketAnimation(CricketAnimationType.Milestone("50! HALF CENTURY")) },
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                }
            }
        }

        // Continue Unfinished Match Banner
        if (unfinishedMatch != null) {
            val match = unfinishedMatch!!
            item {
                InteractiveGlassCard(
                    onClick = {
                        viewModel.loadMatch(match.id)
                        onNavigateToLiveScoring(match.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = tokens.accentGoldBright
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
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Innings ${match.currentInnings} • ${match.format.displayName}",
                                color = tokens.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        GoldButton(
                            text = "Resume",
                            onClick = {
                                viewModel.loadMatch(match.id)
                                onNavigateToLiveScoring(match.id)
                            },
                            modifier = Modifier.width(115.dp)
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
                InteractiveGlassCard(
                    onClick = onNavigateToRankings,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = tokens.accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Top Batter",
                                color = tokens.textGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = topBatter?.let { if (it.runs > 0) it.playerName else "No stats yet" } ?: "No stats yet",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = topBatter?.let { if (it.runs > 0) "${it.runs} runs (HS: ${it.highestScore})" else "Start a match" } ?: "Seed initial roster",
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Top Bowler
                InteractiveGlassCard(
                    onClick = onNavigateToRankings,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = tokens.accentGoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Top Bowler",
                                color = tokens.textGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = topBowler?.let { if (it.wickets > 0) it.playerName else "No stats yet" } ?: "No stats yet",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = topBowler?.let { if (it.wickets > 0) "${it.wickets} wkts (${it.bestBowlingFigures})" else "Start bowling" } ?: "Real figures",
                            color = tokens.textSecondary,
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
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "${allMatches.size} Total",
                    color = tokens.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (allMatches.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
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
                            tint = tokens.accentGold,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Matches Recorded Yet",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set up your first match, record live video footage with your phone camera, and score ball-by-ball.",
                            color = tokens.textSecondary,
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
    val tokens = LiquidGlassTheme.tokens
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(match.matchDateMillis))

    InteractiveGlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (match.isCompleted) tokens.glassCardBorder else tokens.accentGoldBright
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.tournamentName.ifEmpty { match.matchTitle },
                    color = tokens.accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateStr,
                    color = tokens.textMuted,
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
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "vs ${match.awayTeamName}",
                        color = tokens.textSecondary,
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
                    color = tokens.accentGoldBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CricketFxPill(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp
        )
    }
}

