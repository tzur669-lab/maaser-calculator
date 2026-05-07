package com.maaser.app.domain.usecase

import com.maaser.app.data.local.UserSettingsDataStore
import com.maaser.app.data.remote.FirebaseRealtimeRepository
import com.maaser.app.data.repository.MaaserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val repository: MaaserRepository,
    private val remoteRepository: FirebaseRealtimeRepository,
    private val settingsDataStore: UserSettingsDataStore
) {
    suspend operator fun invoke() {
        val settings = settingsDataStore.settings.first()
        val userId = settings.userId ?: return

        val unsynced = repository.getUnsyncedTransactions()
        if (unsynced.isNotEmpty()) {
            remoteRepository.uploadTransactions(userId, unsynced)
            repository.markAsSynced(unsynced.map { it.id })
        }

        val remoteData = remoteRepository.getTransactionsSince(userId, settings.lastSyncAt)
        remoteData.forEach { map ->
            val remote = remoteRepository.toTransaction(map) ?: return@forEach
            val localList = repository.getUnsyncedTransactions()
            val local = localList.firstOrNull { it.id == remote.id }
            when {
                local == null -> repository.insertTransaction(remote)
                remote.updatedAt > local.updatedAt -> repository.updateTransaction(remote)
            }
        }
        settingsDataStore.setLastSyncAt(System.currentTimeMillis())
    }
}
