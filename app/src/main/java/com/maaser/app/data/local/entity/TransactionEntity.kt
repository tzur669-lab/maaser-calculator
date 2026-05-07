package com.maaser.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val amount: Double,
    val maaserAmount: Double,
    val source: String?,
    val destinationId: String?,
    val destinationFreeText: String?,
    val note: String?,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val isSynced: Boolean = false
)
