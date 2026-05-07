package com.maaser.app.ui.addpayment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maaser.app.data.model.PaymentDestination
import com.maaser.app.data.repository.MaaserRepository
import com.maaser.app.domain.usecase.AddPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPaymentViewModel @Inject constructor(
    private val addPaymentUseCase: AddPaymentUseCase,
    repository: MaaserRepository
) : ViewModel() {
    val destinations: StateFlow<List<PaymentDestination>> =
        repository.getPaymentDestinations().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val isSaving: StateFlow<Boolean> = MutableStateFlow(false)

    fun save(amount: Double, destinationId: String?, destinationFreeText: String?, note: String?, date: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            (isSaving as MutableStateFlow).value = true
            addPaymentUseCase(amount, destinationId, destinationFreeText, note, date)
            isSaving.value = false
            onDone()
        }
    }
}
