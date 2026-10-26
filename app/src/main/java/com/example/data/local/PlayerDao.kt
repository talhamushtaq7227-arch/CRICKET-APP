package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY fullName ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE isActive = 1 ORDER BY fullName ASC")
    fun getActivePlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    fun observePlayerById(id: Long): Flow<PlayerEntity?>

    @Query("SELECT * FROM players WHERE fullName = :name LIMIT 1")
    suspend fun getPlayerByName(name: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayers(players: List<PlayerEntity>): List<Long>

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("UPDATE players SET isActive = :isActive WHERE id = :id")
    suspend fun setPlayerActive(id: Long, isActive: Boolean)

    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deletePlayer(id: Long)

    @Query("SELECT COUNT(*) FROM players")
    suspend fun getPlayerCount(): Int
}
