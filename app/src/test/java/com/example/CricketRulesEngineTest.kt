package com.example

import com.example.data.model.BallDeliveryEntity
import com.example.data.model.ExtrasType
import com.example.data.model.MatchEntity
import com.example.data.model.MatchFormat
import com.example.data.model.TossDecision
import com.example.data.model.WicketType
import com.example.domain.engine.CricketRulesEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CricketRulesEngineTest {

    private val testMatch = MatchEntity(
        id = 1L,
        matchTitle = "Rajpoot Cricket Boys vs Challengers",
        homeTeamName = "Rajpoot Cricket Boys",
        awayTeamName = "Challengers",
        battingFirstTeam = "Rajpoot Cricket Boys",
        format = MatchFormat.T20,
        totalOvers = 20,
        playersPerSide = 11,
        tossWinnerTeam = "Rajpoot Cricket Boys",
        tossDecision = TossDecision.BAT,
        currentInnings = 1
    )

    @Test
    fun sixLegalDeliveriesCompleteOneOver() {
        val balls = mutableListOf<BallDeliveryEntity>()
        for (i in 1..6) {
            balls.add(
                BallDeliveryEntity(
                    matchId = 1L,
                    inningsNumber = 1,
                    overNumber = 0,
                    legalBallInOver = i,
                    ballSequence = i,
                    bowlerName = "Faheem",
                    strikerName = "Talha",
                    nonStrikerName = "Mudassir",
                    runsOffBat = 1,
                    isLegalDelivery = true,
                    totalBallRuns = 1
                )
            )
        }

        val state = CricketRulesEngine.calculateInningsState(
            match = testMatch,
            inningsNumber = 1,
            deliveries = balls,
            initialStrikerName = "Talha",
            initialNonStrikerName = "Mudassir",
            initialBowlerName = "Faheem"
        )

        assertEquals(6, state.legalBalls)
        assertEquals(1, state.fullOvers)
        assertEquals(0, state.ballsInCurrentOver)
        assertEquals("1.0", state.formattedOvers)
        assertTrue("Over must be marked complete", state.isOverComplete)
    }

    @Test
    fun legalDeliveriesUpdateBatterAndBowlerFigures() {
        val balls = listOf(
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 4, isLegalDelivery = true, totalBallRuns = 4),
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 2, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 6, isLegalDelivery = true, totalBallRuns = 6),
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 3, ballSequence = 3, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 0, isLegalDelivery = true, totalBallRuns = 0),
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 4, ballSequence = 4, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 2, isLegalDelivery = true, totalBallRuns = 2)
        )

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, balls, "Talha", "Mudassir", "Faheem")

        val talha = state.battingCard.find { it.playerName == "Talha" }
        assertNotNull(talha)
        assertEquals("Talha runs should be 12", 12, talha!!.runs)
        assertEquals("Talha balls faced should be 4", 4, talha.ballsFaced)
        assertEquals("Talha fours should be 1", 1, talha.fours)
        assertEquals("Talha sixes should be 1", 1, talha.sixes)
        assertEquals("Talha strike rate should be 300.0%", 300.0, talha.strikeRate, 0.01)

        val faheem = state.bowlingCard.find { it.bowlerName == "Faheem" }
        assertNotNull(faheem)
        assertEquals(4, faheem!!.legalBallsBowled)
        assertEquals(12, faheem.runsConceded)
        assertEquals("0.4", faheem.formattedOvers)
        assertEquals(18.0, faheem.economyRate, 0.01)
    }

    @Test
    fun widesAndNoBallsDoNotAdvanceLegalBallCount() {
        val balls = listOf(
            // Ball 1: Legal
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 0, isLegalDelivery = true, totalBallRuns = 0),
            // Ball 2: Wide
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 0, ballSequence = 2, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", extrasType = ExtrasType.WIDE, extrasRuns = 1, isLegalDelivery = false, totalBallRuns = 1),
            // Ball 3: No-ball
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 0, ballSequence = 3, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", extrasType = ExtrasType.NO_BALL, extrasRuns = 1, isLegalDelivery = false, totalBallRuns = 1),
            // Ball 4: Legal
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 4, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 4, isLegalDelivery = true, totalBallRuns = 4)
        )

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, balls)

        assertEquals("Total legal deliveries must be 2", 2, state.legalBalls)
        assertEquals("Total runs must be 6 (0 + 1 wide + 1 nb + 4)", 6, state.totalRuns)
        assertEquals(1, state.extras.wides)
        assertEquals(1, state.extras.noBalls)
        assertFalse(state.isOverComplete)
    }

    @Test
    fun extrasAttributedCorrectly() {
        val balls = listOf(
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 0, ballSequence = 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", extrasType = ExtrasType.WIDE, extrasRuns = 1, isLegalDelivery = false, totalBallRuns = 1),
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 2, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", extrasType = ExtrasType.BYE, extrasRuns = 2, isLegalDelivery = true, totalBallRuns = 2),
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 3, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", extrasType = ExtrasType.LEG_BYE, extrasRuns = 1, isLegalDelivery = true, totalBallRuns = 1)
        )

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, balls)

        assertEquals(4, state.totalRuns)
        assertEquals(1, state.extras.wides)
        assertEquals(2, state.extras.byes)
        assertEquals(1, state.extras.legByes)
        assertEquals(4, state.extras.total)

        // Bowler should NOT be charged for byes or leg-byes!
        val bowler = state.bowlingCard.find { it.bowlerName == "Faheem" }
        assertEquals("Bowler conceded only wide run (1)", 1, bowler?.runsConceded)
    }

    @Test
    fun maidenOverCalculatedAccurately() {
        // Over 0: 6 dot balls -> Maiden
        val maidenBalls = (1..6).map { i ->
            BallDeliveryEntity(
                matchId = 1L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = i,
                ballSequence = i,
                bowlerName = "Faheem",
                strikerName = "Talha",
                nonStrikerName = "Mudassir",
                runsOffBat = 0,
                isLegalDelivery = true,
                totalBallRuns = 0
            )
        }

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, maidenBalls)
        val faheem = state.bowlingCard.find { it.bowlerName == "Faheem" }
        assertEquals("6 dot balls must produce 1 maiden over", 1, faheem?.maidens)

        // Byes do NOT ruin a maiden (MCC rules)
        val maidenWithByes = (1..6).map { i ->
            BallDeliveryEntity(
                matchId = 1L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = i,
                ballSequence = i,
                bowlerName = "Faheem",
                strikerName = "Talha",
                nonStrikerName = "Mudassir",
                runsOffBat = 0,
                extrasType = if (i == 3) ExtrasType.BYE else ExtrasType.NONE,
                extrasRuns = if (i == 3) 2 else 0,
                isLegalDelivery = true,
                totalBallRuns = if (i == 3) 2 else 0
            )
        }
        val stateByes = CricketRulesEngine.calculateInningsState(testMatch, 1, maidenWithByes)
        val faheemByes = stateByes.bowlingCard.find { it.bowlerName == "Faheem" }
        assertEquals("Byes must not spoil a maiden over", 1, faheemByes?.maidens)

        // But a wide DOES ruin a maiden
        val overWithWide = mutableListOf<BallDeliveryEntity>()
        overWithWide.add(BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 0, ballSequence = 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", extrasType = ExtrasType.WIDE, extrasRuns = 1, isLegalDelivery = false, totalBallRuns = 1))
        for (i in 1..6) {
            overWithWide.add(BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = i, ballSequence = i + 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 0, isLegalDelivery = true, totalBallRuns = 0))
        }
        val stateWide = CricketRulesEngine.calculateInningsState(testMatch, 1, overWithWide)
        val faheemWide = stateWide.bowlingCard.find { it.bowlerName == "Faheem" }
        assertEquals("Wide concedes 1 run so maiden count is 0", 0, faheemWide?.maidens)
    }

    @Test
    fun wicketsUpdateInningsAndFallOfWickets() {
        val balls = listOf(
            BallDeliveryEntity(
                matchId = 1L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = 1,
                ballSequence = 1,
                bowlerName = "Faheem",
                strikerName = "Talha",
                nonStrikerName = "Mudassir",
                runsOffBat = 0,
                isLegalDelivery = true,
                isWicket = true,
                wicketType = WicketType.BOWLED,
                dismissedPlayerName = "Talha",
                totalBallRuns = 0
            )
        )

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, balls)

        assertEquals(1, state.totalWickets)
        val talha = state.battingCard.find { it.playerName == "Talha" }
        assertTrue("Talha should be marked out", talha?.isOut == true)
        assertEquals("b Faheem", talha?.dismissalText)
        assertEquals(1, state.fallOfWickets.size)
        assertEquals("Talha", state.fallOfWickets[0].batsmanName)
        assertEquals(1, state.fallOfWickets[0].wicketNumber)
        assertEquals(0, state.fallOfWickets[0].score)
    }

    @Test
    fun bowlerWicketsVersusNonBowlerWickets() {
        val balls = listOf(
            // Bowled wicket -> Bowler credited
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 0, isLegalDelivery = true, isWicket = true, wicketType = WicketType.BOWLED, dismissedPlayerName = "Talha", totalBallRuns = 0),
            // Run out -> Bowler NOT credited
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 2, bowlerName = "Faheem", strikerName = "Mudassir", nonStrikerName = "Abrar", runsOffBat = 1, isLegalDelivery = true, isWicket = true, wicketType = WicketType.RUN_OUT, dismissedPlayerName = "Mudassir", fielderPlayerName = "Haris", totalBallRuns = 1)
        )

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, balls)
        val faheem = state.bowlingCard.find { it.bowlerName == "Faheem" }

        assertEquals("Total wickets in innings is 2", 2, state.totalWickets)
        assertEquals("Faheem should only have 1 wicket (Bowled, not Run Out)", 1, faheem?.wickets)

        val mudassir = state.battingCard.find { it.playerName == "Mudassir" }
        assertEquals("run out (Haris)", mudassir?.dismissalText)
    }

    @Test
    fun strikeRotationOnOddRunsAndEndOfOver() {
        // Ball 1: 1 run -> Striker (Talha) and Non-striker (Mudassir) swap ends. Mudassir becomes striker.
        val ball1 = BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 1, isLegalDelivery = true, totalBallRuns = 1)
        val state1 = CricketRulesEngine.calculateInningsState(testMatch, 1, listOf(ball1), "Talha", "Mudassir", "Faheem")

        assertEquals("Mudassir", state1.currentStriker?.playerName)
        assertEquals("Talha", state1.currentNonStriker?.playerName)

        // Ball 2: 2 runs -> Mudassir stays on strike
        val ball2 = BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 2, bowlerName = "Faheem", strikerName = "Mudassir", nonStrikerName = "Talha", runsOffBat = 2, isLegalDelivery = true, totalBallRuns = 2)
        val state2 = CricketRulesEngine.calculateInningsState(testMatch, 1, listOf(ball1, ball2), "Talha", "Mudassir", "Faheem")

        assertEquals("Mudassir", state2.currentStriker?.playerName)
        assertEquals("Talha", state2.currentNonStriker?.playerName)
    }

    @Test
    fun singleOnSixthBallRetainsStrikeNextOver() {
        // Over 0: Balls 1-5 are 0 runs, ball 6 is 1 run.
        val balls = (1..5).map { i ->
            BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = i, ballSequence = i, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 0, isLegalDelivery = true, totalBallRuns = 0)
        }.toMutableList()

        // Ball 6: Talha takes a single
        balls.add(BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 6, ballSequence = 6, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 1, isLegalDelivery = true, totalBallRuns = 1))

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, balls, "Talha", "Mudassir", "Faheem")

        // In cricket, 1 run swaps ends, then end of over swaps ends again.
        // Therefore Talha stays on strike facing from the new end!
        assertEquals("Talha faces 1st ball of new over", "Talha", state.currentStriker?.playerName)
        assertEquals("Mudassir is non-striker", "Mudassir", state.currentNonStriker?.playerName)
        assertTrue(state.isOverComplete)
    }

    @Test
    fun runRatesCurrentAndRequiredCalculations() {
        // 12 legal balls (2.0 overs) for 18 runs
        val balls = (1..12).map { i ->
            BallDeliveryEntity(
                matchId = 1L,
                inningsNumber = 1,
                overNumber = (i - 1) / 6,
                legalBallInOver = ((i - 1) % 6) + 1,
                ballSequence = i,
                bowlerName = "Faheem",
                strikerName = "Talha",
                nonStrikerName = "Mudassir",
                runsOffBat = if (i % 2 == 0) 2 else 1,
                isLegalDelivery = true,
                totalBallRuns = if (i % 2 == 0) 2 else 1
            )
        }

        val state = CricketRulesEngine.calculateInningsState(testMatch, 1, balls)

        assertEquals(18, state.totalRuns)
        assertEquals(12, state.legalBalls)
        assertEquals("2.0", state.formattedOvers)
        assertEquals("CRR for 18 runs in 2 overs should be 9.00", 9.0, state.currentRunRate, 0.01)
        assertEquals("9.00", state.formattedCRR)

        // Innings 2 Chasing: Target 151 in 20 overs (120 balls).
        // 30 balls bowled, 41 runs scored.
        val chaseMatch = testMatch.copy(currentInnings = 2, targetRuns = 151)
        val chaseBalls = (1..30).map { i ->
            BallDeliveryEntity(
                matchId = 1L,
                inningsNumber = 2,
                overNumber = (i - 1) / 6,
                legalBallInOver = ((i - 1) % 6) + 1,
                ballSequence = i,
                bowlerName = "Faheem",
                strikerName = "Talha",
                nonStrikerName = "Mudassir",
                runsOffBat = 1,
                isLegalDelivery = true,
                totalBallRuns = 1
            )
        }

        val chaseState = CricketRulesEngine.calculateInningsState(chaseMatch, 2, chaseBalls)

        assertEquals(30, chaseState.legalBalls)
        assertEquals(90, chaseState.ballsRemaining)
        assertEquals(121, chaseState.runsRequired) // 151 - 30 = 121
        // RRR = (121 / 90) * 6 = 8.0667 -> 8.07
        assertEquals(8.07, chaseState.requiredRunRate ?: 0.0, 0.02)
    }

    @Test
    fun undoRestoresExactPreviousState() {
        val ball1 = BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 4, isLegalDelivery = true, totalBallRuns = 4)
        val ball2 = BallDeliveryEntity(matchId = 1L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 2, bowlerName = "Faheem", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 6, isLegalDelivery = true, totalBallRuns = 6)

        val fullList = listOf(ball1, ball2)
        val undoneList = listOf(ball1) // Last ball removed

        val stateFull = CricketRulesEngine.calculateInningsState(testMatch, 1, fullList)
        assertEquals(10, stateFull.totalRuns)

        val stateUndone = CricketRulesEngine.calculateInningsState(testMatch, 1, undoneList)
        assertEquals(4, stateUndone.totalRuns)
        assertEquals(1, stateUndone.legalBalls)
    }

    @Test
    fun innings1CompletesWhenAllWicketsFallOrOversFinished() {
        // 10 wickets fall in an 11-player side -> Innings Complete
        val outBalls = (1..10).map { i ->
            BallDeliveryEntity(
                matchId = 1L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = i.coerceAtMost(6),
                ballSequence = i,
                bowlerName = "Faheem",
                strikerName = "Player$i",
                nonStrikerName = "Player${i + 1}",
                runsOffBat = 0,
                isLegalDelivery = true,
                isWicket = true,
                wicketType = WicketType.BOWLED,
                dismissedPlayerName = "Player$i",
                totalBallRuns = 0
            )
        }

        val allOutState = CricketRulesEngine.calculateInningsState(testMatch, 1, outBalls)
        assertEquals(10, allOutState.totalWickets)
        assertTrue("Innings must complete on 10 wickets", allOutState.isInningsComplete)
        assertFalse("Match is not complete after 1st innings", allOutState.isMatchComplete)
        assertTrue("Summary contains target", allOutState.resultSummary?.contains("Target: 1") == true)
    }

    @Test
    fun targetChasingAndMatchCompletion() {
        // Match 2nd innings: Target is 50 runs
        val chasingMatch = testMatch.copy(
            currentInnings = 2,
            targetRuns = 50,
            battingFirstTeam = "Challengers"
        )

        // Deliveries in 2nd innings reach 52 runs
        val balls = listOf(
            BallDeliveryEntity(matchId = 1L, inningsNumber = 2, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "Haris", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 6, isLegalDelivery = true, totalBallRuns = 6),
            BallDeliveryEntity(matchId = 1L, inningsNumber = 2, overNumber = 0, legalBallInOver = 2, ballSequence = 2, bowlerName = "Haris", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 6, isLegalDelivery = true, totalBallRuns = 6),
            BallDeliveryEntity(matchId = 1L, inningsNumber = 2, overNumber = 0, legalBallInOver = 3, ballSequence = 3, bowlerName = "Haris", strikerName = "Talha", nonStrikerName = "Mudassir", runsOffBat = 40, isLegalDelivery = true, totalBallRuns = 40)
        )

        val state = CricketRulesEngine.calculateInningsState(chasingMatch, 2, balls)

        assertEquals(52, state.totalRuns)
        assertTrue("Match must be marked complete", state.isMatchComplete)
        assertTrue("Summary should report wickets win", state.resultSummary?.contains("won by") == true)
    }

    @Test
    fun defendingTeamWinsWhenBattingTeamFallsShort() {
        // Target is 100, 20 overs match (120 balls). 10 wickets fall at 85 runs.
        val chaseMatch = testMatch.copy(
            currentInnings = 2,
            targetRuns = 100,
            battingFirstTeam = "Rajpoot Cricket Boys"
        )

        val balls = (1..10).map { i ->
            BallDeliveryEntity(
                matchId = 1L,
                inningsNumber = 2,
                overNumber = i - 1,
                legalBallInOver = 1,
                ballSequence = i,
                bowlerName = "Faheem",
                strikerName = "Challenger$i",
                nonStrikerName = "Challenger${i + 1}",
                runsOffBat = 8,
                isLegalDelivery = true,
                isWicket = true,
                wicketType = WicketType.BOWLED,
                dismissedPlayerName = "Challenger$i",
                totalBallRuns = 8
            )
        }

        val state = CricketRulesEngine.calculateInningsState(chaseMatch, 2, balls)

        assertEquals(80, state.totalRuns)
        assertEquals(10, state.totalWickets)
        assertTrue(state.isInningsComplete)
        assertTrue(state.isMatchComplete)
        // Target was 100, score was 80 -> Defending team wins by 19 runs (since target - 1 = 99, 99 - 80 = 19 runs)
        assertTrue("Summary reports defending team win", state.resultSummary?.contains("Rajpoot Cricket Boys won by 19 runs") == true)
    }

    @Test
    fun matchTieWhenScoresLevelAtInningsEnd() {
        // Target is 100. Challengers score exactly 99 runs and lose their 10th wicket.
        val chaseMatch = testMatch.copy(
            currentInnings = 2,
            targetRuns = 100,
            battingFirstTeam = "Rajpoot Cricket Boys"
        )

        // 9 deliveries of 10 runs, 1 delivery of 9 runs with 10th wicket
        val balls = mutableListOf<BallDeliveryEntity>()
        for (i in 1..9) {
            balls.add(BallDeliveryEntity(matchId = 1L, inningsNumber = 2, overNumber = i - 1, legalBallInOver = 1, ballSequence = i, bowlerName = "Faheem", strikerName = "Challenger$i", nonStrikerName = "Challenger${i + 1}", runsOffBat = 10, isLegalDelivery = true, isWicket = true, wicketType = WicketType.BOWLED, dismissedPlayerName = "Challenger$i", totalBallRuns = 10))
        }
        balls.add(BallDeliveryEntity(matchId = 1L, inningsNumber = 2, overNumber = 9, legalBallInOver = 1, ballSequence = 10, bowlerName = "Faheem", strikerName = "Challenger10", nonStrikerName = "Challenger11", runsOffBat = 9, isLegalDelivery = true, isWicket = true, wicketType = WicketType.BOWLED, dismissedPlayerName = "Challenger10", totalBallRuns = 9))

        val state = CricketRulesEngine.calculateInningsState(chaseMatch, 2, balls)

        assertEquals(99, state.totalRuns)
        assertEquals(10, state.totalWickets)
        assertTrue(state.isMatchComplete)
        assertEquals("Match Tied!", state.resultSummary)
    }
}
