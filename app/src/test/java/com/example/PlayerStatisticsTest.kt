package com.example

import com.example.data.model.BallDeliveryEntity
import com.example.data.model.ExtrasType
import com.example.data.model.MatchEntity
import com.example.data.model.MatchFormat
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import com.example.data.model.WicketType
import com.example.domain.engine.PlayerStatisticsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStatisticsTest {

    private val playerTalha = PlayerEntity(
        id = 1L,
        fullName = "Talha",
        jerseyNumber = 7,
        role = PlayerRole.ALL_ROUNDER,
        isCaptain = true
    )

    private val playerFaheem = PlayerEntity(
        id = 2L,
        fullName = "Faheem",
        jerseyNumber = 10,
        role = PlayerRole.BOWLER,
        isViceCaptain = true
    )

    private val testMatch = MatchEntity(
        id = 100L,
        matchTitle = "Rajpoot Cricket Boys vs Challengers",
        tournamentName = "Champions Trophy",
        season = "2026",
        homeTeamName = "Rajpoot Cricket Boys",
        awayTeamName = "Challengers",
        format = MatchFormat.T20,
        totalOvers = 20
    )

    @Test
    fun testBattingAverageWithDismissals() {
        // Player plays 2 matches:
        // Match 1: 50 runs, dismissed
        // Match 2: 70 runs, not out
        // Total runs = 120, Innings = 2, Not outs = 1, Dismissals = 1
        // Batting average = 120 / 1 = 120.0
        val match1 = testMatch.copy(id = 101L)
        val match2 = testMatch.copy(id = 102L)

        val deliveries = listOf(
            // Match 1
            BallDeliveryEntity(
                matchId = 101L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = 1,
                ballSequence = 1,
                bowlerName = "Opponent Bowler",
                strikerName = "Talha",
                strikerPlayerId = 1L,
                nonStrikerName = "Partner",
                runsOffBat = 50,
                isLegalDelivery = true,
                totalBallRuns = 50
            ),
            BallDeliveryEntity(
                matchId = 101L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = 2,
                ballSequence = 2,
                bowlerName = "Opponent Bowler",
                strikerName = "Talha",
                strikerPlayerId = 1L,
                nonStrikerName = "Partner",
                runsOffBat = 0,
                isLegalDelivery = true,
                isWicket = true,
                wicketType = WicketType.BOWLED,
                dismissedPlayerName = "Talha",
                dismissedPlayerId = 1L
            ),
            // Match 2
            BallDeliveryEntity(
                matchId = 102L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = 1,
                ballSequence = 1,
                bowlerName = "Opponent Bowler",
                strikerName = "Talha",
                strikerPlayerId = 1L,
                nonStrikerName = "Partner",
                runsOffBat = 70,
                isLegalDelivery = true,
                totalBallRuns = 70
            )
        )

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(match1, match2),
            deliveries = deliveries
        )

        assertEquals(2, stats.matchesPlayed)
        assertEquals(2, stats.inningsBatted)
        assertEquals(1, stats.notOuts)
        assertEquals(120, stats.totalRuns)
        assertEquals(1, stats.dismissals)
        assertNotNull(stats.battingAverage)
        assertEquals(120.0, stats.battingAverage!!, 0.001)
        assertEquals("120.00", stats.formattedBattingAverage)
        assertEquals("70*", stats.highestScoreFormatted)
    }

    @Test
    fun testBattingAverageZeroDismissalsReturnsNullAndFormattedNA() {
        // Player plays 1 match, scores 45 runs, remaining not out
        val deliveries = listOf(
            BallDeliveryEntity(
                matchId = 100L,
                inningsNumber = 1,
                overNumber = 0,
                legalBallInOver = 1,
                ballSequence = 1,
                bowlerName = "Bowler A",
                strikerName = "Talha",
                strikerPlayerId = 1L,
                nonStrikerName = "Partner",
                runsOffBat = 45,
                isLegalDelivery = true,
                totalBallRuns = 45
            )
        )

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(testMatch),
            deliveries = deliveries
        )

        assertEquals(1, stats.inningsBatted)
        assertEquals(1, stats.notOuts)
        assertEquals(0, stats.dismissals)
        assertNull(stats.battingAverage)
        assertEquals("N/A", stats.formattedBattingAverage)
        assertEquals("45*", stats.highestScoreFormatted)
    }

    @Test
    fun testStrikeRateAndWidesExcludedFromBallsFaced() {
        // 5 deliveries faced by batter:
        // Ball 1: 4 runs off bat (legal)
        // Ball 2: 1 wide (extrasType = WIDE, runsOffBat = 0, extraRuns = 1) -> does NOT count as ball faced!
        // Ball 3: 6 runs off bat (legal)
        // Ball 4: 0 runs off bat, 1 no-ball (extrasType = NO_BALL) -> DOES count as ball faced!
        // Ball 5: 0 runs off bat (legal)
        // Total runs off bat = 4 + 6 = 10 runs
        // Total balls faced = 4 (Balls 1, 3, 4, 5)
        // Strike Rate = (10 / 4) * 100 = 250.0
        val deliveries = listOf(
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "B", strikerName = "Talha", nonStrikerName = "NS", runsOffBat = 4, isLegalDelivery = true),
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 0, ballSequence = 2, bowlerName = "B", strikerName = "Talha", nonStrikerName = "NS", runsOffBat = 0, extrasType = ExtrasType.WIDE, extrasRuns = 1, isLegalDelivery = false),
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 3, bowlerName = "B", strikerName = "Talha", nonStrikerName = "NS", runsOffBat = 6, isLegalDelivery = true),
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 0, ballSequence = 4, bowlerName = "B", strikerName = "Talha", nonStrikerName = "NS", runsOffBat = 0, extrasType = ExtrasType.NO_BALL, extrasRuns = 1, isLegalDelivery = false),
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 3, ballSequence = 5, bowlerName = "B", strikerName = "Talha", nonStrikerName = "NS", runsOffBat = 0, isLegalDelivery = true)
        )

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(testMatch),
            deliveries = deliveries
        )

        assertEquals(10, stats.totalRuns)
        assertEquals(4, stats.ballsFaced)
        assertNotNull(stats.strikeRate)
        assertEquals(250.0, stats.strikeRate!!, 0.001)
        assertEquals("250.0", stats.formattedStrikeRate)
        assertEquals(1, stats.fours)
        assertEquals(1, stats.sixes)
    }

    @Test
    fun testStrikeRateZeroBallsFacedReturnsNullAndFormattedNA() {
        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(testMatch),
            deliveries = emptyList()
        )

        assertEquals(0, stats.ballsFaced)
        assertNull(stats.strikeRate)
        assertEquals("N/A", stats.formattedStrikeRate)
    }

    @Test
    fun testBowlingRunsConcededExcludesByesAndLegByes() {
        // Faheem bowls 4 balls:
        // Ball 1: 4 byes (extrasType = BYE, extraRuns = 4, runsOffBat = 0) -> 0 runs conceded by bowler!
        // Ball 2: 2 leg-byes (extrasType = LEG_BYE, extraRuns = 2, runsOffBat = 0) -> 0 runs conceded by bowler!
        // Ball 3: 1 wide (extrasType = WIDE, extraRuns = 1) -> 1 run conceded!
        // Ball 4: 4 runs off bat -> 4 runs conceded!
        // Total runs conceded = 0 + 0 + 1 + 4 = 5 runs conceded.
        val deliveries = listOf(
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "S", nonStrikerName = "NS", runsOffBat = 0, extrasType = ExtrasType.BYE, extrasRuns = 4, isLegalDelivery = true),
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 2, bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "S", nonStrikerName = "NS", runsOffBat = 0, extrasType = ExtrasType.LEG_BYE, extrasRuns = 2, isLegalDelivery = true),
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 0, ballSequence = 3, bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "S", nonStrikerName = "NS", runsOffBat = 0, extrasType = ExtrasType.WIDE, extrasRuns = 1, isLegalDelivery = false),
            BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 3, ballSequence = 4, bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "S", nonStrikerName = "NS", runsOffBat = 4, isLegalDelivery = true)
        )

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerFaheem,
            matches = listOf(testMatch),
            deliveries = deliveries
        )

        assertEquals(5, stats.runsConceded)
        assertEquals(3, stats.legalBallsBowled)
        assertEquals("0.3", stats.formattedOversBowled)
    }

    @Test
    fun testBowlerWicketsExcludesRunOuts() {
        // Faheem bowls 2 balls resulting in dismissals:
        // Ball 1: WicketType.BOWLED -> Bowler wicket credited
        // Ball 2: WicketType.RUN_OUT -> Bowler wicket NOT credited
        val deliveries = listOf(
            BallDeliveryEntity(
                matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1,
                bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "Batter 1", nonStrikerName = "Batter 2",
                isLegalDelivery = true, isWicket = true, wicketType = WicketType.BOWLED,
                dismissedPlayerName = "Batter 1"
            ),
            BallDeliveryEntity(
                matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 2,
                bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "Batter 3", nonStrikerName = "Batter 2",
                isLegalDelivery = true, isWicket = true, wicketType = WicketType.RUN_OUT,
                dismissedPlayerName = "Batter 2"
            )
        )

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerFaheem,
            matches = listOf(testMatch),
            deliveries = deliveries
        )

        assertEquals(1, stats.wickets)
    }

    @Test
    fun testBowlingEconomyAndAverageCalculations() {
        // Faheem bowls 12 legal balls (2 full overs), concedes 12 runs, takes 2 wickets
        // Economy = 12 / (12 / 6.0) = 6.00
        // Bowling Average = 12 / 2 = 6.00
        // Bowling Strike Rate = 12 / 2 = 6.00
        val balls = mutableListOf<BallDeliveryEntity>()
        for (i in 1..12) {
            val isWicketBall = (i == 3 || i == 7)
            balls.add(
                BallDeliveryEntity(
                    matchId = 100L,
                    inningsNumber = 1,
                    overNumber = if (i <= 6) 0 else 1,
                    legalBallInOver = if (i <= 6) i else i - 6,
                    ballSequence = i,
                    bowlerName = "Faheem",
                    bowlerPlayerId = 2L,
                    strikerName = "Batter",
                    nonStrikerName = "NS",
                    runsOffBat = 1,
                    isLegalDelivery = true,
                    isWicket = isWicketBall,
                    wicketType = if (isWicketBall) WicketType.CAUGHT else WicketType.NONE
                )
            )
        }

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerFaheem,
            matches = listOf(testMatch),
            deliveries = balls
        )

        assertEquals(12, stats.legalBallsBowled)
        assertEquals("2.0", stats.formattedOversBowled)
        assertEquals(12, stats.runsConceded)
        assertEquals(2, stats.wickets)
        assertNotNull(stats.economyRate)
        assertEquals(6.0, stats.economyRate!!, 0.001)
        assertEquals("6.00", stats.formattedEconomyRate)
        assertNotNull(stats.bowlingAverage)
        assertEquals(6.0, stats.bowlingAverage!!, 0.001)
        assertEquals("6.00", stats.formattedBowlingAverage)
        assertNotNull(stats.bowlingStrikeRate)
        assertEquals(6.0, stats.bowlingStrikeRate!!, 0.001)
        assertEquals("6.0", stats.formattedBowlingStrikeRate)
    }

    @Test
    fun testBowlingMaidenCalculation() {
        // Over 0: 6 legal balls, all 0 runs -> Maiden = 1
        // Over 1: 6 legal balls, 1 run conceded -> Not a maiden
        val balls = mutableListOf<BallDeliveryEntity>()
        // Over 0
        for (i in 1..6) {
            balls.add(
                BallDeliveryEntity(
                    matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = i, ballSequence = i,
                    bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "B", nonStrikerName = "NS",
                    runsOffBat = 0, isLegalDelivery = true
                )
            )
        }
        // Over 1
        for (i in 1..6) {
            balls.add(
                BallDeliveryEntity(
                    matchId = 100L, inningsNumber = 1, overNumber = 1, legalBallInOver = i, ballSequence = i + 6,
                    bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "B", nonStrikerName = "NS",
                    runsOffBat = if (i == 1) 1 else 0, isLegalDelivery = true
                )
            )
        }

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerFaheem,
            matches = listOf(testMatch),
            deliveries = balls
        )

        assertEquals(1, stats.maidens)
        assertEquals(11, stats.dotBalls)
    }

    @Test
    fun testZeroWicketsBowlingReturnsNullAverageAndStrikeRate() {
        val balls = listOf(
            BallDeliveryEntity(
                matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1,
                bowlerName = "Faheem", bowlerPlayerId = 2L, strikerName = "B", nonStrikerName = "NS",
                runsOffBat = 4, isLegalDelivery = true
            )
        )

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerFaheem,
            matches = listOf(testMatch),
            deliveries = balls
        )

        assertEquals(0, stats.wickets)
        assertNull(stats.bowlingAverage)
        assertEquals("N/A", stats.formattedBowlingAverage)
        assertNull(stats.bowlingStrikeRate)
        assertEquals("N/A", stats.formattedBowlingStrikeRate)
    }

    @Test
    fun testSeasonAndTournamentFiltering() {
        val matchSeason2025 = testMatch.copy(id = 201L, season = "2025", tournamentName = "Ramadan Cup")
        val matchSeason2026 = testMatch.copy(id = 202L, season = "2026", tournamentName = "Champions Trophy")

        val deliveries = listOf(
            // In 2025 match
            BallDeliveryEntity(
                matchId = 201L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1,
                bowlerName = "B", strikerName = "Talha", strikerPlayerId = 1L, nonStrikerName = "NS",
                runsOffBat = 40, isLegalDelivery = true
            ),
            // In 2026 match
            BallDeliveryEntity(
                matchId = 202L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1,
                bowlerName = "B", strikerName = "Talha", strikerPlayerId = 1L, nonStrikerName = "NS",
                runsOffBat = 80, isLegalDelivery = true
            )
        )

        // All-Time
        val careerStats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(matchSeason2025, matchSeason2026),
            deliveries = deliveries
        )
        assertEquals(120, careerStats.totalRuns)

        // Filtered Season 2026
        val season2026Stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(matchSeason2025, matchSeason2026),
            deliveries = deliveries,
            seasonFilter = "2026"
        )
        assertEquals(80, season2026Stats.totalRuns)

        // Filtered Tournament "Ramadan Cup"
        val ramadanStats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(matchSeason2025, matchSeason2026),
            deliveries = deliveries,
            tournamentFilter = "Ramadan Cup"
        )
        assertEquals(40, ramadanStats.totalRuns)
    }

    @Test
    fun testDuckCalculation() {
        val deliveries = listOf(
            BallDeliveryEntity(
                matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1,
                bowlerName = "B", strikerName = "Talha", strikerPlayerId = 1L, nonStrikerName = "NS",
                runsOffBat = 0, isLegalDelivery = true, isWicket = true, wicketType = WicketType.LBW,
                dismissedPlayerName = "Talha", dismissedPlayerId = 1L
            )
        )

        val stats = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = playerTalha,
            matches = listOf(testMatch),
            deliveries = deliveries
        )

        assertEquals(0, stats.totalRuns)
        assertEquals(1, stats.inningsBatted)
        assertEquals(0, stats.notOuts)
        assertEquals(1, stats.ducks)
    }

    @Test
    fun testSquadRankingsAndQualificationThresholds() {
        val p1 = playerTalha
        val p2 = playerFaheem

        // p1: 100 runs, 0 dismissals (average N/A because dismissals = 0)
        // p2: 50 runs, 1 dismissal (average = 50.0)
        val s1 = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = p1,
            matches = listOf(testMatch),
            deliveries = listOf(
                BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "B", strikerName = "Talha", nonStrikerName = "NS", runsOffBat = 100, isLegalDelivery = true)
            )
        )

        val s2 = PlayerStatisticsCalculator.calculatePlayerStatistics(
            player = p2,
            matches = listOf(testMatch),
            deliveries = listOf(
                BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 1, ballSequence = 1, bowlerName = "B", strikerName = "Faheem", nonStrikerName = "NS", runsOffBat = 50, isLegalDelivery = true),
                BallDeliveryEntity(matchId = 100L, inningsNumber = 1, overNumber = 0, legalBallInOver = 2, ballSequence = 2, bowlerName = "B", strikerName = "Faheem", nonStrikerName = "NS", runsOffBat = 0, isLegalDelivery = true, isWicket = true, wicketType = WicketType.BOWLED, dismissedPlayerName = "Faheem")
            )
        )

        // Category 0: Most Runs -> p1 should be ranked #1
        val mostRunsRankings = PlayerStatisticsCalculator.computeSquadRankings(0, listOf(s1, s2))
        assertEquals(2, mostRunsRankings.size)
        assertEquals("Talha", mostRunsRankings[0].player.fullName)
        assertEquals("Faheem", mostRunsRankings[1].player.fullName)

        // Category 3: Batting Average -> Only p2 qualifies because p1 has 0 dismissals (denominator zero is excluded from qualified rankings)
        val avgRankings = PlayerStatisticsCalculator.computeSquadRankings(3, listOf(s1, s2))
        assertEquals(1, avgRankings.size)
        assertEquals("Faheem", avgRankings[0].player.fullName)
    }
}
