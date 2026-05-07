package com.maaser.app.di

import android.content.Context
import androidx.room.Room
import com.maaser.app.data.local.MaaserDatabase
import com.maaser.app.data.local.dao.PaymentDestinationDao
import com.maaser.app.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MaaserDatabase =
        Room.databaseBuilder(context, MaaserDatabase::class.java, "maaser_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTransactionDao(db: MaaserDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun providePaymentDestinationDao(db: MaaserDatabase): PaymentDestinationDao =
        db.paymentDestinationDao()
}
