package com.example.domain.engine

import com.example.data.model.BallDeliveryEntity
import com.example.data.model.WicketType
import java.util.Locale

data class BatterInningsStats(
    val playerName: String,
    val playerId: Long? = null,
    val runs: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val isOut: Boolean = false,
    val dismissalText: String = "not out",
    val dismissalType: WicketType = WicketType.NONE,
    val bowlerName: String? = null,
    val fielderName: String? = null
) {
    val strikeRate: Double
        get() = if (ballsFaced > 0) (runs.toDouble() / ballsFaced) * 100.0 else 0.0

    val formattedStrikeRate: String
        get() = String.format(Locale.US, "%.1f", strikeRate)
}

data class BowlerInningsStats(
    val bowlerName: String,
    val bowlerId: Long? = null,
    val legalBallsBowled: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0
) {
    val fullOvers: Int get() = legalBallsBowled / 6
    val ballsInOver: Int get() = legalBallsBowled % 6
    val formattedOvers: String get() = "$fullOvers.$ballsInOver"

    val economyRate: Double
        get() = if (legalBallsBowled > 0) (runsConceded.toDouble() / legalBallsBowled) * 6.0 else 0.0

    val formattedEconomyRate: String
        get() = String.format(Locale.US, "%.2f", economyRate)
}

data class ExtrasBreakdown(
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0,
    val penalties: Int = 0
) {
    val total: Int get() = wides + noBalls + byes + legByes + penalties

    val summaryText: String
        get() = "$total (w $wides, nb $noBalls, b $byes, lb $legByes, pen $penalties)"
}

data class FallOfWicket(
    val wicketNumber: Int,
    val score: Int,
    val overBall: String,
    val batsmanName: String
)

data class Partnership(
    val runs: Int = 0,
    val balls: Int = 0,
    val batter1Name: String = "",
    val batter1Runs: Int = 0,
    val batter2Name: String = "",
    val batter2Runs: Int = 0
)

data class InningsLiveState(
    val inningsNumber: Int = 1,
    val battingTeam: String = "",
    val bowlingTeam: String = "",
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val legalBalls: Int = 0,
    val totalOversConfigured: Int = 20,
    val maxWickets: Int = 10,
    val currentStriker: BatterInningsStats? = null,
    val currentNonStriker: BatterInningsStats? = null,
    val currentBowler: BowlerInningsStats? = null,
    val recentDeliveries: List<BallDeliveryEntity> = emptyList(),
    val currentOverDeliveries: List<BallDeliveryEntity> = emptyList(),
    val partnership: Partnership = Partnership(),
    val fallOfWickets: List<FallOfWicket> = emptyList(),
    val battingCard: List<BatterInningsStats> = emptyList(),
    val bowlingCard: List<BowlerInningsStats> = emptyList(),
    val extras: ExtrasBreakdown = ExtrasBreakdown(),
    val target: Int? = null,
    val isOverComplete: Boolean = false,
    val isInningsComplete: Boolean = false,
    val isMatchComplete: Boolean = false,
    val resultSummary: String? = null
) {
    val fullOvers: Int get() = legalBalls / 6
    val ballsInCurrentOver: Int get() = legalBalls % 6
    val formattedOvers: String get() = "$fullOvers.$ballsInCurrentOver"

    val currentRunRate: Double
        get() = if (legalBalls > 0) (totalRuns.toDouble() / legalBalls) * 6.0 else 0.0

    val formattedCRR: String
        get() = String.format(Locale.US, "%.2f", currentRunRate)

    val ballsRemaining: Int
        get() = maxOf(0, (totalOversConfigured * 6) - legalBalls)

    val runsRequired: Int?
        get() = target?.let { maxOf(0, it - totalRuns) }

    val requiredRunRate: Double?
        get() {
            val req = runsRequired ?: return null
            if (ballsRemaining <= 0) return 0.0
            return (req.toDouble() / ballsRemaining) * 6.0
        }

    val formattedRRR: String?
        get() = requiredRunRate?.let { String.format(Locale.US, "%.2f", it) }
}
