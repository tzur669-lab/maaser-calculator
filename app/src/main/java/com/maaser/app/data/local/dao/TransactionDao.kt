package com.maaser.app.data.local.dao

import androidx.room.*
import com.maaser.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllActive(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    suspend fun getAllActiveList(): List<TransactionEntity>

    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN maaserAmount ELSE 0 END), 0)
            - COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END), 0)
        FROM transactions
        WHERE isDeleted = 0
    """)
    fun getBalance(): Flow<Double>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Query("UPDATE transactions SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND date BETWEEN :from AND :to ORDER BY date ASC")
    suspend fun getForExport(from: Long, to: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?
}
