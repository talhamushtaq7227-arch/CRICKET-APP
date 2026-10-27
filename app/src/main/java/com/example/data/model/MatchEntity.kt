package com.example.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchTitle: String,
    val tournamentName: String = "",
    val season: String = "2026",
    val homeTeamName: String = "Rajpoot Cricket Boys",
    val awayTeamName: String,
    val venue: String = "RCB Cricket Ground",
    val matchDateMillis: Long = System.currentTimeMillis(),
    val format: MatchFormat = MatchFormat.T20,
    val totalOvers: Int = 20,
    val playersPerSide: Int = 11,
    val tossWinnerTeam: String = "",
    val tossDecision: TossDecision = TossDecision.BAT,
    val battingFirstTeam: String = "",
    val currentInnings: Int = 1,
    val targetRuns: Int? = null,
    val isCompleted: Boolean = false,
    val winnerTeam: String? = null,
    val winMargin: String? = null,
    val isDraft: Boolean = true,
    val sourceVideoPath: String? = null,
    val exportedVideoPath: String? = null,
    val homeSquadPlayerIds: String = "", // Comma-separated IDs: "1,2,3"
    val awaySquadPlayerNames: String = "", // Comma-separated player names
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
