package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.engine.PlayerStats
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.CharcoalBlack
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

@Composable
fun RankingsScreen(
    viewModel: CricketViewModel
) {
    val rankings by viewModel.rankings.collectAsStateWithLifecycle()
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val categories = listOf(
        "Top Runs",
        "Top Wickets",
        "Highest Score",
        "Strike Rate",
        "Economy",
        "Boundaries"
    )

    val hasActiveStats = rankings.any { it.runs > 0 || it.wickets > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBlack)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Club Leaderboard & Rankings",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Calculated in real-time from official scorecard deliveries",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = CharcoalDark,
            contentColor = GoldPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                    color = GoldPrimary,
                    height = 3.dp
                )
            }
        ) {
            categories.forEachIndexed { index, title ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategoryIndex == index) GoldBright else TextSecondary
                        )
                    }
                )
            }
        }

        if (!hasActiveStats) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x66161622)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldLight, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Official Stats Yet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Leaderboard rankings are calculated strictly from real match deliveries. Play and record your first match to generate player leaderboards.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Sort list according to selected category
            val sortedList = when (selectedCategoryIndex) {
                0 -> rankings.filter { it.runs > 0 }.sortedByDescending { it.runs }
                1 -> rankings.filter { it.wickets > 0 }.sortedByDescending { it.wickets }
                2 -> rankings.filter { it.highestScore > 0 }.sortedByDescending { it.highestScore }
                3 -> rankings.filter { it.ballsFaced >= 10 }.sortedByDescending { it.strikeRate }
                4 -> rankings.filter { it.legalBallsBowled >= 12 }.sortedBy { it.bowlingEconomy ?: 999.0 }
                5 -> rankings.filter { (it.fours + it.sixes) > 0 }.sortedByDescending { (it.fours * 4) + (it.sixes * 6) }
                else -> rankings
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val criteriaNote = when (selectedCategoryIndex) {
                        3 -> "Qualifying threshold: Minimum 10 balls faced."
                        4 -> "Qualifying threshold: Minimum 2 overs (12 legal balls) bowled."
                        5 -> "Ranked by boundary runs (Fours x4 + Sixes x6)."
                        else -> "Ranked by total recorded statistics across all matches."
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(criteriaNote, color = TextMuted, fontSize = 11.sp)
                    }
                }

                itemsIndexed(sortedList) { index, playerStats ->
                    LeaderboardItemCard(
                        rank = index + 1,
                        stats = playerStats,
                        categoryIndex = selectedCategoryIndex
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardItemCard(
    rank: Int,
    stats: PlayerStats,
    categoryIndex: Int
) {
    val rankColor = when (rank) {
        1 -> GoldPrimary
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> TextMuted
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xCC161622),
        borderColor = if (rank == 1) GoldBright else Color(0x22D4AF37)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (rank <= 3) rankColor else CharcoalElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = if (rank <= 3) CharcoalBlack else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.playerName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${stats.matchesPlayed} matches • ${stats.inningsBatted} innings",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Stat Value
            Column(horizontalAlignment = Alignment.End) {
                val (primaryText, subText) = when (categoryIndex) {
                    0 -> Pair("${stats.runs} runs", "Avg: ${stats.formattedBattingAverage} | SR: ${stats.formattedStrikeRate}")
                    1 -> Pair("${stats.wickets} wickets", "Econ: ${stats.formattedBowlingEconomy} | Best: ${stats.bestBowlingFigures}")
                    2 -> Pair("${stats.highestScore}* HS", "${stats.runs} total runs")
                    3 -> Pair("${stats.formattedStrikeRate} SR", "${stats.runs} runs off ${stats.ballsFaced} balls")
                    4 -> Pair("${stats.formattedBowlingEconomy} Econ", "${stats.runsConceded} runs in ${stats.formattedOversBowled} ov")
                    5 -> Pair("${stats.fours} x 4s | ${stats.sixes} x 6s", "${(stats.fours * 4) + (stats.sixes * 6)} boundary runs")
                    else -> Pair("${stats.runs} runs", "${stats.wickets} wkts")
                }

                Text(
                    text = primaryText,
                    color = GoldBright,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Text(
                    text = subText,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
