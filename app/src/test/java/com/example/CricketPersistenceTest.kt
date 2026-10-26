package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BallDeliveryDao
import com.example.data.local.MatchDao
import com.example.data.local.PlayerDao
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.BattingHand
import com.example.data.model.BowlingStyle
import com.example.data.model.ExtrasType
import com.example.data.model.MatchEntity
import com.example.data.model.MatchFormat
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import com.example.data.model.TossDecision
import com.example.data.model.WicketType
import com.example.domain.engine.CricketRulesEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CricketPersistenceTest {

    private lateinit var db: AppDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var matchDao: MatchDao
    private lateinit var ballDeliveryDao: BallDeliveryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playerDao = db.playerDao()
        matchDao = db.matchDao()
        ballDeliveryDao = db.ballDeliveryDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndQuerySquadPlayers() = runBlocking {
        val talha = PlayerEntity(
            fullName = "Talha",
            jerseyNumber = 7,
            role = PlayerRole.ALL_ROUNDER,
            battingHand = BattingHand.RIGHT_HAND,
            bowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM
        )
        val faheem = PlayerEntity(
            fullName = "Faheem",
            jerseyNumber = 10,
            role = PlayerRole.BOWLER,
            battingHand = BattingHand.RIGHT_HAND,
            bowlingStyle = BowlingStyle.RIGHT_ARM_FAST
        )

        val insertedIds = playerDao.insertPlayers(listOf(talha, faheem))
        assertEquals(2, insertedIds.size)

        val players = playerDao.getAllPlayers().first()
        assertEquals(2, players.size)
        assertTrue(players.any { it.fullName == "Talha" && it.jerseyNumber == 7 })
        assertTrue(players.any { it.fullName == "Faheem" && it.jerseyNumber == 10 })
    }

    @Test
    fun createAndPersistMatch() = runBlocking {
        val match = MatchEntity(
            matchTitle = "Final: Rajpoot Cricket Boys vs Lahore Tigers",
            homeTeamName = "Rajpoot Cricket Boys",
            awayTeamName = "Lahore Tigers",
            battingFirstTeam = "Rajpoot Cricket Boys",
            format = MatchFormat.T20,
            totalOvers = 20,
            playersPerSide = 11,
            tossWinnerTeam = "Rajpoot Cricket Boys",
            tossDecision = TossDecision.BAT,
            currentInnings = 1
        )

        val matchId = matchDao.insertMatch(match)
        assertTrue("Match ID should be greater than 0", matchId > 0)

        val fetched = matchDao.getMatchById(matchId)
        assertNotNull(fetched)
        assertEquals("Rajpoot Cricket Boys", fetched?.homeTeamName)
        assertEquals(20, fetched?.totalOvers)
        assertEquals(1, fetched?.currentInnings)

        // Update match status to completed
        matchDao.updateMatch(fetched!!.copy(isCompleted = true, winnerTeam = "Rajpoot Cricket Boys"))
        val updated = matchDao.getMatchById(matchId)
        assertTrue(updated?.isCompleted == true)
        assertEquals("Rajpoot Cricket Boys", updated?.winnerTeam)
    }

    @Test
    fun ballDeliveryPersistenceAndUndoWorkflow() = runBlocking {
        val match = MatchEntity(
            matchTitle = "Rajpoot Cricket Boys vs Rawalpindi Royals",
            homeTeamName = "Rajpoot Cricket Boys",
            awayTeamName = "Rawalpindi Royals",
            battingFirstTeam = "Rajpoot Cricket Boys",
            format = MatchFormat.T20,
            totalOvers = 20,
            playersPerSide = 11,
            tossWinnerTeam = "Rajpoot Cricket Boys",
            tossDecision = TossDecision.BAT,
            currentInnings = 1
        )
        val matchId = matchDao.insertMatch(match)

        // 1. Insert 3 deliveries:
        // Ball 1: 4 runs off bat (legal)
        val delivery1 = BallDeliveryEntity(
            matchId = matchId,
            inningsNumber = 1,
            overNumber = 0,
            legalBallInOver = 1,
            ballSequence = 1,
            bowlerName = "Faheem",
            strikerName = "Talha",
            nonStrikerName = "Mudassir",
            runsOffBat = 4,
            isLegalDelivery = true,
            totalBallRuns = 4
        )
        // Ball 2: Wide (1 extra run, not legal)
        val delivery2 = BallDeliveryEntity(
            matchId = matchId,
            inningsNumber = 1,
            overNumber = 0,
            legalBallInOver = 0,
            ballSequence = 2,
            bowlerName = "Faheem",
            strikerName = "Talha",
            nonStrikerName = "Mudassir",
            runsOffBat = 0,
            extrasType = ExtrasType.WIDE,
            extrasRuns = 1,
            isLegalDelivery = false,
            totalBallRuns = 1
        )
        // Ball 3: Bowled wicket (legal)
        val delivery3 = BallDeliveryEntity(
            matchId = matchId,
            inningsNumber = 1,
            overNumber = 0,
            legalBallInOver = 2,
            ballSequence = 3,
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

        ballDeliveryDao.insertDelivery(delivery1)
        ballDeliveryDao.insertDelivery(delivery2)
        ballDeliveryDao.insertDelivery(delivery3)

        // Query persisted deliveries from Room
        val savedDeliveries = ballDeliveryDao.getDeliveriesForMatchSnapshot(matchId)
        assertEquals(3, savedDeliveries.size)

        // Calculate live state from Room snapshot
        val stateAfter3Balls = CricketRulesEngine.calculateInningsState(match, 1, savedDeliveries)
        assertEquals("Total score after 3 balls is 5", 5, stateAfter3Balls.totalRuns)
        assertEquals("Total legal deliveries is 2", 2, stateAfter3Balls.legalBalls)
        assertEquals("Total wickets is 1", 1, stateAfter3Balls.totalWickets)
        assertEquals("Extras wide is 1", 1, stateAfter3Balls.extras.wides)

        // 2. Perform Undo (delete last delivery)
        ballDeliveryDao.deleteLastDelivery(matchId)

        val deliveriesAfterUndo = ballDeliveryDao.getDeliveriesForMatchSnapshot(matchId)
        assertEquals(2, deliveriesAfterUndo.size)
        // Verify Ball 3 was removed, Ball 1 and Ball 2 remain
        assertEquals(1, deliveriesAfterUndo[0].ballSequence)
        assertEquals(2, deliveriesAfterUndo[1].ballSequence)

        // Recalculate engine state after undo
        val stateAfterUndo = CricketRulesEngine.calculateInningsState(match, 1, deliveriesAfterUndo)
        assertEquals("Total score restored to 5", 5, stateAfterUndo.totalRuns)
        assertEquals("Total legal deliveries restored to 1", 1, stateAfterUndo.legalBalls)
        assertEquals("Total wickets restored to 0", 0, stateAfterUndo.totalWickets)

        val talha = stateAfterUndo.battingCard.find { it.playerName == "Talha" }
        assertEquals("Talha is not out after undo", false, talha?.isOut)
    }
}
