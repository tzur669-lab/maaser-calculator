package com.maaser.app.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.maaser.app.data.local.UserSettingsDataStore
import com.maaser.app.data.model.AppLanguage
import com.maaser.app.data.model.PaymentDestination
import com.maaser.app.data.model.UserSettings
import com.maaser.app.data.remote.GoogleDriveRepository
import com.maaser.app.data.repository.MaaserRepository
import com.maaser.app.domain.usecase.SyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: UserSettingsDataStore,
    private val repository: MaaserRepository,
    private val syncUseCase: SyncUseCase,
    private val driveRepository: GoogleDriveRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val destinations: StateFlow<List<PaymentDestination>> = repository.getPaymentDestinations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    fun setMaaserPercentage(percentage: Double) {
        viewModelScope.launch { settingsDataStore.setMaaserPercentage(percentage) }
    }

    fun addDestination(name: String) {
        viewModelScope.launch {
            repository.insertPaymentDestination(
                PaymentDestination(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    isDeleted = false,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteDestination(id: String) {
        viewModelScope.launch { repository.softDeletePaymentDestination(id) }
    }

    fun setLanguage(language: AppLanguage, activity: Activity) {
        viewModelScope.launch {
            settingsDataStore.setAppLanguage(language)
            activity.recreate()
        }
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential).await()
                val user = firebaseAuth.currentUser ?: return@launch
                settingsDataStore.setGoogleSignedIn(true, user.uid, account.email)
                _isSyncing.value = true
                try {
                    syncUseCase()
                    _syncMessage.value = "sync_success"
                } catch (e: Exception) {
                    _syncMessage.value = null
                }
                _isSyncing.value = false
            } catch (e: ApiException) {
                _syncMessage.value = "sign_in_failed"
            }
        }
    }

    fun signOut(activity: Activity) {
        viewModelScope.launch {
            firebaseAuth.signOut()
            GoogleSignIn.getClient(
                activity,
                GoogleSignInOptions.DEFAULT_SIGN_IN
            ).signOut().await()
            settingsDataStore.setGoogleSignedIn(false, null, null)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                syncUseCase()
                _syncMessage.value = "sync_success"
            } catch (e: Exception) {
                _syncMessage.value = "sync_failed"
            }
            _isSyncing.value = false
        }
    }

    fun driveBackup() {
        viewModelScope.launch {
            _isSyncing.value = true
            val pct = settingsDataStore.settings.first().maaserPercentage
            val success = driveRepository.backup(pct)
            if (success) {
                settingsDataStore.setDriveLastBackupAt(System.currentTimeMillis())
                _syncMessage.value = "backup_success"
            } else {
                _syncMessage.value = "backup_failed"
            }
            _isSyncing.value = false
        }
    }

    fun driveRestore() {
        viewModelScope.launch {
            _isSyncing.value = true
            val data = driveRepository.restore()
            if (data != null) {
                data.transactions.forEach { repository.insertTransaction(it) }
                data.destinations.forEach { repository.insertPaymentDestination(it) }
                settingsDataStore.setMaaserPercentage(data.maaserPercentage)
                _syncMessage.value = "restore_success"
            } else {
                _syncMessage.value = "restore_failed"
            }
            _isSyncing.value = false
        }
    }

    fun clearMessage() {
        _syncMessage.value = null
    }
}
