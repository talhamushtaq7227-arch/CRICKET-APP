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

        // Bowler should NOT be charged for byes or leg-byes!
        val bowler = state.bowlingCard.find { it.bowlerName == "Faheem" }
        assertEquals("Bowler conceded only wide run (1)", 1, bowler?.runsConceded)
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
    fun targetChasingAndMatchCompletion() {
        // Match 2nd innings: Target is 50 runs
        val chasingMatch = testMatch.copy(
            currentInnings = 2,
            targetRuns = 50,
            battingFirstTeam = "Challengers"
        )

        // Deliveries in 2nd innings reach 51 runs
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
}
