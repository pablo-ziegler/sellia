package com.example.selliaapp.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selliaapp.data.model.dashboard.DailySalesPoint
import com.example.selliaapp.data.model.dashboard.LowStockProduct
import com.example.selliaapp.repository.InvoiceRepository
import com.example.selliaapp.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val monthTotal: Double = 0.0,
    val weekSales: List<DailySalesPoint> = emptyList(),
    val isLoading: Boolean = false,
    val lowStockAlerts: List<LowStockProduct> = emptyList(),
    val errorMessage: String? = null
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val productRepo: ProductRepository
) : ViewModel() {

    companion object {
        private const val ALERT_LIMIT = 5
        private const val DIAS_SEMANA = 7
    }

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        observeLowStock()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val totalMes = async { invoiceRepo.sumThisMonth() }
                val serieSemana = async { invoiceRepo.salesLastDays(DIAS_SEMANA) }
                totalMes.await() to serieSemana.await()
            }.onSuccess { (totalMes, serieSemana) ->
                _state.update {
                    it.copy(
                        monthTotal = totalMes,
                        weekSales = serieSemana,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Error inesperado al cargar métricas"
                    )
                }
            }
        }
    }

    private fun observeLowStock(limit: Int = ALERT_LIMIT) {
        viewModelScope.launch {
            productRepo.lowStockAlerts(limit)
                .catch { error ->
                    _state.update {
                        it.copy(errorMessage = error.localizedMessage ?: "No fue posible cargar alertas de stock")
                    }
                }
                .collectLatest { alerts ->
                    _state.update { it.copy(lowStockAlerts = alerts) }
                }
        }
    }
}
