package com.maaser.app.di

import com.maaser.app.data.local.dao.PaymentDestinationDao
import com.maaser.app.data.local.dao.TransactionDao
import com.maaser.app.data.repository.MaaserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMaaserRepository(
        transactionDao: TransactionDao,
        destinationDao: PaymentDestinationDao
    ): MaaserRepository = MaaserRepository(transactionDao, destinationDao)
}
