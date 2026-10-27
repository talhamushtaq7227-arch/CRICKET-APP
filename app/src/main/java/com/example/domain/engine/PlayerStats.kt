package com.example.domain.engine

import androidx.compose.runtime.Immutable
import java.util.Locale

@Immutable
data class PlayerStats(
    val playerId: Long? = null,
    val playerName: String,
    val matchesPlayed: Int = 0,
    val inningsBatted: Int = 0,
    val runs: Int = 0,
    val highestScore: Int = 0,
    val notOuts: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val ballsFaced: Int = 0,
    val legalBallsBowled: Int = 0,
    val wickets: Int = 0,
    val runsConceded: Int = 0,
    val bestBowlingWickets: Int = 0,
    val bestBowlingRuns: Int = 0
) {
    val battingAverage: Double?
        get() {
            val dismissals = inningsBatted - notOuts
            return if (dismissals > 0) runs.toDouble() / dismissals else if (runs > 0) runs.toDouble() else null
        }

    val formattedBattingAverage: String
        get() = battingAverage?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

    val strikeRate: Double
        get() = if (ballsFaced > 0) (runs.toDouble() / ballsFaced) * 100.0 else 0.0

    val formattedStrikeRate: String
        get() = if (ballsFaced > 0) String.format(Locale.US, "%.1f", strikeRate) else "—"

    val fullOversBowled: Int get() = legalBallsBowled / 6
    val ballsInOver: Int get() = legalBallsBowled % 6
    val formattedOversBowled: String get() = "$fullOversBowled.$ballsInOver"

    val bowlingEconomy: Double?
        get() = if (legalBallsBowled > 0) (runsConceded.toDouble() / legalBallsBowled) * 6.0 else null

    val formattedBowlingEconomy: String
        get() = bowlingEconomy?.let { String.format(Locale.US, "%.2f", it) } ?: "—"

    val bestBowlingFigures: String
        get() = if (bestBowlingWickets > 0 || bestBowlingRuns > 0) "$bestBowlingWickets/$bestBowlingRuns" else "—"
}
