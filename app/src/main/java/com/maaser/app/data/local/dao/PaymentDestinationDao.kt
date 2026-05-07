package com.maaser.app.data.local.dao

import androidx.room.*
import com.maaser.app.data.local.entity.PaymentDestinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDestinationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(destination: PaymentDestinationEntity)

    @Query("SELECT * FROM payment_destinations WHERE isDeleted = 0 ORDER BY createdAt ASC")
    fun getAllActive(): Flow<List<PaymentDestinationEntity>>

    @Query("SELECT * FROM payment_destinations WHERE isDeleted = 0 ORDER BY createdAt ASC")
    suspend fun getAllActiveList(): List<PaymentDestinationEntity>

    @Query("UPDATE payment_destinations SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)
}
