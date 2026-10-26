package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.navigation.Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveScoringScreen
import com.example.ui.screens.MatchSetupScreen
import com.example.ui.screens.MatchesListScreen
import com.example.ui.screens.RankingsScreen
import com.example.ui.screens.ScorecardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SquadScreen
import com.example.ui.screens.VideoExportScreen
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
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

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
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
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = CharcoalDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
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
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) CharcoalBlack else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    color = if (selected) GoldBright else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GoldPrimary,
                                selectedIconColor = CharcoalBlack,
                                unselectedIconColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
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
                    SquadScreen(viewModel = viewModel)
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
                    RankingsScreen(viewModel = viewModel)
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
            }
        }
    }
}
