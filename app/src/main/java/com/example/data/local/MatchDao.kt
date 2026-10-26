package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY matchDateMillis DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 1")
    fun getLatestUnfinishedMatch(): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE isCompleted = 1 ORDER BY matchDateMillis DESC")
    fun getCompletedMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: Long): MatchEntity?

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    fun observeMatchById(id: Long): Flow<MatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteMatch(id: Long)

    @Query("SELECT COUNT(*) FROM matches")
    suspend fun getMatchCount(): Int

    @Query("SELECT COUNT(*) FROM matches WHERE isCompleted = 1")
    suspend fun getCompletedMatchCount(): Int

    @Query("SELECT DISTINCT season FROM matches WHERE season != '' ORDER BY season DESC")
    fun getDistinctSeasons(): Flow<List<String>>

    @Query("SELECT DISTINCT tournamentName FROM matches WHERE tournamentName != '' ORDER BY tournamentName ASC")
    fun getDistinctTournaments(): Flow<List<String>>
}
