package com.maaser.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maaser.app.data.model.Transaction
import com.maaser.app.data.repository.MaaserRepository
import com.maaser.app.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase,
    private val repository: MaaserRepository
) : ViewModel() {

    val transactionsByMonth: StateFlow<Map<String, List<Transaction>>> =
        getHistoryUseCase().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun deleteTransaction(id: String) {
        viewModelScope.launch { repository.softDeleteTransaction(id) }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch { repository.updateTransaction(transaction) }
    }
}
