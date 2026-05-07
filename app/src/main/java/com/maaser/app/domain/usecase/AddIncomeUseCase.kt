package com.maaser.app.domain.usecase

import com.maaser.app.data.local.UserSettingsDataStore
import com.maaser.app.data.model.Transaction
import com.maaser.app.data.model.TransactionType
import com.maaser.app.data.repository.MaaserRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class AddIncomeUseCase @Inject constructor(
    private val repository: MaaserRepository,
    private val settingsDataStore: UserSettingsDataStore
) {
    suspend operator fun invoke(amount: Double, source: String?, note: String?, date: Long) {
        val percentage = settingsDataStore.settings.first().maaserPercentage
        val maaserAmount = Math.round(amount * (percentage / 100) * 100) / 100.0
        val now = System.currentTimeMillis()
        repository.insertTransaction(
            Transaction(
                id = UUID.randomUUID().toString(), type = TransactionType.INCOME,
                amount = amount, maaserAmount = maaserAmount,
                source = source?.takeIf { it.isNotBlank() }, destinationId = null,
                destinationFreeText = null, note = note?.takeIf { it.isNotBlank() },
                date = date, createdAt = now, updatedAt = now, isDeleted = false, isSynced = false
            )
        )
    }
}
