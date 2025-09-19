package com.example.selliaapp.viewmodel


import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.selliaapp.data.csv.ProductCsvImporter
import com.example.selliaapp.data.dao.ProductDao
import com.example.selliaapp.data.local.entity.ProductEntity
import com.example.selliaapp.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Estado UI de la pantalla Stock: lista filtrada, query y toggles
 */
data class StockUiState(
    val query: String = "",
    val onlyLowStock: Boolean = false,
    val onlyNoStock: Boolean = false,
    val products: List<ProductEntity> = emptyList()
)


@HiltViewModel
class StockViewModel @Inject constructor(
    private val productRepo: ProductRepository,
    private val productDao: ProductDao,
    app: Application
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        // Suscribimos el flujo base del repositorio y aplicamos filtros en memoria.
        viewModelScope.launch {
            productRepo.getProducts().collectLatest { list ->
                applyFiltersAndPublish(list)
            }
        }
    }

    fun onQueryChange(q: String) {
        _uiState.value = _uiState.value.copy(query = q)
        viewModelScope.launch {
            // Reaplicar sobre lo que haya en repo (ya llega por flow, pero forzamos recalcular con cache)
            val current = productRepo.cachedOrEmpty()
            applyFiltersAndPublish(current)
        }
    }

    fun onToggleLowStock() {
        _uiState.value = _uiState.value.copy(onlyLowStock = !_uiState.value.onlyLowStock)
        viewModelScope.launch {
            applyFiltersAndPublish(productRepo.cachedOrEmpty())
        }
    }

    fun onToggleNoStock() {
        _uiState.value = _uiState.value.copy(onlyNoStock = !_uiState.value.onlyNoStock)
        viewModelScope.launch {
            applyFiltersAndPublish(productRepo.cachedOrEmpty())
        }
    }

    /**
     * Importa productos/stock desde un CSV (Excel/Sheets exportan a CSV).
     * - Actualiza productos existentes por code/barcode si coincide
     * - Inserta nuevos si no existen
     */
    suspend fun importFromCsvUri(uri: Uri): Result<Unit> {
        return try {
            val ctx = getApplication<Application>()
            val rows = ctx.contentResolver.openInputStream(uri).use { input ->
                if (input == null) error("No se pudo abrir el archivo")
                ProductCsvImporter.parseCsv(input)
            }
            // Delegamos al repo la lógica de upsert masivo
            productRepo.bulkUpsert(rows)
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    // ---- Internos ----

    private fun applyFiltersAndPublish(source: List<ProductEntity>) {
        val st = _uiState.value
        val q = st.query.trim().lowercase()

        val filtered = source.asSequence()
            .filter { p ->
                if (q.isBlank()) true
                else (p.name.lowercase().contains(q)
                        || (p.code?.lowercase()?.contains(q) == true)
                        || (p.barcode?.lowercase()?.contains(q) == true))
            }
            .filter { p ->
                if (st.onlyNoStock) p.quantity == 0 else true
            }
            .filter { p ->
                if (st.onlyLowStock) {
                    val min = p.minStock ?: 0
                    p.quantity <= min
                } else true
            }
            .sortedWith(compareBy<ProductEntity> { it.name.lowercase() }.thenBy { it.code ?: "" })
            .toList()

        _uiState.value = st.copy(products = filtered)
    }



    /**
     * Lógica al confirmar el agregado por escaneo.
     * onSuccess: devuelve nombre o algún dato útil (si lo necesitás en el UI).
     */
    fun addStockByScan(
        barcode: String,
        qty: Int,
        onSuccess: (String?) -> Unit = {},
        onNotFound: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val product = productRepo.getByBarcodeOrNull(barcode)
                if (product == null) {
                    onNotFound()
                    return@launch
                }
                val ok = productRepo.increaseStockByBarcode(barcode, qty)
                if (ok) {
                    onSuccess(product.name)
                } else {
                    onError(IllegalStateException("No se pudo actualizar el stock"))
                }
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    // LÓGICA DE ESCÁNER INTELIGENTE (F3)
    data class ScanResult(
        val foundId: Int?,     // id del producto si existe
        val prefillBarcode: String?
    )

    suspend fun onScanBarcode(barcode: String): ScanResult {
        val existing = productDao.getByBarcodeOnce(barcode)
        return if (existing != null) {
            ScanResult(foundId = existing.id, prefillBarcode = null)
        } else {
            // No existe: navegamos a ALTA con barcode precargado
            ScanResult(foundId = null, prefillBarcode = barcode)
        }
    }


}
