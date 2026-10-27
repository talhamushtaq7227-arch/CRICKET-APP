package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import com.example.domain.engine.PlayerDetailedStatistics
import com.example.domain.engine.PlayerMatchPerformance
import com.example.domain.engine.PlayerStatisticsCalculator
import com.example.ui.components.CricketBarChart
import com.example.ui.components.CricketTrendLineChart
import com.example.ui.components.EditPlayerProfileDialog
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalCard
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
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.theme.LiquidGlassTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    playerId: Long,
    viewModel: CricketViewModel,
    onBack: () -> Unit,
    onNavigateToScorecard: (Long) -> Unit,
    onNavigateToComparison: (Long) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val tokens = LiquidGlassTheme.tokens

    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val deliveries by viewModel.allDeliveries.collectAsStateWithLifecycle()
    val seasons by viewModel.distinctSeasons.collectAsStateWithLifecycle()
    val tournaments by viewModel.distinctTournaments.collectAsStateWithLifecycle()

    val player = players.find { it.id == playerId }

    var selectedSeasonFilter by remember { mutableStateOf<String?>(null) }
    var selectedTournamentFilter by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (player == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Player Not Found", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tokens.accentGold)
                }
            }
        }
        return
    }

    // Compute live authoritative stats from saved matches on background thread
    val stats by produceState(
        initialValue = remember(player) {
            PlayerStatisticsCalculator.calculatePlayerStatistics(
                player = player,
                matches = emptyList(),
                deliveries = emptyList()
            )
        },
        player, matches, deliveries, selectedSeasonFilter, selectedTournamentFilter
    ) {
        value = withContext(Dispatchers.Default) {
            PlayerStatisticsCalculator.calculatePlayerStatistics(
                player = player,
                matches = matches,
                deliveries = deliveries,
                seasonFilter = selectedSeasonFilter,
                tournamentFilter = selectedTournamentFilter
            )
        }
    }

    val tabs = listOf("Overview", "Batting", "Bowling", "History (${stats.matchPerformances.size})")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // App Bar & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = tokens.accentGold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Player Profile",
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Row {
                IconButton(onClick = { onNavigateToComparison(player.id) }) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Compare Player",
                        tint = tokens.accentGold
                    )
                }
                IconButton(onClick = {
                    val summary = buildStatsShareText(stats)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Player Stats", summary))
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Stats",
                        tint = tokens.accentGold
                    )
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = tokens.accentGold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Player Profile Header
            item {
                PlayerHeaderSection(
                    player = player,
                    onEditPhoto = { showEditDialog = true }
                )
            }

            // Season & Tournament Filter Bar
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

            // Top Dashboard Glass Cards (Based on Role)
            item {
                PlayerRoleDashboardCards(
                    player = player,
                    stats = stats
                )
            }

            // Profile Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = tokens.glassCardElevated,
                    contentColor = tokens.accentGold,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = tokens.accentGold,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) tokens.accentGold else tokens.textSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Overview Tab
                    item {
                        OverviewTabContent(
                            player = player,
                            stats = stats,
                            onNavigateToScorecard = onNavigateToScorecard
                        )
                    }
                }
                1 -> {
                    // Batting Tab
                    item {
                        BattingTabContent(stats = stats)
                    }
                }
                2 -> {
                    // Bowling Tab
                    item {
                        BowlingTabContent(stats = stats)
                    }
                }
                3 -> {
                    // Performance History Tab
                    items(stats.matchPerformances) { performance ->
                        MatchPerformanceCard(
                            performance = performance,
                            onClick = { onNavigateToScorecard(performance.matchId) }
                        )
                    }
                    if (stats.matchPerformances.isEmpty()) {
                        item {
                            EmptyHistoryCard()
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditPlayerProfileDialog(
            player = player,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.updatePlayer(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun PlayerHeaderSection(
    player: PlayerEntity,
    onEditPhoto: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = tokens.glassCardBackground,
            borderColor = tokens.glassBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Player Photo or Silhouette Avatar
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(tokens.accentGold.copy(alpha = 0.35f), tokens.glassCardElevated)
                                )
                            )
                            .border(2.5.dp, tokens.accentGold, CircleShape)
                            .clickable { onEditPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!player.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = player.photoUri,
                                contentDescription = player.fullName,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            if (player.jerseyNumber > 0) {
                                Text(
                                    text = "#${player.jerseyNumber}",
                                    color = tokens.accentGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = tokens.accentGold,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Player identity
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = player.fullName,
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            if (player.isCaptain) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(tokens.accentGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CAPTAIN",
                                        color = tokens.buttonContent,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
                            } else if (player.isViceCaptain) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(tokens.accentGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VC",
                                        color = tokens.accentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Rajpoot Cricket Boys",
                            color = tokens.accentGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Role Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tokens.glassCardElevated)
                                    .border(1.dp, tokens.glassBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = player.role.displayName,
                                    color = tokens.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Jersey #
                            if (player.jerseyNumber > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(tokens.accentGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "No. ${player.jerseyNumber}",
                                        color = tokens.accentGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Active / Inactive
                            if (!player.isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x33FF5252))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Inactive",
                                        color = Color(0xFFFF5252),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodFilterBar(
    availableSeasons: List<String>,
    availableTournaments: List<String>,
    selectedSeason: String?,
    selectedTournament: String?,
    onSelectSeason: (String?) -> Unit,
    onSelectTournament: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PeriodChip(
                label = "Career (All-Time)",
                isSelected = selectedSeason == null && selectedTournament == null,
                onClick = {
                    onSelectSeason(null)
                    onSelectTournament(null)
                }
            )
        }

        // Season Chips
        val seasonList = if (availableSeasons.isNotEmpty()) availableSeasons else listOf("2026")
        items(seasonList) { season ->
            PeriodChip(
                label = "Season $season",
                isSelected = selectedSeason == season && selectedTournament == null,
                onClick = {
                    onSelectSeason(if (selectedSeason == season) null else season)
                    onSelectTournament(null)
                }
            )
        }

        // Tournament Chips
        items(availableTournaments) { tourney ->
            PeriodChip(
                label = tourney,
                isSelected = selectedTournament == tourney,
                onClick = {
                    onSelectTournament(if (selectedTournament == tourney) null else tourney)
                    onSelectSeason(null)
                }
            )
        }
    }
}

@Composable
fun PeriodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val bg = if (isSelected) tokens.accentGold else tokens.glassCardElevated
    val textColor = if (isSelected) tokens.buttonContent else tokens.textSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, if (isSelected) tokens.accentGold else tokens.glassBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun PlayerRoleDashboardCards(
    player: PlayerEntity,
    stats: PlayerDetailedStatistics
) {
    val tokens = LiquidGlassTheme.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        when (player.role) {
            PlayerRole.BATTER -> {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileMetric("MATCHES", "${stats.matchesPlayed}")
                        ProfileMetric("RUNS", "${stats.totalRuns}")
                        ProfileMetric("HIGHEST", stats.highestScoreFormatted)
                        ProfileMetric("AVERAGE", stats.formattedBattingAverage)
                        ProfileMetric("STRIKE RATE", stats.formattedStrikeRate)
                    }
                }
            }
            PlayerRole.BOWLER -> {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileMetric("MATCHES", "${stats.matchesPlayed}")
                        ProfileMetric("WICKETS", "${stats.wickets}")
                        ProfileMetric("BEST", stats.bestBowlingFigures)
                        ProfileMetric("AVERAGE", stats.formattedBowlingAverage)
                        ProfileMetric("ECONOMY", stats.formattedEconomyRate)
                    }
                }
            }
            PlayerRole.ALL_ROUNDER -> {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileMetric("RUNS", "${stats.totalRuns}")
                            ProfileMetric("BAT AVG", stats.formattedBattingAverage)
                            ProfileMetric("WICKETS", "${stats.wickets}")
                            ProfileMetric("BOWL ECON", stats.formattedEconomyRate)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(tokens.glassCardElevated, RoundedCornerShape(8.dp))
                                .border(1.dp, tokens.glassBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All-Round Impact",
                                color = tokens.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${stats.totalRuns} runs • ${stats.wickets} wickets in ${stats.matchesPlayed} matches",
                                color = tokens.accentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            PlayerRole.WICKET_KEEPER -> {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileMetric("MATCHES", "${stats.matchesPlayed}")
                        ProfileMetric("RUNS", "${stats.totalRuns}")
                        ProfileMetric("DISMISSALS", "${stats.catches + stats.stumpings}")
                        ProfileMetric("CATCHES", "${stats.catches}")
                        ProfileMetric("STUMPINGS", "${stats.stumpings}")
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewTabContent(
    player: PlayerEntity,
    stats: PlayerDetailedStatistics,
    onNavigateToScorecard: (Long) -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Player Bio Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = tokens.glassCardBackground,
            borderColor = tokens.glassBorder
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CRICKET PROFILE",
                    color = tokens.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BioItem("Player ID", "#RCB-${player.id}")
                    BioItem("Batting Hand", player.battingHand.displayName)
                    BioItem("Bowling Style", player.bowlingStyle.displayName)
                }
            }
        }

        // Fielding Statistics
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = tokens.glassCardBackground,
            borderColor = tokens.glassBorder
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "FIELDING RECORD",
                    color = tokens.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileMetric("CATCHES", "${stats.catches}")
                    ProfileMetric("STUMPINGS", "${stats.stumpings}")
                    ProfileMetric("RUN OUTS", "${stats.runOuts}")
                    ProfileMetric("TOTAL", "${stats.catches + stats.stumpings + stats.runOuts}")
                }
            }
        }

        // Mini Charts in Overview
        CricketBarChart(
            title = "Runs by Match",
            data = stats.runsTrend,
            unit = "runs"
        )

        CricketBarChart(
            title = "Wickets by Match",
            data = stats.wicketsTrend,
            unit = "wkts"
        )

        // Latest Match Preview
        val latest = stats.matchPerformances.firstOrNull()
        if (latest != null) {
            Text(
                text = "LATEST PERFORMANCE",
                color = tokens.accentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            MatchPerformanceCard(
                performance = latest,
                onClick = { onNavigateToScorecard(latest.matchId) }
            )
        }
    }
}

@Composable
fun BattingTabContent(stats: PlayerDetailedStatistics) {
    val tokens = LiquidGlassTheme.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Detailed Batting Grid Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = tokens.glassCardBackground,
            borderColor = tokens.glassBorder
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CAREER BATTING STATISTICS",
                    color = tokens.accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Matches", "${stats.matchesPlayed}")
                    StatBox("Innings", "${stats.inningsBatted}")
                    StatBox("Not Outs", "${stats.notOuts}")
                    StatBox("Runs", "${stats.totalRuns}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Highest Score", stats.highestScoreFormatted)
                    StatBox("Average", stats.formattedBattingAverage)
                    StatBox("Balls Faced", "${stats.ballsFaced}")
                    StatBox("Strike Rate", stats.formattedStrikeRate)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Fours (4s)", "${stats.fours}")
                    StatBox("Sixes (6s)", "${stats.sixes}")
                    StatBox("50s", "${stats.fifties}")
                    StatBox("100s", "${stats.hundreds}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Ducks (0)", "${stats.ducks}")
                    StatBox("Dismissals", "${stats.dismissals}")
                    StatBox("Boundary Runs", "${stats.fours * 4 + stats.sixes * 6}")
                    val boundaryPct = if (stats.totalRuns > 0) {
                        String.format(Locale.US, "%.0f%%", ((stats.fours * 4 + stats.sixes * 6).toDouble() / stats.totalRuns) * 100.0)
                    } else "0%"
                    StatBox("Boundary %", boundaryPct)
                }
            }
        }

        // Charts
        CricketBarChart(
            title = "Runs Scored per Match",
            data = stats.runsTrend,
            unit = "runs"
        )

        CricketTrendLineChart(
            title = "Batting Strike Rate Trend",
            data = stats.strikeRateTrend,
            unit = "SR"
        )
    }
}

@Composable
fun BowlingTabContent(stats: PlayerDetailedStatistics) {
    val tokens = LiquidGlassTheme.tokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Detailed Bowling Grid Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = tokens.glassCardBackground,
            borderColor = tokens.glassBorder
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CAREER BOWLING STATISTICS",
                    color = tokens.accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Matches Bowled", "${stats.matchesBowled}")
                    StatBox("Overs", stats.formattedOversBowled)
                    StatBox("Legal Balls", "${stats.legalBallsBowled}")
                    StatBox("Maidens", "${stats.maidens}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Runs Conceded", "${stats.runsConceded}")
                    StatBox("Wickets", "${stats.wickets}")
                    StatBox("Average", stats.formattedBowlingAverage)
                    StatBox("Economy", stats.formattedEconomyRate)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox("Strike Rate", stats.formattedBowlingStrikeRate)
                    StatBox("Best Figures", stats.bestBowlingFigures)
                    StatBox("Dot Balls", "${stats.dotBalls}")
                    val dotPct = if (stats.legalBallsBowled > 0) {
                        String.format(Locale.US, "%.0f%%", (stats.dotBalls.toDouble() / stats.legalBallsBowled) * 100.0)
                    } else "0%"
                    StatBox("Dot %", dotPct)
                }
            }
        }

        // Charts
        CricketBarChart(
            title = "Wickets Taken per Match",
            data = stats.wicketsTrend,
            unit = "wkts"
        )

        CricketTrendLineChart(
            title = "Bowling Economy Trend",
            data = stats.economyTrend,
            unit = "Econ"
        )
    }
}

@Composable
fun MatchPerformanceCard(
    performance: PlayerMatchPerformance,
    onClick: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val dateStr = remember(performance.matchDateMillis) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        sdf.format(Date(performance.matchDateMillis))
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        backgroundColor = tokens.glassCardBackground,
        borderColor = tokens.glassBorder
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Date & Opponent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "vs ${performance.opponent}",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "$dateStr • ${performance.tournamentName}",
                        color = tokens.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tokens.accentGold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "View Scorecard",
                        color = tokens.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Batting performance row
            if (performance.didBat) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tokens.glassCardElevated, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bat: ",
                                color = tokens.accentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${performance.runsScored}${if (!performance.isOut) "*" else ""} ",
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "(${performance.ballsFaced} balls, ${performance.fours}x4, ${performance.sixes}x6)",
                                color = tokens.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = performance.dismissalText,
                            color = tokens.textMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "SR: ${performance.formattedBattingSR}",
                        color = tokens.accentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Bowling performance row
            if (performance.didBowl) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tokens.glassCardElevated, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bowl: ",
                            color = tokens.accentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${performance.wicketsTaken}/${performance.runsConceded} ",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "(${performance.oversFormatted} ov, ${performance.maidens} M)",
                            color = tokens.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "Econ: ${performance.economyRate?.let { String.format(Locale.US, "%.1f", it) } ?: "—"}",
                        color = tokens.accentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Fielding contributions if any
            if (performance.catches + performance.stumpings + performance.runOuts > 0) {
                Text(
                    text = "Fielding: ${performance.catches} catch(es), ${performance.stumpings} stumping(s), ${performance.runOuts} run out(s)",
                    color = tokens.accentGold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Match result
            Text(
                text = "Result: ${performance.matchResult}",
                color = tokens.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyHistoryCard() {
    val tokens = LiquidGlassTheme.tokens
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = tokens.glassCardBackground,
        borderColor = tokens.glassBorder
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Match History for Selected Period",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Deliveries scored with this player will appear here chronologically",
                color = tokens.textSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ProfileMetric(label: String, value: String) {
    val tokens = LiquidGlassTheme.tokens
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = tokens.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = tokens.accentGold,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StatBox(label: String, value: String) {
    val tokens = LiquidGlassTheme.tokens
    Column(
        modifier = Modifier
            .width(76.dp)
            .background(tokens.glassCardElevated, RoundedCornerShape(8.dp))
            .border(1.dp, tokens.glassBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = tokens.accentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            text = label,
            color = tokens.textSecondary,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
fun BioItem(label: String, value: String) {
    val tokens = LiquidGlassTheme.tokens
    Column {
        Text(text = label, color = tokens.textMuted, fontSize = 10.sp)
        Text(text = value, color = tokens.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

private fun buildStatsShareText(stats: PlayerDetailedStatistics): String {
    return """
        Rajpoot Cricket Boys - Official Player Record
        Player: ${stats.player.fullName} (#${stats.player.jerseyNumber})
        Role: ${stats.player.role.displayName}
        
        BATTING:
        Matches: ${stats.matchesPlayed} | Innings: ${stats.inningsBatted} (${stats.notOuts} NO)
        Runs: ${stats.totalRuns} | High Score: ${stats.highestScoreFormatted}
        Average: ${stats.formattedBattingAverage} | Strike Rate: ${stats.formattedStrikeRate}
        4s: ${stats.fours} | 6s: ${stats.sixes} | 50s: ${stats.fifties} | 100s: ${stats.hundreds}
        
        BOWLING:
        Overs: ${stats.formattedOversBowled} | Wickets: ${stats.wickets}
        Best: ${stats.bestBowlingFigures} | Economy: ${stats.formattedEconomyRate}
        Average: ${stats.formattedBowlingAverage} | Maidens: ${stats.maidens}
    """.trimIndent()
}
