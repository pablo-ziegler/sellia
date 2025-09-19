package com.example.selliaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.selliaapp.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface StockMovementDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(m: StockMovementEntity): Long

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY ts DESC")
    fun observeByProduct(productId: Int): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE ts BETWEEN :fromTs AND :toTs ORDER BY ts DESC")
    fun observeByRange(fromTs: Instant, toTs: Instant): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY ts DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<StockMovementEntity>>
}
