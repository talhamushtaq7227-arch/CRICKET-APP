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
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.domain.engine.PlayerDetailedStatistics
import com.example.domain.engine.PlayerStatisticsCalculator
import com.example.domain.engine.SquadRankingItem
import com.example.ui.components.GlassCard
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

import com.example.ui.components.InteractiveGlassCard
import com.example.ui.theme.LiquidGlassTheme

@Composable
fun RankingsScreen(
    viewModel: CricketViewModel,
    onNavigateToProfile: (Long) -> Unit = {},
    onNavigateToComparison: (Long) -> Unit = {}
) {
    val tokens = LiquidGlassTheme.tokens
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val deliveries by viewModel.allDeliveries.collectAsStateWithLifecycle()
    val seasons by viewModel.distinctSeasons.collectAsStateWithLifecycle()
    val tournaments by viewModel.distinctTournaments.collectAsStateWithLifecycle()

    var selectedSeasonFilter by remember { mutableStateOf<String?>(null) }
    var selectedTournamentFilter by remember { mutableStateOf<String?>(null) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val categories = listOf(
        "Most Runs",
        "Most Wickets",
        "Highest Score",
        "Batting Average",
        "Strike Rate",
        "Best Bowling",
        "Economy Rate",
        "Most 4s",
        "Most 6s"
    )

    // Compute detailed stats for every player under current period filter
    val allDetailedStats = remember(players, matches, deliveries, selectedSeasonFilter, selectedTournamentFilter) {
        players.map { p ->
            PlayerStatisticsCalculator.calculatePlayerStatistics(
                player = p,
                matches = matches,
                deliveries = deliveries,
                seasonFilter = selectedSeasonFilter,
                tournamentFilter = selectedTournamentFilter
            )
        }
    }

    val rankingItems = remember(selectedCategoryIndex, allDetailedStats) {
        PlayerStatisticsCalculator.computeSquadRankings(selectedCategoryIndex, allDetailedStats)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Club Leaderboard & Rankings",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Real-time squad rankings from official scorecard deliveries",
                        color = tokens.textSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.glassCardElevated)
                        .border(1.dp, tokens.accentGold, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToComparison(0L) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Compare",
                            tint = tokens.accentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Compare",
                            color = tokens.accentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Period filter bar
        PeriodFilterBar(
            availableSeasons = seasons,
            availableTournaments = tournaments,
            selectedSeason = selectedSeasonFilter,
            selectedTournament = selectedTournamentFilter,
            onSelectSeason = { selectedSeasonFilter = it },
            onSelectTournament = { selectedTournamentFilter = it }
        )

        // Categories Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = tokens.glassCardElevated,
            contentColor = tokens.accentGold,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                    color = tokens.accentGold,
                    height = 3.dp
                )
            }
        ) {
            categories.forEachIndexed { index, name ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Text(
                            text = name,
                            fontSize = 13.sp,
                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedCategoryIndex == index) tokens.accentGold else tokens.textSecondary
                        )
                    }
                )
            }
        }

        // Qualification / Period Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.glassCardElevated)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val qualificationText = when (selectedCategoryIndex) {
                3 -> "Qualification: Minimum 1 dismissal required • Denominator zero handled as N/A"
                4 -> "Qualification: Minimum 10 balls faced required"
                6 -> "Qualification: Minimum 6 legal balls (1 over) bowled required"
                else -> "Rankings derived directly from saved ball-by-ball scorecards"
            }
            Text(
                text = qualificationText,
                color = tokens.textSecondary,
                fontSize = 11.sp
            )
        }

        // Ranking List
        if (rankingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = tokens.accentGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Qualifying Records Found",
                        color = tokens.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Complete matches with official deliveries to populate the squad leaderboard",
                        color = tokens.textSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rankingItems) { item ->
                    RankingRowCard(
                        item = item,
                        onClick = { onNavigateToProfile(item.player.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RankingRowCard(
    item: SquadRankingItem,
    onClick: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val isTop3 = item.rank <= 3
    val rankBadgeColor = when (item.rank) {
        1 -> tokens.accentGold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> tokens.glassCardElevated
    }

    val rankTextColor = if (isTop3) tokens.buttonContent else tokens.textSecondary

    InteractiveGlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (item.rank == 1) tokens.glassCardElevated else tokens.glassCardBackground,
        borderColor = if (item.rank == 1) tokens.accentGold else tokens.glassBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(rankBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                if (item.rank == 1) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Leader",
                        tint = tokens.buttonContent,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "${item.rank}",
                        color = rankTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Avatar / Photo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tokens.glassCardElevated)
                    .border(1.dp, tokens.accentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!item.player.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = item.player.photoUri,
                        contentDescription = item.player.fullName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (item.player.jerseyNumber > 0) {
                    Text(
                        text = "#${item.player.jerseyNumber}",
                        color = tokens.accentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = tokens.accentGold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name & Role
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.player.fullName,
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (item.player.isCaptain) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(C)",
                            color = tokens.accentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = item.secondaryValue,
                    color = tokens.textSecondary,
                    fontSize = 11.sp
                )
            }

            // Primary Stat Value
            Text(
                text = item.primaryValue,
                color = if (isTop3) tokens.accentGold else tokens.textPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}
