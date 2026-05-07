package com.maaser.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maaser.app.data.local.dao.PaymentDestinationDao
import com.maaser.app.data.local.dao.TransactionDao
import com.maaser.app.data.local.entity.PaymentDestinationEntity
import com.maaser.app.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, PaymentDestinationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MaaserDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun paymentDestinationDao(): PaymentDestinationDao
}
