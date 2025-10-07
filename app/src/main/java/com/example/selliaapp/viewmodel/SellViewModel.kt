package com.example.selliaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selliaapp.data.local.entity.ProductEntity
import com.example.selliaapp.repository.IProductRepository
import com.example.selliaapp.ui.state.CartItemUi
import com.example.selliaapp.ui.state.PaymentMethod
import com.example.selliaapp.ui.state.SellUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SellViewModel @Inject constructor(
    private val repo: IProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SellUiState())
    val state: StateFlow<SellUiState> = _state.asStateFlow()

    // ----- Escaneo: helper de resultado -----
    data class ScanResult(val foundId: Int?, val prefillBarcode: String)

    /**
     * Consulta si un barcode existe. Si existe -> foundId != null.
     * Si no existe -> devolvemos el mismo barcode para precargar en alta.
     */
    suspend fun onScanBarcode(barcode: String): ScanResult = withContext(Dispatchers.IO) {
        val p = repo.getByBarcodeOrNull(barcode)
        if (p != null) ScanResult(foundId = p.id, prefillBarcode = barcode)
        else ScanResult(foundId = null, prefillBarcode = barcode)
    }


    /**
     * Agrega al carrito por barcode (usado tras el diálogo de cantidad).
     * Callbacks:
     *  - onSuccess: agregado ok
     *  - onNotFound: si el producto no existe (carrera entre escaneo y alta)
     *  - onError: error inesperado (DB/IO)
     */
    fun addToCartByScan(
        barcode: String,
        qty: Int,
        onSuccess: () -> Unit = {},
        onNotFound: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val p = withContext(Dispatchers.IO) { repo.getByBarcodeOrNull(barcode) }
                if (p == null) {
                    onNotFound()
                } else {
                    addToCart(p, qty)
                    onSuccess()
                }
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    /** Agrega un producto acumulando, respetando stock (clamp 1..max). */
    fun addToCart(product: ProductEntity, qty: Int = 1) {
        _state.update { ui ->
            val max = (product.quantity ?: 0).coerceAtLeast(0)
            val unit = (product.finalPrice ?: product.price ?: 0.0)
            val current = ui.items.find { it.productId == product.id }
            val nuevosItems =
                if (current == null) {
                    val q = qty.coerceIn(1, max.coerceAtLeast(1))
                    ui.items + CartItemUi(
                        productId = product.id,
                        name = product.name,
                        barcode = product.barcode,
                        unitPrice = unit,
                        qty = q,
                        maxStock = max
                    )
                } else {
                    val q = (current.qty + qty).coerceIn(1, max.coerceAtLeast(1))
                    ui.items.map { item ->
                        if (item.productId == product.id) item.copy(qty = q, maxStock = max) else item
                    }
                }
            recalc(ui, nuevosItems)
        }
    }

    /** Incrementa de a 1 (máximo: stock). */
    fun increment(productId: Int) {
        _state.update { ui ->
            val nuevosItems = ui.items.map { item ->
                if (item.productId == productId)
                    item.copy(qty = (item.qty + 1).coerceAtMost(item.maxStock.coerceAtLeast(1)))
                else item
            }
            recalc(ui, nuevosItems)
        }
    }

    /** Decrementa de a 1 (mínimo 1). */
    fun decrement(productId: Int) {
        _state.update { ui ->
            val nuevosItems = ui.items.map { item ->
                if (item.productId == productId)
                    item.copy(qty = (item.qty - 1).coerceAtLeast(1))
                else item
            }
            recalc(ui, nuevosItems)
        }
    }

    /** Fija una cantidad concreta (clamp 1..max). */
    fun updateQty(productId: Int, qty: Int) {
        _state.update { ui ->
            val nuevosItems = ui.items.map { item ->
                if (item.productId == productId) {
                    val max = item.maxStock.coerceAtLeast(1)
                    item.copy(qty = qty.coerceIn(1, max))
                } else item
            }
            recalc(ui, nuevosItems)
        }
    }

    /** Quita un ítem del carrito. */
    fun remove(productId: Int) {
        _state.update { ui ->
            val nuevosItems = ui.items.filterNot { it.productId == productId }
            recalc(ui, nuevosItems)
        }
    }

    /** Limpia el carrito y totales. */
    fun clear() {
        _state.value = SellUiState()
    }

    // --- Recalcula totales/validaciones y retorna nuevo estado (inmutable) ---
    private fun recalc(base: SellUiState, nuevosItems: List<CartItemUi>? = null): SellUiState {
        val items = nuevosItems ?: base.items
        val subtotal = items.sumOf { it.unitPrice * it.qty }
        val violations = items
            .filter { it.qty > it.maxStock }
            .associate { it.productId to it.maxStock }
        val descuento = subtotal * base.discountPercent / 100.0
        val baseConDescuento = subtotal - descuento
        val recargo = baseConDescuento * base.surchargePercent / 100.0
        val total = baseConDescuento + recargo

        return base.copy(
            items = items,
            subtotal = subtotal,
            discountAmount = descuento,
            surchargeAmount = recargo,
            total = total,
            stockViolations = violations
        )
    }

    fun setDiscountPercent(percent: Int) {
        _state.update { ui ->
            val nuevoValor = percent.coerceIn(0, 100)
            recalc(ui.copy(discountPercent = nuevoValor))
        }
    }

    fun setSurchargePercent(percent: Int) {
        _state.update { ui ->
            val nuevoValor = percent.coerceIn(0, 100)
            recalc(ui.copy(surchargePercent = nuevoValor))
        }
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        _state.update { ui ->
            ui.copy(paymentMethod = method)
        }
    }

    fun updatePaymentNotes(notes: String) {
        _state.update { ui ->
            ui.copy(paymentNotes = notes.take(280))
        }
    }
    /**
     * [NUEVO] Finaliza la venta de forma local:
     * - Si hay violaciones de stock lanza excepción.
     * - (TODO) Persistir venta en DB/Firestore si ya tenés entidades.
     * - Limpia el carrito y devuelve un id simbólico.
     */
    fun placeOrder(): CheckoutResult {
        val current = state.value
        require(current.stockViolations.isEmpty()) { "Hay ítems con falta de stock." }
        val orderId = UUID.randomUUID().toString()
        val resultado = CheckoutResult(
            id = orderId,
            total = current.total,
            paymentMethod = current.paymentMethod,
            discountPercent = current.discountPercent,
            surchargePercent = current.surchargePercent,
            notes = current.paymentNotes
        )
        clear()
        return resultado
    }
}

data class CheckoutResult(
    val id: String,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val discountPercent: Int,
    val surchargePercent: Int,
    val notes: String
)
