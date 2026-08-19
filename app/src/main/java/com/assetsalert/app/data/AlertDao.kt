package com.assetsalert.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY id DESC")
    fun observeAll(): Flow<List<Alert>>

    @Query("SELECT * FROM alerts WHERE isActive = 1 AND isTriggered = 0")
    suspend fun getActiveUntriggered(): List<Alert>

    @Insert
    suspend fun insert(alert: Alert): Long

    @Update
    suspend fun update(alert: Alert)

    @Delete
    suspend fun delete(alert: Alert)

    @Query("UPDATE alerts SET isTriggered = 1 WHERE id = :id")
    suspend fun markTriggered(id: Long)

    @Query("UPDATE alerts SET lastKnownPrice = :price WHERE id = :id")
    suspend fun updatePrice(id: Long, price: Double)

    @Query("UPDATE alerts SET isTriggered = 0, isActive = 1 WHERE id = :id")
    suspend fun rearm(id: Long)
}
