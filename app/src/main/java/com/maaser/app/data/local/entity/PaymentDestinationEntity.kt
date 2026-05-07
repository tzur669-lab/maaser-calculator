package com.maaser.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_destinations")
data class PaymentDestinationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isDeleted: Boolean = false,
    val createdAt: Long
)
