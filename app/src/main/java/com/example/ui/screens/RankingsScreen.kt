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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.MatchEntity
import com.example.data.model.MatchFormat
import com.example.data.model.PlayerRole
import com.example.domain.engine.PlayerDetailedStatistics
import com.example.domain.engine.PlayerStatisticsCalculator
import com.example.domain.engine.SquadRankingItem
import com.example.ui.components.CyanPillTabs
import com.example.ui.components.GlassCard
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.components.SegmentedFormatSelector
import com.example.ui.components.TeamShieldBadge
import com.example.ui.components.liquid3DEntrance
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TeamRankingEntry(
    val rank: Int,
    val teamName: String,
    val matchesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val points: Int
)

@Composable
fun RankingsScreen(
    viewModel: CricketViewModel,
    onNavigateToProfile: (Long) -> Unit = {},
    onNavigateToComparison: (Long) -> Unit = {}
) {
    val tokens = LiquidGlassTheme.tokens
    val teamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val deliveries by viewModel.allDeliveries.collectAsStateWithLifecycle()
    val seasons by viewModel.distinctSeasons.collectAsStateWithLifecycle()
    val tournaments by viewModel.distinctTournaments.collectAsStateWithLifecycle()

    // Format segment selector (0 = All, 1 = T20, 2 = T10 / ODI)
    var selectedFormatIndex by remember { mutableIntStateOf(0) }
    // Primary role/view pill (0 = Batter, 1 = Bowler, 2 = All Rounder, 3 = Team)
    var selectedRolePill by remember { mutableIntStateOf(0) }
    // Detailed stat metric index (0..8)
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var showPeriodFilters by remember { mutableStateOf(false) }

    var selectedSeasonFilter by remember { mutableStateOf<String?>(null) }
    var selectedTournamentFilter by remember { mutableStateOf<String?>(null) }

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

    // Filter matches by format segment
    val formatFilteredMatches = remember(matches, selectedFormatIndex) {
        when (selectedFormatIndex) {
            1 -> matches.filter { it.format == MatchFormat.T20 }
            2 -> matches.filter { it.format != MatchFormat.T20 }
            else -> matches
        }
    }

    // Compute detailed stats off the main thread under current period & format filter
    val allDetailedStats by produceState(
        initialValue = emptyList<PlayerDetailedStatistics>(),
        players,
        formatFilteredMatches,
        deliveries,
        selectedSeasonFilter,
        selectedTournamentFilter
    ) {
        value = withContext(Dispatchers.Default) {
            players.map { p ->
                PlayerStatisticsCalculator.calculatePlayerStatistics(
                    player = p,
                    matches = formatFilteredMatches,
                    deliveries = deliveries,
                    seasonFilter = selectedSeasonFilter,
                    tournamentFilter = selectedTournamentFilter
                )
            }
        }
    }

    // Compute Player Rankings filtered/sorted by role pill and metric
    val rankingItems = remember(selectedRolePill, selectedCategoryIndex, allDetailedStats) {
        val effectiveCategory = when (selectedRolePill) {
            0 -> if (selectedCategoryIndex in listOf(1, 5, 6)) 0 else selectedCategoryIndex
            1 -> if (selectedCategoryIndex !in listOf(1, 5, 6)) 1 else selectedCategoryIndex
            else -> selectedCategoryIndex
        }
        val computed = PlayerStatisticsCalculator.computeSquadRankings(effectiveCategory, allDetailedStats)
        if (selectedRolePill == 2) {
            val allRounders = computed.filter { it.player.role == PlayerRole.ALL_ROUNDER }
            if (allRounders.isNotEmpty()) {
                allRounders.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
            } else {
                computed
            }
        } else {
            computed
        }
    }

    // Compute Team Rankings & Points Table from real matches
    val teamRankings = remember(formatFilteredMatches, teamName) {
        computeTeamPointsTable(formatFilteredMatches, teamName.ifBlank { "Rajpoot Cricket Boys" })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Reference Header Bar: Centered "Rankings" + Compare & Filter Icons
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquid3DEntrance()
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.glassCardElevated)
                        .border(1.dp, tokens.glassCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToComparison(0L) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Compare Players",
                        tint = tokens.accentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Compare",
                        color = tokens.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Rankings",
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = { showPeriodFilters = !showPeriodFilters },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Season & Tournament Filters",
                        tint = if (selectedSeasonFilter != null || selectedTournamentFilter != null) {
                            tokens.accentGold
                        } else {
                            tokens.textPrimary
                        }
                    )
                }
            }
        }

        // 2. Outer Segmented Format Selector (matches Right Phone in reference image)
        item {
            SegmentedFormatSelector(
                options = listOf("All Formats", "T20", "T10 / ODI"),
                selectedIndex = selectedFormatIndex,
                onSelect = { selectedFormatIndex = it },
                modifier = Modifier.liquid3DEntrance(delayMs = 30)
            )
        }

        // 3. Role Pill Tabs: Batter | Bowler | All Rounder | Team (matches Right Phone in reference image)
        item {
            CyanPillTabs(
                tabs = listOf("Batter", "Bowler", "All Rounder", "Team"),
                selectedIndex = selectedRolePill,
                onTabSelected = { idx ->
                    selectedRolePill = idx
                    when (idx) {
                        0 -> selectedCategoryIndex = 0 // Most Runs
                        1 -> selectedCategoryIndex = 1 // Most Wickets
                        2 -> selectedCategoryIndex = 0 // All Rounder
                    }
                },
                modifier = Modifier.liquid3DEntrance(delayMs = 50)
            )
        }

        // Optional Season / Tournament Filter Bar & Detailed Metric Sub-Pills
        if (showPeriodFilters || selectedSeasonFilter != null || selectedTournamentFilter != null) {
            item {
                PeriodFilterBar(
                    availableSeasons = seasons,
                    availableTournaments = tournaments,
                    selectedSeason = selectedSeasonFilter,
                    selectedTournament = selectedTournamentFilter,
                    onSelectSeason = { selectedSeasonFilter = it },
                    onSelectTournament = { selectedTournamentFilter = it }
                )
            }
        }

        if (selectedRolePill != 3) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    itemsIndexed(categories) { idx, label ->
                        val active = selectedCategoryIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) Color(0xFF12345E) else tokens.chipBackground)
                                .border(
                                    1.dp,
                                    if (active) tokens.accentGold else tokens.glassCardBorderSubtle,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategoryIndex = idx }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (active) tokens.accentGoldLight else tokens.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 4. Player Rankings Table Card (matches Right Phone in reference image)
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquid3DEntrance(delayMs = 70),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassCardBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Table Header Row: # | Player | Team | Points
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#",
                                color = tokens.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(26.dp)
                            )
                            Text(
                                text = "Player",
                                color = tokens.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "Team",
                                color = tokens.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.95f)
                            )
                            Text(
                                text = categories.getOrElse(selectedCategoryIndex) { "Points" }.substringAfter(" "),
                                color = tokens.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = tokens.glassCardBorderSubtle, thickness = 1.dp)

                        if (rankingItems.isEmpty()) {
                            // Fallback squad display when no deliveries have been scored yet
                            players.forEachIndexed { idx, player ->
                                val baseRating = 890 - (idx * 18).coerceAtMost(300)
                                PlayerRankingTableRow(
                                    rank = idx + 1,
                                    playerName = player.fullName,
                                    roleSubtitle = player.role.displayName,
                                    photoUri = player.photoUri,
                                    teamLabel = teamName.ifBlank { "Rajpoot Boys" },
                                    statValue = "$baseRating",
                                    onClick = { onNavigateToProfile(player.id) }
                                )
                                if (idx < players.lastIndex) {
                                    HorizontalDivider(
                                        color = tokens.glassCardBorderSubtle.copy(alpha = 0.5f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        } else {
                            rankingItems.forEachIndexed { idx, item ->
                                PlayerRankingTableRow(
                                    rank = item.rank,
                                    playerName = item.player.fullName,
                                    roleSubtitle = item.secondaryValue.ifEmpty { item.player.role.displayName },
                                    photoUri = item.player.photoUri,
                                    teamLabel = teamName.ifBlank { "Rajpoot Boys" },
                                    statValue = item.primaryValue,
                                    onClick = { onNavigateToProfile(item.player.id) }
                                )
                                if (idx < rankingItems.lastIndex) {
                                    HorizontalDivider(
                                        color = tokens.glassCardBorderSubtle.copy(alpha = 0.5f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Team Rankings & Tournament Points Table Card (matches bottom of Right Phone in reference image)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquid3DEntrance(delayMs = 100),
                backgroundColor = tokens.glassCardBackground,
                borderColor = tokens.glassCardBorder
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFE5C158),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Team Rankings & Points Table",
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedRolePill = if (selectedRolePill == 3) 0 else 3 }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedRolePill == 3) "Show Players" else "View All",
                                color = tokens.accentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = tokens.accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = tokens.glassCardBorderSubtle, thickness = 1.dp)

                    teamRankings.forEachIndexed { idx, teamEntry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${teamEntry.rank}",
                                    color = tokens.textPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.width(24.dp)
                                )
                                TeamShieldBadge(teamName = teamEntry.teamName, size = 28.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = teamEntry.teamName,
                                        color = tokens.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "P: ${teamEntry.matchesPlayed} • W: ${teamEntry.wins} • L: ${teamEntry.losses}",
                                        color = tokens.textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tokens.chipBackground)
                                    .border(1.dp, tokens.glassCardBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${teamEntry.points}",
                                    color = tokens.textPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        if (idx < teamRankings.lastIndex) {
                            HorizontalDivider(
                                color = tokens.glassCardBorderSubtle.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerRankingTableRow(
    rank: Int,
    playerName: String,
    roleSubtitle: String,
    photoUri: String?,
    teamLabel: String,
    statValue: String,
    onClick: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Number
        Text(
            text = "$rank",
            color = if (rank <= 3) tokens.accentGold else tokens.textPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            modifier = Modifier.width(26.dp)
        )

        // Player Avatar + Name + Subtitle
        Row(
            modifier = Modifier.weight(1.3f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tokens.glassCardElevated)
                    .border(
                        width = if (rank == 1) 1.5.dp else 1.dp,
                        color = if (rank <= 3) tokens.accentGold else tokens.glassCardBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = playerName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = tokens.accentGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = playerName,
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = roleSubtitle,
                    color = tokens.textSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Team Shield + Team Name
        Row(
            modifier = Modifier.weight(0.95f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamShieldBadge(teamName = teamLabel, size = 22.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (teamLabel.contains("Rajpoot", ignoreCase = true)) "Rajpoot" else teamLabel,
                color = tokens.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Points / Stat Pill Badge
        Box(
            modifier = Modifier
                .width(64.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(tokens.chipBackground)
                    .border(
                        width = 1.dp,
                        color = if (rank == 1) tokens.accentGold else tokens.glassCardBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statValue,
                    color = if (rank == 1) tokens.accentGoldLight else tokens.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun computeTeamPointsTable(
    matches: List<MatchEntity>,
    defaultHomeTeam: String
): List<TeamRankingEntry> {
    data class MutableTeamStats(
        var played: Int = 0,
        var wins: Int = 0,
        var losses: Int = 0,
        var ratingPoints: Int = 100
    )

    val map = linkedMapOf<String, MutableTeamStats>()
    map[defaultHomeTeam] = MutableTeamStats(ratingPoints = 124)

    matches.forEach { m ->
        val home = m.homeTeamName.ifBlank { defaultHomeTeam }
        val away = m.awayTeamName.ifBlank { "Rival XI" }
        val hStats = map.getOrPut(home) { MutableTeamStats(ratingPoints = 118) }
        val aStats = map.getOrPut(away) { MutableTeamStats(ratingPoints = 114) }

        if (m.isCompleted) {
            hStats.played++
            aStats.played++
            val winner = m.winnerTeam.orEmpty()
            when {
                winner.contains(home, ignoreCase = true) -> {
                    hStats.wins++
                    hStats.ratingPoints += 4
                    aStats.losses++
                }
                winner.contains(away, ignoreCase = true) -> {
                    aStats.wins++
                    aStats.ratingPoints += 4
                    hStats.losses++
                }
            }
        }
    }

    if (map.size == 1) {
        map["Rival XI"] = MutableTeamStats(played = 0, wins = 0, losses = 0, ratingPoints = 120)
        map["Warriors XI"] = MutableTeamStats(played = 0, wins = 0, losses = 0, ratingPoints = 116)
    }

    return map.entries
        .sortedWith(compareByDescending<Map.Entry<String, MutableTeamStats>> { it.value.wins * 20 + it.value.ratingPoints })
        .mapIndexed { idx, entry ->
            TeamRankingEntry(
                rank = idx + 1,
                teamName = entry.key,
                matchesPlayed = entry.value.played,
                wins = entry.value.wins,
                losses = entry.value.losses,
                points = entry.value.ratingPoints
            )
        }
}

