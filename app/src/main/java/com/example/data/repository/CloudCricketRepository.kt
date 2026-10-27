package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CloudCricketRepository(context: Context) {

    private val db: FirebaseFirestore = try {
        val dbId = context.applicationContext.getString(R.string.firestore_database_id).trim()
        if (dbId.isNotEmpty() && dbId != "(default)") {
            FirebaseFirestore.getInstance(dbId)
        } else {
            FirebaseFirestore.getInstance()
        }
    } catch (e: Exception) {
        FirebaseFirestore.getInstance()
    }

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google to access Cloud Firestore.")
    }

    suspend fun backupAllDataToCloud(
        players: List<PlayerEntity>,
        matches: List<MatchEntity>,
        fetchDeliveriesForMatch: suspend (Long) -> List<BallDeliveryEntity>
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = requireUserId()
            var count = 0

            val userRef = db.collection("users").document(uid)
            userRef.set(
                mapOf(
                    "userId" to uid,
                    "displayName" to (Firebase.auth.currentUser?.displayName ?: "Cricket Captain"),
                    "email" to (Firebase.auth.currentUser?.email ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val squadCollection = userRef.collection("squad_players")
            for (player in players) {
                val playerDoc = squadCollection.document("player_${player.id}")
                val payload = mapOf(
                    "userId" to uid,
                    "localId" to player.id,
                    "fullName" to player.fullName,
                    "jerseyNumber" to player.jerseyNumber,
                    "role" to player.role.name,
                    "battingHand" to player.battingHand.name,
                    "bowlingStyle" to player.bowlingStyle.name,
                    "notes" to (player.notes ?: ""),
                    "isActive" to player.isActive,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                playerDoc.set(payload).await()
                count++
            }

            val matchesCollection = userRef.collection("matches")
            for (match in matches) {
                val matchDoc = matchesCollection.document("match_${match.id}")
                val matchPayload = mapOf(
                    "userId" to uid,
                    "localId" to match.id,
                    "matchTitle" to match.matchTitle,
                    "tournamentName" to match.tournamentName,
                    "homeTeamName" to match.homeTeamName,
                    "awayTeamName" to match.awayTeamName,
                    "venue" to match.venue,
                    "matchDateMillis" to match.matchDateMillis,
                    "format" to match.format.name,
                    "totalOvers" to match.totalOvers,
                    "playersPerSide" to match.playersPerSide,
                    "tossWinnerTeam" to match.tossWinnerTeam,
                    "tossDecision" to match.tossDecision.name,
                    "battingFirstTeam" to match.battingFirstTeam,
                    "currentInnings" to match.currentInnings,
                    "targetRuns" to (match.targetRuns ?: 0),
                    "isCompleted" to match.isCompleted,
                    "winnerTeam" to (match.winnerTeam ?: ""),
                    "winMargin" to (match.winMargin ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                matchDoc.set(matchPayload).await()
                count++

                val deliveries = fetchDeliveriesForMatch(match.id)
                val deliveriesCollection = matchDoc.collection("deliveries")
                for (delivery in deliveries) {
                    val deliveryDoc = deliveriesCollection.document("ball_${delivery.ballSequence}")
                    val deliveryPayload = mapOf(
                        "userId" to uid,
                        "inningsNumber" to delivery.inningsNumber,
                        "overNumber" to delivery.overNumber,
                        "legalBallInOver" to delivery.legalBallInOver,
                        "ballSequence" to delivery.ballSequence,
                        "bowlerName" to delivery.bowlerName,
                        "strikerName" to delivery.strikerName,
                        "nonStrikerName" to delivery.nonStrikerName,
                        "runsOffBat" to delivery.runsOffBat,
                        "extrasType" to delivery.extrasType.name,
                        "extrasRuns" to delivery.extrasRuns,
                        "totalBallRuns" to delivery.totalBallRuns,
                        "isLegalDelivery" to delivery.isLegalDelivery,
                        "isWicket" to delivery.isWicket,
                        "wicketType" to delivery.wicketType.name,
                        "dismissedPlayerName" to (delivery.dismissedPlayerName ?: ""),
                        "commentary" to delivery.commentary,
                        "timestampMillis" to delivery.timestampMillis
                    )
                    deliveryDoc.set(deliveryPayload).await()
                }
            }

            count
        }
    }

    suspend fun restoreDataFromCloud(
        localRepository: CricketRepository? = null,
        existingPlayerNames: Set<String> = emptySet()
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            val squadSnapshot = userRef.collection("squad_players").get().await()
            val matchesSnapshot = userRef.collection("matches").get().await()

            var restoredCount = 0
            val seenNames = existingPlayerNames.map { it.lowercase() }.toMutableSet()

            if (localRepository != null) {
                for (doc in squadSnapshot.documents) {
                    val fullName = doc.getString("fullName")?.trim().orEmpty()
                    if (fullName.isNotEmpty() && !seenNames.contains(fullName.lowercase())) {
                        val jersey = (doc.getLong("jerseyNumber") ?: 0L).toInt()
                        val roleStr = doc.getString("role") ?: PlayerRole.ALL_ROUNDER.name
                        val batStr = doc.getString("battingHand") ?: BattingHand.RIGHT_HAND.name
                        val bowlStr = doc.getString("bowlingStyle") ?: BowlingStyle.RIGHT_ARM_MEDIUM.name
                        val notes = doc.getString("notes")
                        val isActive = doc.getBoolean("isActive") ?: true

                        val player = PlayerEntity(
                            fullName = fullName,
                            jerseyNumber = jersey,
                            role = runCatching { PlayerRole.valueOf(roleStr) }.getOrDefault(PlayerRole.ALL_ROUNDER),
                            battingHand = runCatching { BattingHand.valueOf(batStr) }.getOrDefault(BattingHand.RIGHT_HAND),
                            bowlingStyle = runCatching { BowlingStyle.valueOf(bowlStr) }.getOrDefault(BowlingStyle.RIGHT_ARM_MEDIUM),
                            notes = notes,
                            isActive = isActive
                        )
                        localRepository.insertPlayer(player)
                        seenNames.add(fullName.lowercase())
                        restoredCount++
                    }
                }
            }

            Log.d(
                "CloudCricketRepository",
                "Cloud sync verified ${squadSnapshot.size()} players (${restoredCount} newly imported) and ${matchesSnapshot.size()} matches"
            )
            squadSnapshot.size() + matchesSnapshot.size()
        }
    }
}
