package com.maaser.app.domain.usecase

import com.maaser.app.data.repository.MaaserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBalanceUseCase @Inject constructor(private val repository: MaaserRepository) {
    operator fun invoke(): Flow<Double> = repository.getBalance()
}
