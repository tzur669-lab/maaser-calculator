package com.maaser.app.data.model

import com.maaser.app.data.local.entity.PaymentDestinationEntity

data class PaymentDestination(
    val id: String,
    val name: String,
    val isDeleted: Boolean,
    val createdAt: Long
)

fun PaymentDestination.toEntity() = PaymentDestinationEntity(
    id = id, name = name, isDeleted = isDeleted, createdAt = createdAt
)

fun PaymentDestinationEntity.toDomain() = PaymentDestination(
    id = id, name = name, isDeleted = isDeleted, createdAt = createdAt
)
