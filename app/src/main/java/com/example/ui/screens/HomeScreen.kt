package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.MatchEntity
import com.example.domain.engine.CricketRulesEngine
import com.example.domain.engine.InningsLiveState
import com.example.ui.components.CricketAnimationType
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.components.RcbCrestLogo
import com.example.ui.components.SkyCyanLiveScoreBanner
import com.example.ui.components.TeamShieldBadge
import com.example.ui.components.liquid3DEntrance
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentSix
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    onNavigateToRankings: () -> Unit,
    onNavigateToMatches: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val tokens = LiquidGlassTheme.tokens
    val teamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val allMatches by viewModel.allMatches.collectAsStateWithLifecycle()
    val unfinishedMatch by viewModel.latestUnfinishedMatch.collectAsStateWithLifecycle()
    val featuredMatchStates by viewModel.featuredMatchStates.collectAsStateWithLifecycle()
    val rankings by viewModel.rankings.collectAsStateWithLifecycle()
    val currentUser by viewModel.authManager.currentUser.collectAsStateWithLifecycle()

    val topBatter = remember(rankings) { rankings.maxByOrNull { it.runs } }
    val topBowler = remember(rankings) { rankings.maxByOrNull { it.wickets } }
    val featuredMatch = remember(unfinishedMatch, allMatches) { unfinishedMatch ?: allMatches.firstOrNull() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Reference Header: Menu + Centered RCB Crest Logo & Title + Notification Bell + Profile Avatar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquid3DEntrance(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu & Settings",
                            tint = tokens.textPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RcbCrestLogo(size = 52.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = teamName.ifBlank { "Rajpoot Cricket Boys" },
                            color = tokens.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (featuredMatch != null) {
                                        if (featuredMatch.isCompleted) {
                                            onNavigateToScorecard(featuredMatch.id)
                                        } else {
                                            viewModel.loadMatch(featuredMatch.id)
                                            onNavigateToLiveScoring(featuredMatch.id)
                                        }
                                    } else {
                                        onNavigateToMatchSetup()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Match Alerts",
                                tint = tokens.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF2442))
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // User / Captain Avatar
                        val avatarPhoto = currentUser?.photoUrl?.toString()
                            ?: players.firstOrNull { !it.photoUri.isNullOrBlank() }?.photoUri
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tokens.glassCardElevated)
                                .border(1.5.dp, tokens.accentGold, CircleShape)
                                .clickable(onClick = onNavigateToSquad),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarPhoto.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarPhoto,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Squad Profile",
                                    tint = tokens.accentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Hero Banner Card ("Together We Play Stronger" + Stadium Jersey Graphic + Cyan Arrow Button)
        item {
            HeroTeamBannerCard(
                teamName = teamName.ifBlank { "Rajpoot Cricket Boys" },
                onActionClick = onNavigateToMatchSetup,
                modifier = Modifier.liquid3DEntrance(delayMs = 30)
            )
        }

        // 3. Signature Bright Sky-Cyan Live Match Banner Card (matches reference image)
        item {
            val match = featuredMatch
            val (inn1, inn2) = featuredMatchStates

            if (match != null && inn1 != null) {
                val leftTeam = inn1.battingTeam.ifEmpty { match.homeTeamName }
                val rightTeam = inn1.bowlingTeam.ifEmpty { match.awayTeamName }
                val leftScore = "${inn1.totalRuns}/${inn1.totalWickets}"
                val leftOvers = "(${inn1.formattedOvers} ov)"

                val hasInnings2 = inn2 != null && (inn2.legalBalls > 0 || match.currentInnings == 2)
                val rightScore = if (hasInnings2) "${inn2!!.totalRuns}/${inn2.totalWickets}" else "0/0"
                val rightOvers = if (hasInnings2) "(${inn2!!.formattedOvers} ov)" else "(${match.totalOvers} ov)"

                val statusLine = when {
                    !match.winnerTeam.isNullOrBlank() -> match.winnerTeam!!
                    hasInnings2 && inn2?.target != null -> {
                        val remRuns = (inn2.target - inn2.totalRuns).coerceAtLeast(0)
                        val remBalls = (match.totalOvers * 6 - inn2.legalBalls).coerceAtLeast(0)
                        "${inn2.battingTeam} need $remRuns runs in $remBalls balls"
                    }
                    else -> "${inn1.battingTeam} batting • CRR ${inn1.formattedCRR} • Tap to score live"
                }

                SkyCyanLiveScoreBanner(
                    leftTeamName = leftTeam,
                    leftScore = leftScore,
                    leftOvers = leftOvers,
                    rightTeamName = rightTeam,
                    rightScore = rightScore,
                    rightOvers = rightOvers,
                    statusText = statusLine,
                    matchTitle = "${match.homeTeamName} vs ${match.awayTeamName}",
                    matchSubtitle = "${match.format.name} • ${match.venue}",
                    isLive = !match.isCompleted,
                    onClick = {
                        if (match.isCompleted) {
                            onNavigateToScorecard(match.id)
                        } else {
                            viewModel.loadMatch(match.id)
                            onNavigateToLiveScoring(match.id)
                        }
                    },
                    modifier = Modifier.liquid3DEntrance(delayMs = 60)
                )
            } else {
                // Default Live Score Banner when no matches have been created yet
                SkyCyanLiveScoreBanner(
                    leftTeamName = teamName.ifBlank { "Rajpoot Boys" },
                    leftScore = "0/0",
                    leftOvers = "(0.0 ov)",
                    rightTeamName = "Rival XI",
                    rightScore = "0/0",
                    rightOvers = "(20 ov)",
                    statusText = "Tap here to start a new Live Match & Scoreboard",
                    matchTitle = "${teamName.ifBlank { "Rajpoot Boys" }} vs Rival XI",
                    matchSubtitle = "T20 • National Cricket Ground",
                    isLive = true,
                    onClick = onNavigateToMatchSetup,
                    modifier = Modifier.liquid3DEntrance(delayMs = 60)
                )
            }
        }

        // 4. Quick Actions Bar (New Match, Squad, Live Score, Stats, Rankings)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquid3DEntrance(delayMs = 90),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Default.Add,
                    label = "New Match",
                    onClick = onNavigateToMatchSetup,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Default.Group,
                    label = "Squad (${players.size})",
                    onClick = onNavigateToSquad,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Default.PlayArrow,
                    label = "Live Score",
                    onClick = {
                        if (unfinishedMatch != null) {
                            viewModel.loadMatch(unfinishedMatch!!.id)
                            onNavigateToLiveScoring(unfinishedMatch!!.id)
                        } else {
                            onNavigateToMatchSetup()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Default.BarChart,
                    label = "Rankings",
                    onClick = onNavigateToRankings,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Upcoming Matches & Fixtures Section (matches reference image)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquid3DEntrance(delayMs = 110)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Matches",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToMatches)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View All",
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

                if (allMatches.isEmpty()) {
                    val defaultDateText = remember {
                        SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.US).format(Date())
                    }
                    UpcomingFixtureBannerCard(
                        homeTeam = teamName.ifBlank { "Rajpoot Boys" },
                        awayTeam = "Warriors",
                        dateTimeText = defaultDateText,
                        venueText = "Dera Ismail Khan",
                        statusBadge = "Start Match",
                        onClick = onNavigateToMatchSetup
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        allMatches.take(3).forEach { match ->
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
        }

        // 6. Top Stories & Match Highlights (matches reference image 2-column cards)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquid3DEntrance(delayMs = 140)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Stories",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToRankings)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View All",
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

                val story1Title = if (topBatter != null && topBatter.runs > 0) {
                    "${topBatter.playerName} hits brilliant ${topBatter.highestScore} (${topBatter.runs} total runs)"
                } else {
                    "Talha Mushtaq & squad ready for opening clash"
                }
                val story1Photo = players.find {
                    it.fullName.equals(topBatter?.playerName, ignoreCase = true)
                }?.photoUri

                val story2Title = if (topBowler != null && topBowler.wickets > 0) {
                    "${topBowler.playerName} leads attack with ${topBowler.wickets} wickets (${topBowler.bestBowlingFigures})"
                } else {
                    "${teamName.ifBlank { "Rajpoot Boys" }} prepare for next big clash"
                }
                val story2Photo = players.find {
                    it.fullName.equals(topBowler?.playerName, ignoreCase = true)
                }?.photoUri

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TopStoryCard(
                        headline = story1Title,
                        timeLabel = if (topBatter != null && topBatter.runs > 0) "Top Batter" else "Squad News",
                        photoUri = story1Photo,
                        isTeamGraphic = false,
                        onClick = onNavigateToRankings,
                        modifier = Modifier.weight(1f)
                    )
                    TopStoryCard(
                        headline = story2Title,
                        timeLabel = if (topBowler != null && topBowler.wickets > 0) "Top Bowler" else "Club Update",
                        photoUri = story2Photo,
                        isTeamGraphic = true,
                        onClick = onNavigateToSquad,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 7. Broadcast Celebration FX Bar
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "STADIUM BROADCAST CELEBRATIONS",
                        color = tokens.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
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
    }
}

/**
 * Hero Banner Card ("Together We Play Stronger") matching the reference image.
 */
@Composable
private fun HeroTeamBannerCard(
    teamName: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InteractiveGlassCard(
        onClick = onActionClick,
        modifier = modifier
            .fillMaxWidth()
            .height(164.dp),
        shape = RoundedCornerShape(22.dp),
        borderColor = Color(0xFF1F5D99),
        backgroundColor = Color(0xFF08182E)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val w = size.width
                    val h = size.height

                    val skyBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF061326),
                            Color(0xFF0D284C),
                            Color(0xFF123660)
                        )
                    )
                    val spotlightBrush = Brush.radialGradient(
                        colors = listOf(Color(0x553CE2FF), Color.Transparent),
                        center = Offset(w * 0.72f, h * 0.22f),
                        radius = w * 0.55f
                    )
                    val jerseyTorsoBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B2242), Color(0xFF061326))
                    )
                    val leftScrimBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xEE050E1E),
                            Color(0xAA06152B),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = w * 0.68f
                    )
                    val jerseyConfigs = listOf(
                        Triple(w * 0.52f, h * 0.44f, 0.78f),
                        Triple(w * 0.63f, h * 0.40f, 0.86f),
                        Triple(w * 0.76f, h * 0.34f, 1.0f),
                        Triple(w * 0.89f, h * 0.42f, 0.84f)
                    )
                    val baseJw = 46.dp.toPx()
                    val baseHeadR = 10.dp.toPx()
                    val stripeStartOffset = 14.dp.toPx()
                    val stripeEndOffset = 34.dp.toPx()
                    val baseStroke = 3.dp.toPx()

                    onDrawBehind {
                        drawRect(brush = skyBrush)
                        drawCircle(
                            brush = spotlightBrush,
                            center = Offset(w * 0.72f, h * 0.22f),
                            radius = w * 0.55f
                        )
                        for ((cx, topY, scaleFactor) in jerseyConfigs) {
                            val jw = baseJw * scaleFactor
                            val jh = h - topY
                            drawCircle(
                                color = Color(0xFF1A3A63),
                                radius = baseHeadR * scaleFactor,
                                center = Offset(cx, topY - baseHeadR * scaleFactor)
                            )
                            drawRoundRect(
                                brush = jerseyTorsoBrush,
                                topLeft = Offset(cx - jw / 2f, topY),
                                size = Size(jw, jh),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                            drawLine(
                                color = Color(0xFFE5C158).copy(alpha = 0.7f),
                                start = Offset(cx, topY + stripeStartOffset),
                                end = Offset(cx, topY + stripeEndOffset),
                                strokeWidth = baseStroke * scaleFactor
                            )
                        }
                        drawRect(brush = leftScrimBrush)
                    }
                }
        ) {

            // Left Typography Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Together\nWe Play\nStronger",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = teamName,
                    color = Color(0xFFB8D8F8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Bottom-Right Glowing Cyan Circle Arrow Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF56ECFF), Color(0xFF1BC6F2))
                        )
                    )
                    .border(1.dp, Color(0xFFB5F6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Start Match",
                    tint = Color(0xFF041022),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens
    InteractiveGlassCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D2648))
                    .border(1.dp, tokens.accentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tokens.accentGold,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UpcomingFixtureBannerCard(
    homeTeam: String,
    awayTeam: String,
    dateTimeText: String,
    venueText: String,
    statusBadge: String? = null,
    onClick: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    InteractiveGlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = tokens.glassCardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0E2647))
                        .border(1.dp, Color(0xFF22558C), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    TeamShieldBadge(teamName = homeTeam, size = 36.dp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = homeTeam,
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "   vs   ",
                            color = tokens.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = awayTeam,
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateTimeText,
                        color = tokens.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = tokens.accentGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = venueText,
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                TeamShieldBadge(teamName = awayTeam, size = 36.dp)
                if (!statusBadge.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusBadge,
                        color = tokens.accentGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TopStoryCard(
    headline: String,
    timeLabel: String,
    photoUri: String?,
    isTeamGraphic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens
    InteractiveGlassCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            // Top Visual Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isTeamGraphic) {
                                listOf(Color(0xFF10325C), Color(0xFF091B34))
                            } else {
                                listOf(Color(0xFF0E2A4F), Color(0xFF08172E))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = headline,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    RcbCrestLogo(size = 50.dp)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = headline,
                    color = tokens.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(tokens.accentGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeLabel,
                        color = tokens.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MatchCardItem(
    match: MatchEntity,
    onClick: () -> Unit
) {
    val dateStr = remember(match.matchDateMillis) {
        SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.US).format(Date(match.matchDateMillis))
    }
    val statusBadge = when {
        !match.winnerTeam.isNullOrBlank() -> "Completed"
        match.isCompleted -> "Completed"
        else -> "Live"
    }
    UpcomingFixtureBannerCard(
        homeTeam = match.homeTeamName,
        awayTeam = match.awayTeamName,
        dateTimeText = dateStr,
        venueText = "${match.venue} (${match.format.name})",
        statusBadge = statusBadge,
        onClick = onClick
    )
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
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}
