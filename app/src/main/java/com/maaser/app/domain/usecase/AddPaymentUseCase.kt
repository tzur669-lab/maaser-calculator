package com.maaser.app.domain.usecase

import com.maaser.app.data.model.Transaction
import com.maaser.app.data.model.TransactionType
import com.maaser.app.data.repository.MaaserRepository
import java.util.UUID
import javax.inject.Inject

class AddPaymentUseCase @Inject constructor(
    private val repository: MaaserRepository
) {
    suspend operator fun invoke(
        amount: Double, destinationId: String?, destinationFreeText: String?, note: String?, date: Long
    ) {
        val now = System.currentTimeMillis()
        repository.insertTransaction(
            Transaction(
                id = UUID.randomUUID().toString(), type = TransactionType.PAYMENT,
                amount = amount, maaserAmount = 0.0, source = null,
                destinationId = destinationId,
                destinationFreeText = destinationFreeText?.takeIf { it.isNotBlank() },
                note = note?.takeIf { it.isNotBlank() },
                date = date, createdAt = now, updatedAt = now, isDeleted = false, isSynced = false
            )
        )
    }
}
