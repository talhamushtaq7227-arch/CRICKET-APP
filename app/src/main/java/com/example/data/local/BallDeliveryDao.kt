package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BallDeliveryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BallDeliveryDao {
    @Query("SELECT * FROM ball_deliveries WHERE matchId = :matchId ORDER BY ballSequence ASC")
    fun getDeliveriesForMatch(matchId: Long): Flow<List<BallDeliveryEntity>>

    @Query("SELECT * FROM ball_deliveries WHERE matchId = :matchId ORDER BY ballSequence ASC")
    suspend fun getDeliveriesForMatchSnapshot(matchId: Long): List<BallDeliveryEntity>

    @Query("SELECT * FROM ball_deliveries WHERE matchId = :matchId AND inningsNumber = :innings ORDER BY ballSequence ASC")
    fun getDeliveriesForInnings(matchId: Long, innings: Int): Flow<List<BallDeliveryEntity>>

    @Query("SELECT * FROM ball_deliveries WHERE matchId = :matchId AND inningsNumber = :innings ORDER BY ballSequence DESC LIMIT 1")
    suspend fun getLastDeliveryForInnings(matchId: Long, innings: Int): BallDeliveryEntity?

    @Query("SELECT * FROM ball_deliveries WHERE matchId = :matchId ORDER BY ballSequence DESC LIMIT 1")
    suspend fun getLastDelivery(matchId: Long): BallDeliveryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: BallDeliveryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<BallDeliveryEntity>): List<Long>

    @Query("DELETE FROM ball_deliveries WHERE id = :id")
    suspend fun deleteDeliveryById(id: Long)

    @Query("DELETE FROM ball_deliveries WHERE matchId = :matchId AND id = (SELECT id FROM ball_deliveries WHERE matchId = :matchId ORDER BY ballSequence DESC LIMIT 1)")
    suspend fun deleteLastDelivery(matchId: Long)

    @Query("DELETE FROM ball_deliveries WHERE matchId = :matchId")
    suspend fun deleteAllDeliveriesForMatch(matchId: Long)

    @Query("SELECT * FROM ball_deliveries")
    fun getAllDeliveries(): Flow<List<BallDeliveryEntity>>

    @Query("SELECT * FROM ball_deliveries")
    suspend fun getAllDeliveriesSnapshot(): List<BallDeliveryEntity>
}
