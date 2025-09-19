package com.example.selliaapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.selliaapp.data.local.entity.ProductEntity
import com.example.selliaapp.ui.state.CartItemUi
import com.example.selliaapp.ui.state.SellUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SellViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SellUiState())
    val state: StateFlow<SellUiState> = _state.asStateFlow()

    /** Agrega un producto acumulando, respetando stock (clamp 1..max). */
    fun addToCart(product: ProductEntity, qty: Int = 1) {
        _state.update { ui ->
            val max = product.quantity.coerceAtLeast(0)
            val unit = (product.finalPrice ?: product.price ?: 0.0)
            val current = ui.items.find { it.productId == product.id }
            val newItems =
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
            recalc(newItems, ui)  // ← devolvemos SellUiState
        }
    }

    /** Incrementa de a 1 (máximo: stock). */
    fun increment(productId: Int) {
        _state.update { ui ->
            val newItems = ui.items.map { item ->
                if (item.productId == productId)
                    item.copy(qty = (item.qty + 1).coerceAtMost(item.maxStock.coerceAtLeast(1)))
                else item
            }
            recalc(newItems, ui)
        }
    }

    /** Decrementa de a 1 (mínimo 1). */
    fun decrement(productId: Int) {
        _state.update { ui ->
            val newItems = ui.items.map { item ->
                if (item.productId == productId)
                    item.copy(qty = (item.qty - 1).coerceAtLeast(1))
                else item
            }
            recalc(newItems, ui)
        }
    }

    /** Fija una cantidad concreta (clamp 1..max). */
    fun updateQty(productId: Int, qty: Int) {
        _state.update { ui ->
            val newItems = ui.items.map { item ->
                if (item.productId == productId) {
                    val max = item.maxStock.coerceAtLeast(1)
                    item.copy(qty = qty.coerceIn(1, max))
                } else item
            }
            recalc(newItems, ui)
        }
    }

    /** Quita un ítem del carrito. */
    fun remove(productId: Int) {
        _state.update { ui ->
            val newItems = ui.items.filterNot { it.productId == productId }
            recalc(newItems, ui)
        }
    }

    /** Limpia el carrito y totales. */
    fun clear() {
        _state.value = SellUiState()
    }

    /**
     * Recalcula totales/validaciones y retorna un NUEVO estado.
     * No muta `_state`. Así se puede usar dentro de `update { ... }`.
     */
    private fun recalc(newItems: List<CartItemUi>, base: SellUiState): SellUiState {
        val subtotal = newItems.sumOf { it.unitPrice * it.qty }
        val total = subtotal
        val violations = newItems
            .filter { it.qty > it.maxStock }
            .associate { it.productId to it.maxStock }

        // canCheckout ahora es calculado en SellUiState
        return base.copy(
            items = newItems,
            subtotal = subtotal,
            total = total,
            stockViolations = violations
        )
    }
}