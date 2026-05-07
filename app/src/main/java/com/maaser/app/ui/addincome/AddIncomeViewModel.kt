package com.maaser.app.ui.addincome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maaser.app.domain.usecase.AddIncomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddIncomeViewModel @Inject constructor(
    private val addIncomeUseCase: AddIncomeUseCase
) : ViewModel() {
    val isSaving: StateFlow<Boolean> = MutableStateFlow(false)

    fun save(amount: Double, source: String?, note: String?, date: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            (isSaving as MutableStateFlow).value = true
            addIncomeUseCase(amount, source, note, date)
            isSaving.value = false
            onDone()
        }
    }
}
