package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Squad : Screen("squad", "Squad")
    object Matches : Screen("matches", "Matches")
    object Rankings : Screen("rankings", "Rankings")
    object Settings : Screen("settings", "Settings")
    object MatchSetup : Screen("match_setup", "New Match")
    object LiveScoring : Screen("live_scoring/{matchId}", "Live Scoring") {
        fun createRoute(matchId: Long) = "live_scoring/$matchId"
    }
    object Scorecard : Screen("scorecard/{matchId}", "Scorecard") {
        fun createRoute(matchId: Long) = "scorecard/$matchId"
    }
    object VideoExport : Screen("video_export/{matchId}", "Export Video") {
        fun createRoute(matchId: Long) = "video_export/$matchId"
    }
    object PlayerProfile : Screen("player_profile/{playerId}", "Player Profile") {
        fun createRoute(playerId: Long) = "player_profile/$playerId"
    }
    object PlayerComparison : Screen("player_comparison?player1Id={player1Id}&player2Id={player2Id}", "Player Comparison") {
        fun createRoute(player1Id: Long = 0L, player2Id: Long = 0L) =
            "player_comparison?player1Id=$player1Id&player2Id=$player2Id"
    }
}
