package com.example.selliaapp.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selliaapp.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val monthTotal: Double = 0.0,
    val isLoading: Boolean = false
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val sum = invoiceRepo.sumThisMonth()
            _state.value = HomeUiState(monthTotal = sum, isLoading = false)
        }
    }
}
