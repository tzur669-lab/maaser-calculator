package com.maaser.app.data.model

import com.maaser.app.data.local.entity.TransactionEntity

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val maaserAmount: Double,
    val source: String?,
    val destinationId: String?,
    val destinationFreeText: String?,
    val note: String?,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val isSynced: Boolean
)

fun Transaction.toEntity() = TransactionEntity(
    id = id, type = type.name, amount = amount, maaserAmount = maaserAmount,
    source = source, destinationId = destinationId, destinationFreeText = destinationFreeText,
    note = note, date = date, createdAt = createdAt, updatedAt = updatedAt,
    isDeleted = isDeleted, isSynced = isSynced
)

fun TransactionEntity.toDomain() = Transaction(
    id = id, type = TransactionType.valueOf(type), amount = amount, maaserAmount = maaserAmount,
    source = source, destinationId = destinationId, destinationFreeText = destinationFreeText,
    note = note, date = date, createdAt = createdAt, updatedAt = updatedAt,
    isDeleted = isDeleted, isSynced = isSynced
)
