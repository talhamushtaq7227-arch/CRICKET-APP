package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.BallDeliveryEntity
import com.example.data.model.MatchEntity
import com.example.data.model.PlayerEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CloudCricketRepository(context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )

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

            // 1. Back up user profile
            val userRef = db.collection("users").document(uid)
            userRef.set(
                mapOf(
                    "userId" to uid,
                    "displayName" to (Firebase.auth.currentUser?.displayName ?: "Cricket Fan"),
                    "email" to (Firebase.auth.currentUser?.email ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // 2. Back up squad players
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

            // 3. Back up matches and ball deliveries
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

                // Back up deliveries for this match
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

    suspend fun restoreDataFromCloud(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            val squadSnapshot = userRef.collection("squad_players").get().await()
            val matchesSnapshot = userRef.collection("matches").get().await()

            Log.d("CloudCricketRepository", "Restored ${squadSnapshot.size()} players and ${matchesSnapshot.size()} matches from cloud")
            squadSnapshot.size() + matchesSnapshot.size()
        }
    }
}
