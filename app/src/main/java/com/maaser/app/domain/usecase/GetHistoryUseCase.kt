package com.maaser.app.domain.usecase

import com.maaser.app.data.model.Transaction
import com.maaser.app.data.repository.MaaserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(private val repository: MaaserRepository) {
    operator fun invoke(): Flow<Map<String, List<Transaction>>> = repository.getTransactionsByMonth()
}
