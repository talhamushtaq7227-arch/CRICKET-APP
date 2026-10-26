package com.example.data.repository

import com.example.data.local.BallDeliveryDao
import com.example.data.local.MatchDao
import com.example.data.local.PlayerDao
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.ExtrasType
import com.example.data.model.MatchEntity
import com.example.data.model.PlayerEntity
import com.example.data.model.WicketType
import com.example.domain.engine.PlayerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CricketRepository(
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao,
    private val ballDeliveryDao: BallDeliveryDao
) {
    // Players
    fun getAllPlayers(): Flow<List<PlayerEntity>> = playerDao.getAllPlayers()
    fun getActivePlayers(): Flow<List<PlayerEntity>> = playerDao.getActivePlayers()
    suspend fun getPlayerById(id: Long): PlayerEntity? = playerDao.getPlayerById(id)
    fun observePlayerById(id: Long): Flow<PlayerEntity?> = playerDao.observePlayerById(id)
    suspend fun insertPlayer(player: PlayerEntity): Long = playerDao.insertPlayer(player)
    suspend fun updatePlayer(player: PlayerEntity) = playerDao.updatePlayer(player)
    suspend fun setPlayerActive(id: Long, isActive: Boolean) = playerDao.setPlayerActive(id, isActive)
    suspend fun deletePlayer(id: Long) = playerDao.deletePlayer(id)

    // Matches
    fun getAllMatches(): Flow<List<MatchEntity>> = matchDao.getAllMatches()
    fun getCompletedMatches(): Flow<List<MatchEntity>> = matchDao.getCompletedMatches()
    fun getLatestUnfinishedMatch(): Flow<MatchEntity?> = matchDao.getLatestUnfinishedMatch()
    fun observeMatchById(id: Long): Flow<MatchEntity?> = matchDao.observeMatchById(id)
    fun getDistinctSeasons(): Flow<List<String>> = matchDao.getDistinctSeasons()
    fun getDistinctTournaments(): Flow<List<String>> = matchDao.getDistinctTournaments()
    suspend fun getMatchById(id: Long): MatchEntity? = matchDao.getMatchById(id)
    suspend fun insertMatch(match: MatchEntity): Long = matchDao.insertMatch(match)
    suspend fun updateMatch(match: MatchEntity) = matchDao.updateMatch(match)
    suspend fun deleteMatch(id: Long) {
        ballDeliveryDao.deleteAllDeliveriesForMatch(id)
        matchDao.deleteMatch(id)
    }

    // Deliveries
    fun getAllDeliveries(): Flow<List<BallDeliveryEntity>> = ballDeliveryDao.getAllDeliveries()
    fun getDeliveriesForMatch(matchId: Long): Flow<List<BallDeliveryEntity>> =
        ballDeliveryDao.getDeliveriesForMatch(matchId)

    suspend fun getDeliveriesForMatchSnapshot(matchId: Long): List<BallDeliveryEntity> =
        ballDeliveryDao.getDeliveriesForMatchSnapshot(matchId)

    suspend fun insertDelivery(delivery: BallDeliveryEntity): Long =
        ballDeliveryDao.insertDelivery(delivery)

    suspend fun undoLastDelivery(matchId: Long) =
        ballDeliveryDao.deleteLastDelivery(matchId)

    // Player Rankings & Stats Calculation from real saved match data
    fun getPlayerRankingsFlow(): Flow<List<PlayerStats>> {
        return combine(
            playerDao.getAllPlayers(),
            matchDao.getAllMatches(),
            ballDeliveryDao.getAllDeliveries()
        ) { players, matches, deliveries ->
            computePlayerStats(players, matches, deliveries)
        }
    }

    suspend fun getPlayerStatsSnapshot(): List<PlayerStats> = withContext(Dispatchers.Default) {
        val players = playerDao.getAllPlayers()
        val matches = matchDao.getAllMatches()
        val deliveries = ballDeliveryDao.getAllDeliveriesSnapshot()
        // Compute from snapshot
        computePlayerStats(emptyList(), emptyList(), deliveries)
    }

    private fun computePlayerStats(
        players: List<PlayerEntity>,
        matches: List<MatchEntity>,
        deliveries: List<BallDeliveryEntity>
    ): List<PlayerStats> {
        val playerStatsMap = mutableMapOf<String, PlayerStats>()

        // Initialize from players roster if available
        players.forEach { p ->
            playerStatsMap[p.fullName] = PlayerStats(
                playerId = p.id,
                playerName = p.fullName
            )
        }

        // Group deliveries by match and player to accurately track innings, scores, bowling
        val matchGroups = deliveries.groupBy { it.matchId }

        for ((_, matchDeliveries) in matchGroups) {
            // Batting innings in this match
            val batterDeliveries = matchDeliveries.groupBy { it.strikerName }
            for ((batterName, bBalls) in batterDeliveries) {
                if (batterName.isBlank()) continue
                val existing = playerStatsMap[batterName] ?: PlayerStats(playerName = batterName)
                val inningsRuns = bBalls.sumOf { it.runsOffBat }
                val ballsFaced = bBalls.count { it.extrasType != ExtrasType.WIDE }
                val fours = bBalls.count { it.runsOffBat == 4 }
                val sixes = bBalls.count { it.runsOffBat == 6 }

                // Check if batter was dismissed in this match
                val wasDismissed = matchDeliveries.any { it.isWicket && it.dismissedPlayerName == batterName }
                val newNotOuts = existing.notOuts + (if (!wasDismissed) 1 else 0)

                playerStatsMap[batterName] = existing.copy(
                    matchesPlayed = existing.matchesPlayed + 1,
                    inningsBatted = existing.inningsBatted + 1,
                    runs = existing.runs + inningsRuns,
                    highestScore = maxOf(existing.highestScore, inningsRuns),
                    notOuts = newNotOuts,
                    fours = existing.fours + fours,
                    sixes = existing.sixes + sixes,
                    fifties = existing.fifties + (if (inningsRuns in 50..99) 1 else 0),
                    hundreds = existing.hundreds + (if (inningsRuns >= 100) 1 else 0),
                    ballsFaced = existing.ballsFaced + ballsFaced
                )
            }

            // Bowling innings in this match
            val bowlerDeliveries = matchDeliveries.groupBy { it.bowlerName }
            for ((bowlerName, bBalls) in bowlerDeliveries) {
                if (bowlerName.isBlank()) continue
                val existing = playerStatsMap[bowlerName] ?: PlayerStats(playerName = bowlerName)
                val legalBalls = bBalls.count { it.isLegalDelivery }
                val runsConceded = bBalls.sumOf { b ->
                    b.runsOffBat + when (b.extrasType) {
                        ExtrasType.WIDE, ExtrasType.NO_BALL, ExtrasType.PENALTY -> b.extrasRuns
                        else -> 0
                    }
                }
                val matchWickets = bBalls.count { it.isWicket && it.wicketType.isBowlerWicket }

                // Check best bowling figures
                val isBetterBowling = matchWickets > existing.bestBowlingWickets ||
                        (matchWickets == existing.bestBowlingWickets && runsConceded < existing.bestBowlingRuns && matchWickets > 0)

                val bestW = if (isBetterBowling) matchWickets else existing.bestBowlingWickets
                val bestR = if (isBetterBowling) runsConceded else existing.bestBowlingRuns

                playerStatsMap[bowlerName] = existing.copy(
                    legalBallsBowled = existing.legalBallsBowled + legalBalls,
                    runsConceded = existing.runsConceded + runsConceded,
                    wickets = existing.wickets + matchWickets,
                    bestBowlingWickets = bestW,
                    bestBowlingRuns = bestR
                )
            }
        }

        return playerStatsMap.values.toList()
    }

    // Export match data to JSON
    suspend fun exportMatchJson(matchId: Long): String = withContext(Dispatchers.IO) {
        val match = matchDao.getMatchById(matchId) ?: return@withContext "{}"
        val deliveries = ballDeliveryDao.getDeliveriesForMatchSnapshot(matchId)

        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())

            val matchObj = JSONObject().apply {
                put("id", match.id)
                put("matchTitle", match.matchTitle)
                put("tournamentName", match.tournamentName)
                put("season", match.season)
                put("homeTeamName", match.homeTeamName)
                put("awayTeamName", match.awayTeamName)
                put("venue", match.venue)
                put("matchDateMillis", match.matchDateMillis)
                put("format", match.format.name)
                put("totalOvers", match.totalOvers)
                put("playersPerSide", match.playersPerSide)
                put("tossWinnerTeam", match.tossWinnerTeam)
                put("tossDecision", match.tossDecision.name)
                put("battingFirstTeam", match.battingFirstTeam)
                put("currentInnings", match.currentInnings)
                put("targetRuns", match.targetRuns ?: JSONObject.NULL)
                put("isCompleted", match.isCompleted)
                put("winnerTeam", match.winnerTeam ?: JSONObject.NULL)
                put("winMargin", match.winMargin ?: JSONObject.NULL)
            }
            put("match", matchObj)

            val ballsArray = JSONArray()
            deliveries.forEach { b ->
                val bObj = JSONObject().apply {
                    put("inningsNumber", b.inningsNumber)
                    put("overNumber", b.overNumber)
                    put("legalBallInOver", b.legalBallInOver)
                    put("ballSequence", b.ballSequence)
                    put("bowlerName", b.bowlerName)
                    put("strikerName", b.strikerName)
                    put("nonStrikerName", b.nonStrikerName)
                    put("runsOffBat", b.runsOffBat)
                    put("extrasType", b.extrasType.name)
                    put("extrasRuns", b.extrasRuns)
                    put("totalBallRuns", b.totalBallRuns)
                    put("isLegalDelivery", b.isLegalDelivery)
                    put("isWicket", b.isWicket)
                    put("wicketType", b.wicketType.name)
                    put("dismissedPlayerName", b.dismissedPlayerName ?: JSONObject.NULL)
                    put("fielderPlayerName", b.fielderPlayerName ?: JSONObject.NULL)
                    put("commentary", b.commentary)
                    put("videoTimestampMs", b.videoTimestampMs ?: JSONObject.NULL)
                    put("timestampMillis", b.timestampMillis)
                }
                ballsArray.put(bObj)
            }
            put("deliveries", ballsArray)
        }
        root.toString(2)
    }

    // Import match data from JSON
    suspend fun importMatchJson(jsonString: String): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject(jsonString)
            val matchObj = root.getJSONObject("match")

            val matchEntity = MatchEntity(
                matchTitle = matchObj.getString("matchTitle"),
                tournamentName = matchObj.optString("tournamentName", ""),
                season = matchObj.optString("season", "2026"),
                homeTeamName = matchObj.getString("homeTeamName"),
                awayTeamName = matchObj.getString("awayTeamName"),
                venue = matchObj.optString("venue", "Cricket Ground"),
                matchDateMillis = matchObj.optLong("matchDateMillis", System.currentTimeMillis()),
                totalOvers = matchObj.getInt("totalOvers"),
                playersPerSide = matchObj.optInt("playersPerSide", 11),
                tossWinnerTeam = matchObj.optString("tossWinnerTeam", ""),
                battingFirstTeam = matchObj.optString("battingFirstTeam", ""),
                currentInnings = matchObj.optInt("currentInnings", 1),
                targetRuns = if (matchObj.has("targetRuns") && !matchObj.isNull("targetRuns")) matchObj.getInt("targetRuns") else null,
                isCompleted = matchObj.optBoolean("isCompleted", false),
                winnerTeam = if (matchObj.has("winnerTeam") && !matchObj.isNull("winnerTeam")) matchObj.getString("winnerTeam") else null,
                winMargin = if (matchObj.has("winMargin") && !matchObj.isNull("winMargin")) matchObj.getString("winMargin") else null,
                isDraft = false
            )

            val newMatchId = matchDao.insertMatch(matchEntity)

            val ballsArray = root.optJSONArray("deliveries")
            if (ballsArray != null) {
                val list = mutableListOf<BallDeliveryEntity>()
                for (i in 0 until ballsArray.length()) {
                    val b = ballsArray.getJSONObject(i)
                    list.add(
                        BallDeliveryEntity(
                            matchId = newMatchId,
                            inningsNumber = b.getInt("inningsNumber"),
                            overNumber = b.getInt("overNumber"),
                            legalBallInOver = b.getInt("legalBallInOver"),
                            ballSequence = b.getInt("ballSequence"),
                            bowlerName = b.getString("bowlerName"),
                            strikerName = b.getString("strikerName"),
                            nonStrikerName = b.getString("nonStrikerName"),
                            runsOffBat = b.getInt("runsOffBat"),
                            extrasType = runCatching { ExtrasType.valueOf(b.getString("extrasType")) }.getOrDefault(ExtrasType.NONE),
                            extrasRuns = b.getInt("extrasRuns"),
                            totalBallRuns = b.getInt("totalBallRuns"),
                            isLegalDelivery = b.getBoolean("isLegalDelivery"),
                            isWicket = b.getBoolean("isWicket"),
                            wicketType = runCatching { WicketType.valueOf(b.getString("wicketType")) }.getOrDefault(WicketType.NONE),
                            dismissedPlayerName = if (b.has("dismissedPlayerName") && !b.isNull("dismissedPlayerName")) b.getString("dismissedPlayerName") else null,
                            fielderPlayerName = if (b.has("fielderPlayerName") && !b.isNull("fielderPlayerName")) b.getString("fielderPlayerName") else null,
                            commentary = b.optString("commentary", ""),
                            videoTimestampMs = if (b.has("videoTimestampMs") && !b.isNull("videoTimestampMs")) b.getLong("videoTimestampMs") else null,
                            timestampMillis = b.optLong("timestampMillis", System.currentTimeMillis())
                        )
                    )
                }
                ballDeliveryDao.insertDeliveries(list)
            }
            newMatchId
        }
    }
}
