package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.PlayerEntity
import com.example.domain.engine.PlayerDetailedStatistics
import com.example.domain.engine.PlayerStatisticsCalculator
import com.example.ui.components.GlassCard
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel

@Composable
fun PlayerComparisonScreen(
    initialPlayer1Id: Long = 0L,
    initialPlayer2Id: Long = 0L,
    viewModel: CricketViewModel,
    onBack: () -> Unit,
    onNavigateToProfile: (Long) -> Unit
) {
    BackHandler { onBack() }
    val tokens = LiquidGlassTheme.tokens

    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val deliveries by viewModel.allDeliveries.collectAsStateWithLifecycle()
    val seasons by viewModel.distinctSeasons.collectAsStateWithLifecycle()
    val tournaments by viewModel.distinctTournaments.collectAsStateWithLifecycle()

    var player1Id by remember {
        mutableStateOf(
            if (initialPlayer1Id > 0 && players.any { it.id == initialPlayer1Id }) initialPlayer1Id
            else players.firstOrNull()?.id ?: 0L
        )
    }

    var player2Id by remember {
        mutableStateOf(
            if (initialPlayer2Id > 0 && players.any { it.id == initialPlayer2Id }) initialPlayer2Id
            else players.getOrNull(1)?.id ?: players.firstOrNull()?.id ?: 0L
        )
    }

    var selectedSeasonFilter by remember { mutableStateOf<String?>(null) }
    var selectedTournamentFilter by remember { mutableStateOf<String?>(null) }

    val player1 = players.find { it.id == player1Id } ?: players.firstOrNull()
    val player2 = players.find { it.id == player2Id } ?: players.getOrNull(1)

    val stats1 = remember(player1, matches, deliveries, selectedSeasonFilter, selectedTournamentFilter) {
        if (player1 != null) {
            PlayerStatisticsCalculator.calculatePlayerStatistics(
                player = player1,
                matches = matches,
                deliveries = deliveries,
                seasonFilter = selectedSeasonFilter,
                tournamentFilter = selectedTournamentFilter
            )
        } else null
    }

    val stats2 = remember(player2, matches, deliveries, selectedSeasonFilter, selectedTournamentFilter) {
        if (player2 != null) {
            PlayerStatisticsCalculator.calculatePlayerStatistics(
                player = player2,
                matches = matches,
                deliveries = deliveries,
                seasonFilter = selectedSeasonFilter,
                tournamentFilter = selectedTournamentFilter
            )
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = tokens.accentGold
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Player Head-to-Head Comparison",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // Period filter bar to ensure fair comparison
        PeriodFilterBar(
            availableSeasons = seasons,
            availableTournaments = tournaments,
            selectedSeason = selectedSeasonFilter,
            selectedTournament = selectedTournamentFilter,
            onSelectSeason = { selectedSeasonFilter = it },
            onSelectTournament = { selectedTournamentFilter = it }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Player Selectors Card
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Player 1 Selector
                        Box(modifier = Modifier.weight(1f)) {
                            PlayerSelector(
                                label = "Player 1",
                                selectedPlayer = player1,
                                allPlayers = players,
                                onSelect = { player1Id = it.id },
                                onProfileClick = { if (player1 != null) onNavigateToProfile(player1.id) }
                            )
                        }

                        // VS Badge
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tokens.accentGold.copy(alpha = 0.2f))
                                .border(1.dp, tokens.accentGold.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VS",
                                color = tokens.accentGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }

                        // Player 2 Selector
                        Box(modifier = Modifier.weight(1f)) {
                            PlayerSelector(
                                label = "Player 2",
                                selectedPlayer = player2,
                                allPlayers = players,
                                onSelect = { player2Id = it.id },
                                onProfileClick = { if (player2 != null) onNavigateToProfile(player2.id) }
                            )
                        }
                    }
                }
            }

            if (stats1 != null && stats2 != null) {
                // Batting Comparison
                item {
                    Text(
                        text = "BATTING COMPARISON",
                        color = tokens.accentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = tokens.glassCardBackground,
                        borderColor = tokens.glassBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            ComparisonMetricRow(
                                label = "Total Runs",
                                val1 = "${stats1.totalRuns}",
                                val2 = "${stats2.totalRuns}",
                                is1Better = stats1.totalRuns > stats2.totalRuns,
                                is2Better = stats2.totalRuns > stats1.totalRuns
                            )
                            ComparisonMetricRow(
                                label = "Matches Played",
                                val1 = "${stats1.matchesPlayed}",
                                val2 = "${stats2.matchesPlayed}",
                                is1Better = stats1.matchesPlayed > stats2.matchesPlayed,
                                is2Better = stats2.matchesPlayed > stats1.matchesPlayed
                            )
                            ComparisonMetricRow(
                                label = "Innings Batted",
                                val1 = "${stats1.inningsBatted}",
                                val2 = "${stats2.inningsBatted}",
                                is1Better = false,
                                is2Better = false
                            )
                            ComparisonMetricRow(
                                label = "Highest Score",
                                val1 = stats1.highestScoreFormatted,
                                val2 = stats2.highestScoreFormatted,
                                is1Better = stats1.highestScore > stats2.highestScore,
                                is2Better = stats2.highestScore > stats1.highestScore
                            )
                            ComparisonMetricRow(
                                label = "Batting Average",
                                val1 = stats1.formattedBattingAverage,
                                val2 = stats2.formattedBattingAverage,
                                is1Better = (stats1.battingAverage ?: 0.0) > (stats2.battingAverage ?: 0.0),
                                is2Better = (stats2.battingAverage ?: 0.0) > (stats1.battingAverage ?: 0.0)
                            )
                            ComparisonMetricRow(
                                label = "Strike Rate",
                                val1 = stats1.formattedStrikeRate,
                                val2 = stats2.formattedStrikeRate,
                                is1Better = (stats1.strikeRate ?: 0.0) > (stats2.strikeRate ?: 0.0),
                                is2Better = (stats2.strikeRate ?: 0.0) > (stats1.strikeRate ?: 0.0)
                            )
                            ComparisonMetricRow(
                                label = "Fifties (50s)",
                                val1 = "${stats1.fifties}",
                                val2 = "${stats2.fifties}",
                                is1Better = stats1.fifties > stats2.fifties,
                                is2Better = stats2.fifties > stats1.fifties
                            )
                            ComparisonMetricRow(
                                label = "Hundreds (100s)",
                                val1 = "${stats1.hundreds}",
                                val2 = "${stats2.hundreds}",
                                is1Better = stats1.hundreds > stats2.hundreds,
                                is2Better = stats2.hundreds > stats1.hundreds
                            )
                            ComparisonMetricRow(
                                label = "Fours (4s)",
                                val1 = "${stats1.fours}",
                                val2 = "${stats2.fours}",
                                is1Better = stats1.fours > stats2.fours,
                                is2Better = stats2.fours > stats1.fours
                            )
                            ComparisonMetricRow(
                                label = "Sixes (6s)",
                                val1 = "${stats1.sixes}",
                                val2 = "${stats2.sixes}",
                                is1Better = stats1.sixes > stats2.sixes,
                                is2Better = stats2.sixes > stats1.sixes
                            )
                        }
                    }
                }

                // Bowling Comparison
                item {
                    Text(
                        text = "BOWLING COMPARISON",
                        color = tokens.accentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = tokens.glassCardBackground,
                        borderColor = tokens.glassBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            ComparisonMetricRow(
                                label = "Wickets Taken",
                                val1 = "${stats1.wickets}",
                                val2 = "${stats2.wickets}",
                                is1Better = stats1.wickets > stats2.wickets,
                                is2Better = stats2.wickets > stats1.wickets
                            )
                            ComparisonMetricRow(
                                label = "Overs Bowled",
                                val1 = stats1.formattedOversBowled,
                                val2 = stats2.formattedOversBowled,
                                is1Better = false,
                                is2Better = false
                            )
                            ComparisonMetricRow(
                                label = "Bowling Economy",
                                val1 = stats1.formattedEconomyRate,
                                val2 = stats2.formattedEconomyRate,
                                is1Better = (stats1.economyRate != null && stats2.economyRate != null && stats1.economyRate!! < stats2.economyRate!!),
                                is2Better = (stats1.economyRate != null && stats2.economyRate != null && stats2.economyRate!! < stats1.economyRate!!)
                            )
                            ComparisonMetricRow(
                                label = "Bowling Average",
                                val1 = stats1.formattedBowlingAverage,
                                val2 = stats2.formattedBowlingAverage,
                                is1Better = (stats1.bowlingAverage != null && stats2.bowlingAverage != null && stats1.bowlingAverage!! < stats2.bowlingAverage!!),
                                is2Better = (stats1.bowlingAverage != null && stats2.bowlingAverage != null && stats2.bowlingAverage!! < stats1.bowlingAverage!!)
                            )
                            ComparisonMetricRow(
                                label = "Best Bowling",
                                val1 = stats1.bestBowlingFigures,
                                val2 = stats2.bestBowlingFigures,
                                is1Better = stats1.bestBowlingWickets > stats2.bestBowlingWickets,
                                is2Better = stats2.bestBowlingWickets > stats1.bestBowlingWickets
                            )
                            ComparisonMetricRow(
                                label = "Maidens",
                                val1 = "${stats1.maidens}",
                                val2 = "${stats2.maidens}",
                                is1Better = stats1.maidens > stats2.maidens,
                                is2Better = stats2.maidens > stats1.maidens
                            )
                            ComparisonMetricRow(
                                label = "Dot Balls",
                                val1 = "${stats1.dotBalls}",
                                val2 = "${stats2.dotBalls}",
                                is1Better = stats1.dotBalls > stats2.dotBalls,
                                is2Better = stats2.dotBalls > stats1.dotBalls
                            )
                            ComparisonMetricRow(
                                label = "Catches",
                                val1 = "${stats1.catches}",
                                val2 = "${stats2.catches}",
                                is1Better = stats1.catches > stats2.catches,
                                is2Better = stats2.catches > stats1.catches
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerSelector(
    label: String,
    selectedPlayer: PlayerEntity?,
    allPlayers: List<PlayerEntity>,
    onSelect: (PlayerEntity) -> Unit,
    onProfileClick: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = tokens.textMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(tokens.accentGold.copy(alpha = 0.2f))
                .border(2.dp, tokens.accentGold, CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!selectedPlayer?.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = selectedPlayer?.photoUri,
                    contentDescription = selectedPlayer?.fullName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "#${selectedPlayer?.jerseyNumber ?: 0}",
                    color = tokens.accentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(tokens.glassCardElevated)
                .border(1.dp, tokens.glassBorder, RoundedCornerShape(6.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedPlayer?.fullName ?: "Select",
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = tokens.accentGold)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(tokens.glassCardElevated)
        ) {
            allPlayers.forEach { p ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${p.fullName} (#${p.jerseyNumber})",
                            color = if (p.id == selectedPlayer?.id) tokens.accentGold else tokens.textPrimary
                        )
                    },
                    onClick = {
                        onSelect(p)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ComparisonMetricRow(
    label: String,
    val1: String,
    val2: String,
    is1Better: Boolean,
    is2Better: Boolean
) {
    val tokens = LiquidGlassTheme.tokens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Val 1
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (is1Better) tokens.accentGold.copy(alpha = 0.2f) else Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = val1,
                color = if (is1Better) tokens.accentGold else tokens.textPrimary,
                fontWeight = if (is1Better) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }

        // Metric label in center
        Text(
            text = label,
            color = tokens.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(110.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Val 2
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (is2Better) tokens.accentGold.copy(alpha = 0.2f) else Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = val2,
                color = if (is2Better) tokens.accentGold else tokens.textPrimary,
                fontWeight = if (is2Better) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
