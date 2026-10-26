package com.example.domain.engine

import com.example.data.model.BallDeliveryEntity
import com.example.data.model.ExtrasType
import com.example.data.model.MatchEntity
import com.example.data.model.WicketType

object CricketRulesEngine {

    fun calculateInningsState(
        match: MatchEntity,
        inningsNumber: Int,
        deliveries: List<BallDeliveryEntity>,
        initialStrikerName: String? = null,
        initialNonStrikerName: String? = null,
        initialBowlerName: String? = null,
        initialStrikerId: Long? = null,
        initialNonStrikerId: Long? = null,
        initialBowlerId: Long? = null
    ): InningsLiveState {
        val battingTeam = if (inningsNumber == 1) {
            match.battingFirstTeam.ifEmpty { match.homeTeamName }
        } else {
            if (match.battingFirstTeam == match.homeTeamName) match.awayTeamName else match.homeTeamName
        }
        val bowlingTeam = if (battingTeam == match.homeTeamName) match.awayTeamName else match.homeTeamName
        val maxWickets = maxOf(1, match.playersPerSide - 1)

        var totalRuns = 0
        var totalWickets = 0
        var legalBalls = 0

        var wides = 0
        var noBalls = 0
        var byes = 0
        var legByes = 0
        var penalties = 0

        // Mutable tracking maps
        val battersMap = mutableMapOf<String, BatterInningsStats>()
        val bowlersMap = mutableMapOf<String, BowlerInningsStats>()
        val fallOfWickets = mutableListOf<FallOfWicket>()

        var currentStrikerName = initialStrikerName ?: ""
        var currentNonStrikerName = initialNonStrikerName ?: ""
        var currentBowlerName = initialBowlerName ?: ""

        var currentStrikerId = initialStrikerId
        var currentNonStrikerId = initialNonStrikerId
        var currentBowlerId = initialBowlerId

        // Ensure initial players exist in maps if provided
        if (currentStrikerName.isNotEmpty()) {
            battersMap[currentStrikerName] = BatterInningsStats(playerName = currentStrikerName, playerId = currentStrikerId)
        }
        if (currentNonStrikerName.isNotEmpty()) {
            battersMap[currentNonStrikerName] = BatterInningsStats(playerName = currentNonStrikerName, playerId = currentNonStrikerId)
        }
        if (currentBowlerName.isNotEmpty()) {
            bowlersMap[currentBowlerName] = BowlerInningsStats(bowlerName = currentBowlerName, bowlerId = currentBowlerId)
        }

        var partRuns = 0
        var partBalls = 0
        var partBatter1Runs = 0
        var partBatter2Runs = 0

        val sortedDeliveries = deliveries
            .filter { it.inningsNumber == inningsNumber }
            .sortedBy { it.ballSequence }

        var legalBallsInCurrentOver = 0
        var isCurrentOverFinished = false

        for (ball in sortedDeliveries) {
            val striker = ball.strikerName.ifEmpty { currentStrikerName }
            val nonStriker = ball.nonStrikerName.ifEmpty { currentNonStrikerName }
            val bowler = ball.bowlerName.ifEmpty { currentBowlerName }

            currentStrikerName = striker
            currentNonStrikerName = nonStriker
            currentBowlerName = bowler
            currentStrikerId = ball.strikerPlayerId ?: currentStrikerId
            currentNonStrikerId = ball.nonStrikerPlayerId ?: currentNonStrikerId
            currentBowlerId = ball.bowlerPlayerId ?: currentBowlerId

            val runsOffBat = ball.runsOffBat
            val totalBallRuns = ball.totalBallRuns
            totalRuns += totalBallRuns
            partRuns += totalBallRuns

            // Tally extras
            when (ball.extrasType) {
                ExtrasType.WIDE -> wides += ball.extrasRuns
                ExtrasType.NO_BALL -> noBalls += ball.extrasRuns
                ExtrasType.BYE -> byes += ball.extrasRuns
                ExtrasType.LEG_BYE -> legByes += ball.extrasRuns
                ExtrasType.PENALTY -> penalties += ball.extrasRuns
                ExtrasType.NONE -> {}
            }

            // Legal delivery advances legal ball count
            val isLegal = ball.isLegalDelivery
            if (isLegal) {
                legalBalls++
                legalBallsInCurrentOver++
                partBalls++
            }

            // Striker stats
            val currentBatterStats = battersMap[striker] ?: BatterInningsStats(
                playerName = striker,
                playerId = ball.strikerPlayerId
            )

            // Striker faces ball on legal deliveries and no-balls (not wides)
            val strikerFaced = if (ball.extrasType != ExtrasType.WIDE) 1 else 0
            val isFour = if (runsOffBat == 4) 1 else 0
            val isSix = if (runsOffBat == 6) 1 else 0

            battersMap[striker] = currentBatterStats.copy(
                runs = currentBatterStats.runs + runsOffBat,
                ballsFaced = currentBatterStats.ballsFaced + strikerFaced,
                fours = currentBatterStats.fours + isFour,
                sixes = currentBatterStats.sixes + isSix
            )

            if (striker == currentStrikerName) {
                partBatter1Runs += runsOffBat
            } else {
                partBatter2Runs += runsOffBat
            }

            // Bowler stats
            val currentBowlerStats = bowlersMap[bowler] ?: BowlerInningsStats(
                bowlerName = bowler,
                bowlerId = ball.bowlerPlayerId
            )

            // Bowler concedes runs off bat + wides + no balls + penalty (NOT byes or leg byes)
            val bowlerRunsConceded = runsOffBat + when (ball.extrasType) {
                ExtrasType.WIDE, ExtrasType.NO_BALL, ExtrasType.PENALTY -> ball.extrasRuns
                ExtrasType.BYE, ExtrasType.LEG_BYE, ExtrasType.NONE -> 0
            }

            val bowlerLegalBalls = if (isLegal) 1 else 0
            val bowlerWides = if (ball.extrasType == ExtrasType.WIDE) ball.extrasRuns else 0
            val bowlerNoBalls = if (ball.extrasType == ExtrasType.NO_BALL) ball.extrasRuns else 0
            val isBowlerWkt = ball.isWicket && ball.wicketType.isBowlerWicket

            bowlersMap[bowler] = currentBowlerStats.copy(
                legalBallsBowled = currentBowlerStats.legalBallsBowled + bowlerLegalBalls,
                runsConceded = currentBowlerStats.runsConceded + bowlerRunsConceded,
                wickets = currentBowlerStats.wickets + (if (isBowlerWkt) 1 else 0),
                wides = currentBowlerStats.wides + bowlerWides,
                noBalls = currentBowlerStats.noBalls + bowlerNoBalls
            )

            // Wicket handling
            if (ball.isWicket) {
                totalWickets++
                val dismissedName = ball.dismissedPlayerName?.ifEmpty { striker } ?: striker
                val dismissedId = ball.dismissedPlayerId ?: (if (dismissedName == striker) ball.strikerPlayerId else ball.nonStrikerPlayerId)

                val dismissedStats = battersMap[dismissedName] ?: BatterInningsStats(playerName = dismissedName, playerId = dismissedId)
                val dismissalDesc = createDismissalDescription(ball.wicketType, bowler, ball.fielderPlayerName)

                battersMap[dismissedName] = dismissedStats.copy(
                    isOut = true,
                    dismissalText = dismissalDesc,
                    dismissalType = ball.wicketType,
                    bowlerName = bowler,
                    fielderName = ball.fielderPlayerName
                )

                val overStr = "${(legalBalls - if (isLegal) 1 else 0) / 6}.${(legalBalls - if (isLegal) 1 else 0) % 6}"
                fallOfWickets.add(
                    FallOfWicket(
                        wicketNumber = totalWickets,
                        score = totalRuns,
                        overBall = overStr,
                        batsmanName = dismissedName
                    )
                )

                // Reset partnership on wicket
                partRuns = 0
                partBalls = 0
                partBatter1Runs = 0
                partBatter2Runs = 0
            }

            // Strike rotation logic:
            // 1. Physical runs taken:
            // For legal/no-ball: runsOffBat + bye/leg-bye runs
            // For wide: only if physical runs ran in addition to 1 wide penalty
            val physicalRuns = when (ball.extrasType) {
                ExtrasType.BYE, ExtrasType.LEG_BYE -> ball.extrasRuns
                ExtrasType.WIDE -> maxOf(0, ball.extrasRuns - 1)
                else -> runsOffBat
            }

            var endsRotatedForRuns = false
            if (physicalRuns % 2 != 0) {
                // Odd runs swap ends
                val tempName = currentStrikerName
                val tempId = currentStrikerId
                currentStrikerName = currentNonStrikerName
                currentStrikerId = currentNonStrikerId
                currentNonStrikerName = tempName
                currentNonStrikerId = tempId
                endsRotatedForRuns = true
            }

            // 2. Check Over completion (6 legal balls)
            if (legalBallsInCurrentOver >= 6) {
                isCurrentOverFinished = true
                legalBallsInCurrentOver = 0
                // End of over: ends swap!
                val tempName = currentStrikerName
                val tempId = currentStrikerId
                currentStrikerName = currentNonStrikerName
                currentStrikerId = currentNonStrikerId
                currentNonStrikerName = tempName
                currentNonStrikerId = tempId
            } else {
                isCurrentOverFinished = false
            }
        }

        // Maidens calculation
        val bowlerMaidens = calculateMaidens(sortedDeliveries)
        val finalBowlersList = bowlersMap.values.map { bowlerStats ->
            bowlerStats.copy(maidens = bowlerMaidens[bowlerStats.bowlerName] ?: 0)
        }

        val target = if (inningsNumber == 2) match.targetRuns else null
        val totalBallsConfigured = match.totalOvers * 6

        var isInningsComplete = false
        var isMatchComplete = false
        var resultSummary: String? = null

        // Check Innings 1 Completion
        if (inningsNumber == 1) {
            if (totalWickets >= maxWickets || legalBalls >= totalBallsConfigured) {
                isInningsComplete = true
                resultSummary = "$battingTeam completed 1st innings: $totalRuns/$totalWickets (${legalBalls / 6}.${legalBalls % 6} ov). Target: ${totalRuns + 1}."
            }
        } else {
            // Innings 2 (Chasing)
            if (target != null && totalRuns >= target) {
                isInningsComplete = true
                isMatchComplete = true
                val wicketsRemaining = maxWickets - totalWickets
                resultSummary = "$battingTeam won by $wicketsRemaining wicket${if (wicketsRemaining != 1) "s" else ""}!"
            } else if (totalWickets >= maxWickets || legalBalls >= totalBallsConfigured) {
                isInningsComplete = true
                isMatchComplete = true
                val runsShort = (target ?: (totalRuns + 1)) - 1 - totalRuns
                resultSummary = if (runsShort == 0) {
                    "Match Tied!"
                } else if (runsShort > 0) {
                    val defendingTeam = bowlingTeam
                    "$defendingTeam won by $runsShort run${if (runsShort != 1) "s" else ""}!"
                } else {
                    "$battingTeam won!"
                }
            }
        }

        val recentBalls = sortedDeliveries.takeLast(12)
        val currentOverBalls = sortedDeliveries.filter { it.overNumber == (legalBalls / 6) || (isCurrentOverFinished && it.overNumber == ((legalBalls - 1).coerceAtLeast(0) / 6)) }

        val activeStriker = battersMap[currentStrikerName] ?: if (currentStrikerName.isNotEmpty()) {
            BatterInningsStats(playerName = currentStrikerName, playerId = currentStrikerId)
        } else null

        val activeNonStriker = battersMap[currentNonStrikerName] ?: if (currentNonStrikerName.isNotEmpty()) {
            BatterInningsStats(playerName = currentNonStrikerName, playerId = currentNonStrikerId)
        } else null

        val activeBowler = finalBowlersList.find { it.bowlerName == currentBowlerName } ?: if (currentBowlerName.isNotEmpty()) {
            BowlerInningsStats(bowlerName = currentBowlerName, bowlerId = currentBowlerId)
        } else null

        return InningsLiveState(
            inningsNumber = inningsNumber,
            battingTeam = battingTeam,
            bowlingTeam = bowlingTeam,
            totalRuns = totalRuns,
            totalWickets = totalWickets,
            legalBalls = legalBalls,
            totalOversConfigured = match.totalOvers,
            maxWickets = maxWickets,
            currentStriker = activeStriker,
            currentNonStriker = activeNonStriker,
            currentBowler = activeBowler,
            recentDeliveries = recentBalls,
            currentOverDeliveries = currentOverBalls,
            partnership = Partnership(
                runs = partRuns,
                balls = partBalls,
                batter1Name = currentStrikerName,
                batter1Runs = partBatter1Runs,
                batter2Name = currentNonStrikerName,
                batter2Runs = partBatter2Runs
            ),
            fallOfWickets = fallOfWickets,
            battingCard = battersMap.values.toList(),
            bowlingCard = finalBowlersList,
            extras = ExtrasBreakdown(
                wides = wides,
                noBalls = noBalls,
                byes = byes,
                legByes = legByes,
                penalties = penalties
            ),
            target = target,
            isOverComplete = isCurrentOverFinished && legalBalls > 0 && (legalBalls % 6 == 0),
            isInningsComplete = isInningsComplete,
            isMatchComplete = isMatchComplete,
            resultSummary = resultSummary
        )
    }

    private fun calculateMaidens(deliveries: List<BallDeliveryEntity>): Map<String, Int> {
        val maidensMap = mutableMapOf<String, Int>()
        val oversGrouped = deliveries.groupBy { "${it.overNumber}_${it.bowlerName}" }

        for ((_, ballsInOver) in oversGrouped) {
            val legalCount = ballsInOver.count { it.isLegalDelivery }
            if (legalCount == 6) {
                val bowlerName = ballsInOver.first().bowlerName
                val totalOverRunsConceded = ballsInOver.sumOf { ball ->
                    ball.runsOffBat + when (ball.extrasType) {
                        ExtrasType.WIDE, ExtrasType.NO_BALL, ExtrasType.PENALTY -> ball.extrasRuns
                        else -> 0
                    }
                }
                if (totalOverRunsConceded == 0) {
                    maidensMap[bowlerName] = (maidensMap[bowlerName] ?: 0) + 1
                }
            }
        }
        return maidensMap
    }

    fun createDismissalDescription(type: WicketType, bowler: String, fielder: String?): String {
        return when (type) {
            WicketType.BOWLED -> "b $bowler"
            WicketType.CAUGHT -> if (!fielder.isNullOrBlank() && fielder != bowler) "c $fielder b $bowler" else "c & b $bowler"
            WicketType.LBW -> "lbw b $bowler"
            WicketType.RUN_OUT -> if (!fielder.isNullOrBlank()) "run out ($fielder)" else "run out"
            WicketType.STUMPED -> if (!fielder.isNullOrBlank()) "st $fielder b $bowler" else "stumped b $bowler"
            WicketType.HIT_WICKET -> "hit wicket b $bowler"
            WicketType.RETIRED_HURT -> "retired hurt"
            WicketType.RETIRED_OUT -> "retired out"
            WicketType.TIMED_OUT -> "timed out"
            WicketType.OBSTRUCTING_FIELD -> "obstructing the field"
            WicketType.NONE -> "not out"
        }
    }
}
