package com.example.domain.engine

import androidx.compose.runtime.Immutable
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.ExtrasType
import com.example.data.model.MatchEntity
import com.example.data.model.PlayerEntity
import com.example.data.model.WicketType
import java.util.Locale

/**
 * Individual performance of a player in a single match.
 */
@Immutable
data class PlayerMatchPerformance(
    val matchId: Long,
    val matchTitle: String,
    val tournamentName: String,
    val season: String,
    val matchDateMillis: Long,
    val opponent: String,
    // Batting in match
    val didBat: Boolean,
    val runsScored: Int,
    val ballsFaced: Int,
    val fours: Int,
    val sixes: Int,
    val isOut: Boolean,
    val dismissalText: String,
    val battingStrikeRate: Double?,
    // Bowling in match
    val didBowl: Boolean,
    val legalBallsBowled: Int,
    val oversFormatted: String,
    val maidens: Int,
    val runsConceded: Int,
    val wicketsTaken: Int,
    val bowlingFigures: String,
    val economyRate: Double?,
    // Fielding in match
    val catches: Int,
    val stumpings: Int,
    val runOuts: Int,
    // Result
    val matchResult: String
) {
    val formattedBattingScore: String
        get() = if (didBat) "$runsScored${if (!isOut) "*" else ""} ($ballsFaced)" else "DNB"

    val formattedBattingSR: String
        get() = battingStrikeRate?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

    val formattedBowlingSummary: String
        get() = if (didBowl) "$oversFormatted-$maidens-$runsConceded-$wicketsTaken" else "DNB"
}

/**
 * Comprehensive professional cricket statistics for a player.
 */
@Immutable
data class PlayerDetailedStatistics(
    val player: PlayerEntity,
    val seasonFilter: String? = null,
    val tournamentFilter: String? = null,
    // General
    val matchesPlayed: Int = 0,
    // Batting
    val inningsBatted: Int = 0,
    val notOuts: Int = 0,
    val totalRuns: Int = 0,
    val highestScore: Int = 0,
    val isHighestScoreNotOut: Boolean = false,
    val battingAverage: Double? = null,
    val ballsFaced: Int = 0,
    val strikeRate: Double? = null,
    val fours: Int = 0,
    val sixes: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val ducks: Int = 0,
    // Bowling
    val matchesBowled: Int = 0,
    val legalBallsBowled: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val bowlingAverage: Double? = null,
    val economyRate: Double? = null,
    val bowlingStrikeRate: Double? = null,
    val bestBowlingWickets: Int = 0,
    val bestBowlingRuns: Int = 0,
    val maidens: Int = 0,
    val dotBalls: Int = 0,
    // Fielding
    val catches: Int = 0,
    val stumpings: Int = 0,
    val runOuts: Int = 0,
    // Match Performances (newest first)
    val matchPerformances: List<PlayerMatchPerformance> = emptyList()
) {
    val highestScoreFormatted: String
        get() = if (inningsBatted > 0) "$highestScore${if (isHighestScoreNotOut) "*" else ""}" else "0"

    val formattedBattingAverage: String
        get() = battingAverage?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"

    val formattedStrikeRate: String
        get() = strikeRate?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"

    val fullOversBowled: Int get() = legalBallsBowled / 6
    val ballsInCurrentOver: Int get() = legalBallsBowled % 6
    val formattedOversBowled: String get() = "$fullOversBowled.$ballsInCurrentOver"

    val formattedBowlingAverage: String
        get() = bowlingAverage?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"

    val formattedEconomyRate: String
        get() = economyRate?.let { String.format(Locale.US, "%.2f", it) } ?: "N/A"

    val formattedBowlingStrikeRate: String
        get() = bowlingStrikeRate?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"

    val bestBowlingFigures: String
        get() = if (bestBowlingWickets > 0 || bestBowlingRuns > 0) "$bestBowlingWickets/$bestBowlingRuns" else "—"

    val dismissals: Int get() = inningsBatted - notOuts

    // Chart trend series
    val runsTrend: List<Pair<String, Int>>
        get() = matchPerformances.filter { it.didBat }.reversed().map { it.opponent to it.runsScored }

    val wicketsTrend: List<Pair<String, Int>>
        get() = matchPerformances.filter { it.didBowl }.reversed().map { it.opponent to it.wicketsTaken }

    val strikeRateTrend: List<Pair<String, Double>>
        get() = matchPerformances.filter { it.didBat && it.battingStrikeRate != null }.reversed().map { it.opponent to it.battingStrikeRate!! }

    val economyTrend: List<Pair<String, Double>>
        get() = matchPerformances.filter { it.didBowl && it.economyRate != null }.reversed().map { it.opponent to it.economyRate!! }
}

/**
 * Squad ranking item with value and rank.
 */
@Immutable
data class SquadRankingItem(
    val rank: Int,
    val player: PlayerEntity,
    val primaryValue: String,
    val secondaryValue: String,
    val sortKey: Double
)

object PlayerStatisticsCalculator {

    /**
     * Calculate detailed statistics for a specific player across all or filtered matches.
     */
    fun calculatePlayerStatistics(
        player: PlayerEntity,
        matches: List<MatchEntity>,
        deliveries: List<BallDeliveryEntity>,
        seasonFilter: String? = null,
        tournamentFilter: String? = null
    ): PlayerDetailedStatistics {
        // Filter matches by season / tournament if specified
        val validMatches = matches.filter { match ->
            val seasonMatches = seasonFilter.isNullOrBlank() || match.season.equals(seasonFilter, ignoreCase = true)
            val tournamentMatches = tournamentFilter.isNullOrBlank() || match.tournamentName.equals(tournamentFilter, ignoreCase = true)
            seasonMatches && tournamentMatches
        }
        val matchMap = validMatches.associateBy { it.id }

        // Filter deliveries belonging only to valid matches
        val validDeliveries = deliveries.filter { it.matchId in matchMap }

        // Group deliveries by matchId (to guarantee each match/innings is evaluated once)
        val deliveriesByMatch = validDeliveries.groupBy { it.matchId }

        var totalRuns = 0
        var ballsFaced = 0
        var fours = 0
        var sixes = 0
        var fifties = 0
        var hundreds = 0
        var ducks = 0
        var inningsBatted = 0
        var notOuts = 0
        var highestScore = 0
        var isHighestScoreNotOut = false

        var matchesBowledCount = 0
        var legalBallsBowled = 0
        var runsConceded = 0
        var bowlerWickets = 0
        var bestBowlingW = 0
        var bestBowlingR = 0
        var totalMaidens = 0
        var totalDotBalls = 0

        var totalCatches = 0
        var totalStumpings = 0
        var totalRunOuts = 0

        val matchPerformances = mutableListOf<PlayerMatchPerformance>()
        val matchesPlayedSet = mutableSetOf<Long>()

        for (match in validMatches.sortedByDescending { it.matchDateMillis }) {
            val mDeliveries = deliveriesByMatch[match.id] ?: emptyList()

            // Check if player participated in this match
            val isStrikerInMatch = mDeliveries.any { isPlayerNameOrId(player, it.strikerName, it.strikerPlayerId) }
            val isNonStrikerInMatch = mDeliveries.any { isPlayerNameOrId(player, it.nonStrikerName, it.nonStrikerPlayerId) }
            val isBowlerInMatch = mDeliveries.any { isPlayerNameOrId(player, it.bowlerName, it.bowlerPlayerId) }
            val isFielderInMatch = mDeliveries.any { isPlayerNameOrId(player, it.fielderPlayerName, it.fielderPlayerId) }
            val isDismissedInMatch = mDeliveries.any { it.isWicket && isPlayerNameOrId(player, it.dismissedPlayerName, it.dismissedPlayerId) }
            val inSquadList = match.homeSquadPlayerIds.split(",").mapNotNull { it.trim().toLongOrNull() }.contains(player.id)

            val didParticipate = isStrikerInMatch || isNonStrikerInMatch || isBowlerInMatch || isFielderInMatch || isDismissedInMatch || inSquadList
            if (didParticipate) {
                matchesPlayedSet.add(match.id)
            }

            // Batting performance in this match
            val playerBattingBalls = mDeliveries.filter { isPlayerNameOrId(player, it.strikerName, it.strikerPlayerId) }
            val didBat = isStrikerInMatch || isNonStrikerInMatch || isDismissedInMatch

            var mRuns = 0
            var mBallsFaced = 0
            var mFours = 0
            var mSixes = 0
            var mIsOut = false
            var mDismissalText = "not out"

            if (didBat) {
                inningsBatted++
                mRuns = playerBattingBalls.sumOf { it.runsOffBat }
                // Wides do not count as balls faced by the batter
                mBallsFaced = playerBattingBalls.count { it.extrasType != ExtrasType.WIDE }
                mFours = playerBattingBalls.count { it.runsOffBat == 4 }
                mSixes = playerBattingBalls.count { it.runsOffBat == 6 }

                val dismissalDelivery = mDeliveries.firstOrNull { it.isWicket && isPlayerNameOrId(player, it.dismissedPlayerName, it.dismissedPlayerId) }
                if (dismissalDelivery != null) {
                    mIsOut = true
                    mDismissalText = formatDismissal(dismissalDelivery)
                    if (mRuns == 0) {
                        ducks++
                    }
                } else {
                    notOuts++
                }

                totalRuns += mRuns
                ballsFaced += mBallsFaced
                fours += mFours
                sixes += mSixes
                if (mRuns in 50..99) fifties++
                if (mRuns >= 100) hundreds++

                if (mRuns > highestScore) {
                    highestScore = mRuns
                    isHighestScoreNotOut = !mIsOut
                } else if (mRuns == highestScore && !mIsOut) {
                    // Prefer not out for identical score
                    isHighestScoreNotOut = true
                }
            }

            // Bowling performance in this match
            val playerBowlingBalls = mDeliveries.filter { isPlayerNameOrId(player, it.bowlerName, it.bowlerPlayerId) }
            val didBowl = playerBowlingBalls.isNotEmpty()

            var mLegalBalls = 0
            var mRunsConceded = 0
            var mWickets = 0
            var mMaidens = 0

            if (didBowl) {
                matchesBowledCount++
                mLegalBalls = playerBowlingBalls.count { it.isLegalDelivery }
                // Only runs off bat, wides, no-balls and penalties are charged to the bowler
                // Byes and leg-byes are EXCLUDED
                mRunsConceded = playerBowlingBalls.sumOf { ball ->
                    ball.runsOffBat + when (ball.extrasType) {
                        ExtrasType.WIDE, ExtrasType.NO_BALL, ExtrasType.PENALTY -> ball.extrasRuns
                        else -> 0
                    }
                }
                // Only bowler wickets (bowled, caught, lbw, stumped, hit wicket)
                mWickets = playerBowlingBalls.count { it.isWicket && it.wicketType.isBowlerWicket }

                // Calculate maidens in this match for this bowler
                val oversGrouped = playerBowlingBalls.groupBy { "${it.inningsNumber}_${it.overNumber}" }
                for ((_, overBalls) in oversGrouped) {
                    val legalInOver = overBalls.count { it.isLegalDelivery }
                    val runsInOver = overBalls.sumOf { b ->
                        b.runsOffBat + when (b.extrasType) {
                            ExtrasType.WIDE, ExtrasType.NO_BALL, ExtrasType.PENALTY -> b.extrasRuns
                            else -> 0
                        }
                    }
                    if (legalInOver == 6 && runsInOver == 0) {
                        mMaidens++
                    }
                }

                // Dot balls in this match
                val mDots = playerBowlingBalls.count { b ->
                    val chargedExtras = when (b.extrasType) {
                        ExtrasType.WIDE, ExtrasType.NO_BALL, ExtrasType.PENALTY -> b.extrasRuns
                        else -> 0
                    }
                    b.runsOffBat == 0 && chargedExtras == 0
                }

                legalBallsBowled += mLegalBalls
                runsConceded += mRunsConceded
                bowlerWickets += mWickets
                totalMaidens += mMaidens
                totalDotBalls += mDots

                val isBetterBowling = mWickets > bestBowlingW || (mWickets == bestBowlingW && mRunsConceded < bestBowlingR && mWickets > 0)
                if (isBetterBowling) {
                    bestBowlingW = mWickets
                    bestBowlingR = mRunsConceded
                }
            }

            // Fielding contributions in this match
            val mCatches = mDeliveries.count { it.isWicket && it.wicketType == WicketType.CAUGHT && isPlayerNameOrId(player, it.fielderPlayerName, it.fielderPlayerId) }
            val mStumpings = mDeliveries.count { it.isWicket && it.wicketType == WicketType.STUMPED && isPlayerNameOrId(player, it.fielderPlayerName, it.fielderPlayerId) }
            val mRunOuts = mDeliveries.count { it.isWicket && it.wicketType == WicketType.RUN_OUT && isPlayerNameOrId(player, it.fielderPlayerName, it.fielderPlayerId) }

            totalCatches += mCatches
            totalStumpings += mStumpings
            totalRunOuts += mRunOuts

            if (didParticipate || didBat || didBowl || (mCatches + mStumpings + mRunOuts > 0)) {
                val bSR = if (mBallsFaced > 0) (mRuns.toDouble() / mBallsFaced) * 100.0 else null
                val bEcon = if (mLegalBalls > 0) (mRunsConceded.toDouble() / mLegalBalls) * 6.0 else null
                val oversStr = "${mLegalBalls / 6}.${mLegalBalls % 6}"
                val bFigures = if (mWickets > 0 || mRunsConceded > 0) "$mWickets/$mRunsConceded" else "—"

                val resultSummary = match.winMargin ?: if (match.winnerTeam != null) "${match.winnerTeam} won" else "In Progress"

                matchPerformances.add(
                    PlayerMatchPerformance(
                        matchId = match.id,
                        matchTitle = match.matchTitle,
                        tournamentName = match.tournamentName.ifBlank { "General" },
                        season = match.season,
                        matchDateMillis = match.matchDateMillis,
                        opponent = match.awayTeamName.ifBlank { "Opposition" },
                        didBat = didBat,
                        runsScored = mRuns,
                        ballsFaced = mBallsFaced,
                        fours = mFours,
                        sixes = mSixes,
                        isOut = mIsOut,
                        dismissalText = mDismissalText,
                        battingStrikeRate = bSR,
                        didBowl = didBowl,
                        legalBallsBowled = mLegalBalls,
                        oversFormatted = oversStr,
                        maidens = mMaidens,
                        runsConceded = mRunsConceded,
                        wicketsTaken = mWickets,
                        bowlingFigures = bFigures,
                        economyRate = bEcon,
                        catches = mCatches,
                        stumpings = mStumpings,
                        runOuts = mRunOuts,
                        matchResult = resultSummary
                    )
                )
            }
        }

        // Standard cricket formulas:
        // Batting average = total runs / dismissals (excluding not outs). N/A if dismissals == 0
        val dismissals = inningsBatted - notOuts
        val battingAvg = if (dismissals > 0) totalRuns.toDouble() / dismissals else null

        // Strike rate = (total runs / balls faced) * 100. N/A if balls faced == 0
        val strikeRate = if (ballsFaced > 0) (totalRuns.toDouble() / ballsFaced) * 100.0 else null

        // Bowling average = runs conceded / bowler wickets. N/A if wickets == 0
        val bowlingAvg = if (bowlerWickets > 0) runsConceded.toDouble() / bowlerWickets else null

        // Economy rate = (runs conceded / legal balls) * 6.0. N/A if legal balls == 0
        val economy = if (legalBallsBowled > 0) (runsConceded.toDouble() / legalBallsBowled) * 6.0 else null

        // Bowling strike rate = legal balls / bowler wickets. N/A if wickets == 0
        val bowlingSR = if (bowlerWickets > 0) legalBallsBowled.toDouble() / bowlerWickets else null

        return PlayerDetailedStatistics(
            player = player,
            seasonFilter = seasonFilter,
            tournamentFilter = tournamentFilter,
            matchesPlayed = matchesPlayedSet.size,
            inningsBatted = inningsBatted,
            notOuts = notOuts,
            totalRuns = totalRuns,
            highestScore = highestScore,
            isHighestScoreNotOut = isHighestScoreNotOut,
            battingAverage = battingAvg,
            ballsFaced = ballsFaced,
            strikeRate = strikeRate,
            fours = fours,
            sixes = sixes,
            fifties = fifties,
            hundreds = hundreds,
            ducks = ducks,
            matchesBowled = matchesBowledCount,
            legalBallsBowled = legalBallsBowled,
            runsConceded = runsConceded,
            wickets = bowlerWickets,
            bowlingAverage = bowlingAvg,
            economyRate = economy,
            bowlingStrikeRate = bowlingSR,
            bestBowlingWickets = bestBowlingW,
            bestBowlingRuns = bestBowlingR,
            maidens = totalMaidens,
            dotBalls = totalDotBalls,
            catches = totalCatches,
            stumpings = totalStumpings,
            runOuts = totalRunOuts,
            matchPerformances = matchPerformances
        )
    }

    /**
     * Compute squad rankings for a specific category with qualification rules.
     */
    fun computeSquadRankings(
        categoryIndex: Int,
        allPlayerStats: List<PlayerDetailedStatistics>
    ): List<SquadRankingItem> {
        val filtered = allPlayerStats.filter { it.matchesPlayed > 0 || it.inningsBatted > 0 || it.matchesBowled > 0 }

        return when (categoryIndex) {
            0 -> { // Most Runs
                filtered.sortedByDescending { it.totalRuns }
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = "${s.totalRuns} runs",
                            secondaryValue = "${s.inningsBatted} inn • HS ${s.highestScoreFormatted} • Avg ${s.formattedBattingAverage}",
                            sortKey = s.totalRuns.toDouble()
                        )
                    }
            }
            1 -> { // Most Wickets
                filtered.sortedByDescending { it.wickets }
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = "${s.wickets} wkts",
                            secondaryValue = "Econ ${s.formattedEconomyRate} • Best ${s.bestBowlingFigures} • ${s.formattedOversBowled} ov",
                            sortKey = s.wickets.toDouble()
                        )
                    }
            }
            2 -> { // Highest Individual Score
                filtered.sortedWith(compareByDescending<PlayerDetailedStatistics> { it.highestScore }.thenByDescending { if (it.isHighestScoreNotOut) 1 else 0 })
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = s.highestScoreFormatted,
                            secondaryValue = "${s.totalRuns} total runs • SR ${s.formattedStrikeRate}",
                            sortKey = s.highestScore.toDouble()
                        )
                    }
            }
            3 -> { // Best Batting Average (Min 1 dismissal required to qualify)
                filtered.filter { it.battingAverage != null && it.dismissals >= 1 }
                    .sortedByDescending { it.battingAverage }
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = s.formattedBattingAverage,
                            secondaryValue = "${s.totalRuns} runs • ${s.inningsBatted} inn (${s.notOuts} NO)",
                            sortKey = s.battingAverage ?: 0.0
                        )
                    }
            }
            4 -> { // Best Batting Strike Rate (Min 10 balls faced to qualify)
                filtered.filter { it.strikeRate != null && it.ballsFaced >= 10 }
                    .sortedByDescending { it.strikeRate }
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = s.formattedStrikeRate,
                            secondaryValue = "${s.totalRuns} runs off ${s.ballsFaced} balls",
                            sortKey = s.strikeRate ?: 0.0
                        )
                    }
            }
            5 -> { // Best Bowling Figures
                filtered.filter { it.bestBowlingWickets > 0 }
                    .sortedWith(compareByDescending<PlayerDetailedStatistics> { it.bestBowlingWickets }.thenBy { it.bestBowlingRuns })
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = s.bestBowlingFigures,
                            secondaryValue = "${s.wickets} total wkts • Econ ${s.formattedEconomyRate}",
                            sortKey = s.bestBowlingWickets.toDouble()
                        )
                    }
            }
            6 -> { // Best Bowling Economy (Min 6 legal balls / 1 over bowled)
                filtered.filter { it.economyRate != null && it.legalBallsBowled >= 6 }
                    .sortedBy { it.economyRate }
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = s.formattedEconomyRate,
                            secondaryValue = "${s.runsConceded} runs in ${s.formattedOversBowled} overs • ${s.wickets} wkts",
                            sortKey = -(s.economyRate ?: 999.0)
                        )
                    }
            }
            7 -> { // Most Fours
                filtered.sortedByDescending { it.fours }
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = "${s.fours} fours",
                            secondaryValue = "${s.totalRuns} runs • ${s.sixes} sixes",
                            sortKey = s.fours.toDouble()
                        )
                    }
            }
            else -> { // Most Sixes
                filtered.sortedByDescending { it.sixes }
                    .mapIndexed { index, s ->
                        SquadRankingItem(
                            rank = index + 1,
                            player = s.player,
                            primaryValue = "${s.sixes} sixes",
                            secondaryValue = "${s.totalRuns} runs • ${s.fours} fours",
                            sortKey = s.sixes.toDouble()
                        )
                    }
            }
        }
    }

    private fun isPlayerNameOrId(player: PlayerEntity, name: String?, id: Long?): Boolean {
        if (id != null && id == player.id) return true
        if (!name.isNullOrBlank() && name.equals(player.fullName, ignoreCase = true)) return true
        return false
    }

    private fun formatDismissal(ball: BallDeliveryEntity): String {
        return when (ball.wicketType) {
            WicketType.BOWLED -> "b ${ball.bowlerName}"
            WicketType.CAUGHT -> {
                val fielder = ball.fielderPlayerName
                if (!fielder.isNullOrBlank() && !fielder.equals(ball.bowlerName, ignoreCase = true)) {
                    "c $fielder b ${ball.bowlerName}"
                } else if (!fielder.isNullOrBlank()) {
                    "c & b ${ball.bowlerName}"
                } else {
                    "c ? b ${ball.bowlerName}"
                }
            }
            WicketType.LBW -> "lbw b ${ball.bowlerName}"
            WicketType.STUMPED -> {
                val keeper = ball.fielderPlayerName ?: "wk"
                "st $keeper b ${ball.bowlerName}"
            }
            WicketType.RUN_OUT -> {
                val fielder = ball.fielderPlayerName
                if (!fielder.isNullOrBlank()) "run out ($fielder)" else "run out"
            }
            WicketType.HIT_WICKET -> "hit wicket b ${ball.bowlerName}"
            WicketType.RETIRED_HURT -> "retired hurt"
            WicketType.RETIRED_OUT -> "retired out"
            WicketType.TIMED_OUT -> "timed out"
            WicketType.OBSTRUCTING_FIELD -> "obstructing field"
            WicketType.NONE -> "not out"
        }
    }
}
