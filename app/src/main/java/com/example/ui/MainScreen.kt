package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.CricketAnimationType
import com.example.ui.components.CricketCelebrationOverlay
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.RcbCrestLogo
import com.example.ui.components.liquid3DEntrance
import com.example.ui.navigation.Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveScoringScreen
import com.example.ui.screens.MatchSetupScreen
import com.example.ui.screens.MatchesListScreen
import com.example.ui.screens.PlayerComparisonScreen
import com.example.ui.screens.PlayerProfileScreen
import com.example.ui.screens.RankingsScreen
import com.example.ui.screens.ScorecardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SquadScreen
import com.example.ui.screens.VideoExportScreen
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainScreen(viewModel: CricketViewModel = viewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val tokens = LiquidGlassTheme.tokens
    var activeCricketAnimation by remember { mutableStateOf<CricketAnimationType?>(null) }
    var showSplashOverlay by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(950)
        showSplashOverlay = false
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cricketAnimationEvent.collectLatest { event ->
            activeCricketAnimation = event
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, Icons.Default.Home, "Home"),
        BottomNavItem(Screen.Squad, Icons.Default.Group, "Squad"),
        BottomNavItem(Screen.Matches, Icons.Default.SportsCricket, "Matches"),
        BottomNavItem(Screen.Rankings, Icons.Default.BarChart, "Stats"),
        BottomNavItem(Screen.Settings, Icons.Default.MoreHoriz, "More")
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Squad.route,
        Screen.Matches.route,
        Screen.Rankings.route,
        Screen.Settings.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
                )
            ) {
                // Coordinated Midnight Navy Bottom Navigation with Glowing Cyan Active Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 14.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = tokens.cardShadowColor,
                                spotColor = tokens.cardShadowColor
                            )
                            .clip(RoundedCornerShape(28.dp))
                            .background(tokens.navDockBackground)
                            .border(1.5.dp, tokens.navDockBorder, RoundedCornerShape(28.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = currentRoute == item.screen.route
                                ModernCyanNavItem(
                                    item = item,
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LiquidGlassBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(220)) +
                            scaleIn(initialScale = 0.97f, animationSpec = tween(220))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(160))
                    }
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToMatchSetup = { navController.navigate(Screen.MatchSetup.route) },
                            onNavigateToLiveScoring = { matchId ->
                                navController.navigate(Screen.LiveScoring.createRoute(matchId))
                            },
                            onNavigateToScorecard = { matchId ->
                                navController.navigate(Screen.Scorecard.createRoute(matchId))
                            },
                            onNavigateToSquad = { navController.navigate(Screen.Squad.route) },
                            onNavigateToRankings = { navController.navigate(Screen.Rankings.route) },
                            onNavigateToMatches = { navController.navigate(Screen.Matches.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                        )
                    }

                    composable(Screen.Squad.route) {
                        SquadScreen(
                            viewModel = viewModel,
                            onNavigateToProfile = { playerId ->
                                navController.navigate(Screen.PlayerProfile.createRoute(playerId))
                            },
                            onNavigateToComparison = { pId ->
                                navController.navigate(Screen.PlayerComparison.createRoute(pId))
                            }
                        )
                    }

                    composable(Screen.Matches.route) {
                        MatchesListScreen(
                            viewModel = viewModel,
                            onNavigateToMatchSetup = { navController.navigate(Screen.MatchSetup.route) },
                            onNavigateToLiveScoring = { matchId ->
                                navController.navigate(Screen.LiveScoring.createRoute(matchId))
                            },
                            onNavigateToScorecard = { matchId ->
                                navController.navigate(Screen.Scorecard.createRoute(matchId))
                            }
                        )
                    }

                    composable(Screen.Rankings.route) {
                        RankingsScreen(
                            viewModel = viewModel,
                            onNavigateToProfile = { playerId ->
                                navController.navigate(Screen.PlayerProfile.createRoute(playerId))
                            },
                            onNavigateToComparison = { pId ->
                                navController.navigate(Screen.PlayerComparison.createRoute(pId))
                            }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(viewModel = viewModel)
                    }

                    composable(Screen.MatchSetup.route) {
                        MatchSetupScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onMatchCreated = { matchId ->
                                navController.navigate(Screen.LiveScoring.createRoute(matchId)) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        )
                    }

                    composable(
                        route = Screen.LiveScoring.route,
                        arguments = listOf(navArgument("matchId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
                        LiveScoringScreen(
                            matchId = matchId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToScorecard = { id ->
                                navController.navigate(Screen.Scorecard.createRoute(id))
                            },
                            onNavigateToVideoExport = { id ->
                                navController.navigate(Screen.VideoExport.createRoute(id))
                            }
                        )
                    }

                    composable(
                        route = Screen.Scorecard.route,
                        arguments = listOf(navArgument("matchId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
                        ScorecardScreen(
                            matchId = matchId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onResumeScoring = { id ->
                                navController.navigate(Screen.LiveScoring.createRoute(id))
                            },
                            onExportVideo = { id ->
                                navController.navigate(Screen.VideoExport.createRoute(id))
                            }
                        )
                    }

                    composable(
                        route = Screen.VideoExport.route,
                        arguments = listOf(navArgument("matchId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val matchId = backStackEntry.arguments?.getLong("matchId") ?: 0L
                        VideoExportScreen(
                            matchId = matchId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Screen.PlayerProfile.route,
                        arguments = listOf(navArgument("playerId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val playerId = backStackEntry.arguments?.getLong("playerId") ?: 0L
                        PlayerProfileScreen(
                            playerId = playerId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToScorecard = { matchId ->
                                navController.navigate(Screen.Scorecard.createRoute(matchId))
                            },
                            onNavigateToComparison = { pId ->
                                navController.navigate(Screen.PlayerComparison.createRoute(pId))
                            }
                        )
                    }

                    composable(
                        route = Screen.PlayerComparison.route,
                        arguments = listOf(
                            navArgument("player1Id") {
                                type = NavType.LongType
                                defaultValue = 0L
                            },
                            navArgument("player2Id") {
                                type = NavType.LongType
                                defaultValue = 0L
                            }
                        )
                    ) { backStackEntry ->
                        val p1 = backStackEntry.arguments?.getLong("player1Id") ?: 0L
                        val p2 = backStackEntry.arguments?.getLong("player2Id") ?: 0L
                        PlayerComparisonScreen(
                            initialPlayer1Id = p1,
                            initialPlayer2Id = p2,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToProfile = { pId ->
                                navController.navigate(Screen.PlayerProfile.createRoute(pId))
                            }
                        )
                    }
                }

                CricketCelebrationOverlay(
                    event = activeCricketAnimation,
                    onDismiss = { activeCricketAnimation = null }
                )

                // Animated Splash Screen Overlay matching the reference branding panel
                AnimatedVisibility(
                    visible = showSplashOverlay,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(260))
                ) {
                    RcbSplashOverlay(onDismiss = { showSplashOverlay = false })
                }
            }
        }
    }
}

/**
 * Rajpoot Cricket Boys Splash Screen Overlay matching the reference image branding panel.
 */
@Composable
fun RcbSplashOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040A16),
                        Color(0xFF07152B),
                        Color(0xFF050C1A)
                    )
                )
            )
            .clickable(onClick = onDismiss)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.liquid3DEntrance(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RcbCrestLogo(size = 104.dp)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Rajpoot\nCricket Boys",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "One Team. One Dream.\nOne Rajpoot Family.",
                color = Color(0xFF9AB8D9),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF29E2FF))
            )
        }

        Text(
            text = "CRICKET   /   TEAM   /   PASSION   /   BROTHERHOOD",
            color = Color(0xFF5E80A6),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * Bottom Navigation Item matching the reference image:
 * Icon stacked above Label; when selected, wrapped in a glowing bright cyan rounded pill.
 */
@Composable
fun ModernCyanNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "nav_item_scale"
    )

    val activeBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF66F0FF), Color(0xFF20CEFA))
    )

    Box(
        modifier = modifier
            .scale(scale)
            .padding(horizontal = 3.dp)
            .height(50.dp)
            .let { base ->
                if (selected) {
                    base
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x8829E2FF),
                            spotColor = Color(0x8829E2FF)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(activeBrush)
                        .border(1.dp, Color(0xFFB5F6FF), RoundedCornerShape(20.dp))
                } else {
                    base.clip(RoundedCornerShape(20.dp))
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) Color(0xFF041022) else tokens.navUnselectedContent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (selected) Color(0xFF041022) else tokens.navUnselectedContent,
                maxLines = 1
            )
        }
    }
}
