package com.maaser.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maaser.app.domain.usecase.GetBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SyncStatus { IDLE, SYNCING, SUCCESS, ERROR }

@HiltViewModel
class MainViewModel @Inject constructor(
    getBalanceUseCase: GetBalanceUseCase
) : ViewModel() {
    val balance: StateFlow<Double> = getBalanceUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val syncStatus: StateFlow<SyncStatus> = MutableStateFlow(SyncStatus.IDLE)
}
