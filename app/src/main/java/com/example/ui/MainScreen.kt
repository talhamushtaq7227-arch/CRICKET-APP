package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
        BottomNavItem(Screen.Rankings, Icons.Default.EmojiEvents, "Rankings"),
        BottomNavItem(Screen.Settings, Icons.Default.Settings, "Settings")
    )

    // Hide bottom bar on live scoring and setup screen for maximum focus and camera area
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
                // iOS Floating Liquid Glass Capsule Dock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = if (tokens.isDark) 16.dp else 12.dp,
                                shape = RoundedCornerShape(32.dp),
                                ambientColor = tokens.cardShadowColor,
                                spotColor = tokens.cardShadowColor
                            )
                            .clip(RoundedCornerShape(32.dp))
                            .background(tokens.navDockBackground)
                            .border(1.5.dp, tokens.navDockBorder, RoundedCornerShape(32.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        // Top Specular Refraction Highlight
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(tokens.glassSpecularTop, Color.Transparent),
                                        startY = 0f,
                                        endY = 32f
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = currentRoute == item.screen.route
                                FloatingDockItem(
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
                                    }
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
                    startDestination = Screen.Home.route
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
                            onNavigateToRankings = { navController.navigate(Screen.Rankings.route) }
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
            }
        }
    }
}

/**
 * Individual item inside the iOS Floating Liquid Glass Capsule Dock.
 */
@Composable
fun FloatingDockItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "nav_item_scale"
    )

    val pillWidth by animateDpAsState(
        targetValue = if (selected) 84.dp else 48.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "nav_pill_width"
    )

    val pillBg by animateColorAsState(
        targetValue = if (selected) tokens.navPillBackground else Color.Transparent,
        animationSpec = tween(250),
        label = "nav_pill_bg"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .height(44.dp)
            .width(pillWidth)
            .clip(RoundedCornerShape(22.dp))
            .background(pillBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) tokens.navPillContent else tokens.navUnselectedContent,
                modifier = Modifier.size(20.dp)
            )
            if (selected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = tokens.navPillContent,
                    maxLines = 1
                )
            }
        }
    }
}
