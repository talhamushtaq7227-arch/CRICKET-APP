package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "ball_deliveries",
    indices = [
        Index(value = ["matchId", "inningsNumber", "ballSequence"]),
        Index(value = ["matchId"])
    ]
)
data class BallDeliveryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: Long,
    val inningsNumber: Int,
    val overNumber: Int, // 0-based over index (0 = 1st over)
    val legalBallInOver: Int, // 1..6 if legal, or 0 if illegal delivery
    val ballSequence: Int, // 1, 2, 3...
    val bowlerPlayerId: Long? = null,
    val bowlerName: String,
    val strikerPlayerId: Long? = null,
    val strikerName: String,
    val nonStrikerPlayerId: Long? = null,
    val nonStrikerName: String,
    val runsOffBat: Int = 0,
    val extrasType: ExtrasType = ExtrasType.NONE,
    val extrasRuns: Int = 0,
    val totalBallRuns: Int = 0,
    val isLegalDelivery: Boolean = true,
    val isWicket: Boolean = false,
    val wicketType: WicketType = WicketType.NONE,
    val dismissedPlayerId: Long? = null,
    val dismissedPlayerName: String? = null,
    val fielderPlayerId: Long? = null,
    val fielderPlayerName: String? = null,
    val videoTimestampMs: Long? = null,
    val commentary: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)
