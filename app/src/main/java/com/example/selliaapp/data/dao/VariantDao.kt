package com.example.selliaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.selliaapp.data.local.entity.VariantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VariantDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(v: VariantEntity): Long

    @Update
    suspend fun update(v: VariantEntity): Int

    @Query("SELECT * FROM variants WHERE productId = :productId ORDER BY option1, option2")
    fun observeByProduct(productId: Int): Flow<List<VariantEntity>>

    @Query("SELECT * FROM variants WHERE productId = :productId")
    suspend fun getByProductOnce(productId: Int): List<VariantEntity>

    @Query("SELECT SUM(quantity) FROM variants WHERE productId = :productId")
    suspend fun sumQuantity(productId: Int): Int?
}
