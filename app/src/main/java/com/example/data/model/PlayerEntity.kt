package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val jerseyNumber: Int = 0,
    val role: PlayerRole = PlayerRole.ALL_ROUNDER,
    val battingHand: BattingHand = BattingHand.RIGHT_HAND,
    val bowlingStyle: BowlingStyle = BowlingStyle.RIGHT_ARM_MEDIUM,
    val photoUri: String? = null,
    val notes: String? = null,
    val isCaptain: Boolean = false,
    val isViceCaptain: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
