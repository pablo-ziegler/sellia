package com.example.selliaapp.viewmodel.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selliaapp.data.model.sales.CartItem
import com.example.selliaapp.data.model.sales.InvoiceDraft
import com.example.selliaapp.repository.CartRepository
import com.example.selliaapp.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {
    // [NUEVO] Configuración de impuestos simple (si tu lógica real difiere, ajustá)
    private val TAX_RATE = 0.0 // 0% por defecto; cambiar si aplican impuestos

    // [NUEVO] Estado de UI con items/subtotal/taxes/total
    val uiState: StateFlow<CheckoutUiState> =
        cartRepository.observeCart()
            .map { lines ->
                val subtotal = lines.sumOf { it.lineTotal }
                val taxes = subtotal * TAX_RATE
                val total = subtotal + taxes
                CheckoutUiState(
                    items = lines,
                    subtotal = subtotal,
                    taxes = taxes,
                    total = total
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CheckoutUiState.EMPTY
            )

    // -----------------------
    // Acciones del carrito
    // -----------------------
    fun addItem(productId: Long, name: String, unitPrice: Double, quantity: Int = 1) {
        viewModelScope.launch {
            cartRepository.add(productId, name, unitPrice, quantity)
        }
    }

    fun removeItem(productId: Long, quantity: Int = 1) {
        viewModelScope.launch {
            cartRepository.remove(productId, quantity)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clear()
        }
    }


// -----------------------
    // Confirmación de venta
    // -----------------------
    /**
     * Confirma la venta generando un InvoiceDraft a partir del estado actual del carrito
     * y delega en InvoiceRepository.confirmInvoice(draft).
     *
     * @param customerId    opcional, si ya tenés el ID del cliente (puede ser null)
     * @param customerName  opcional, nombre a mostrar (si no hay cliente seleccionado)
     */
    fun confirmSale(
        customerId: Long? = null,
        customerName: String? = null,
        onSuccess: (invoiceId: Long, invoiceNumber: String) -> Unit = { _, _ -> },
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val state = uiState.value

                // [NUEVO] armamos el InvoiceDraft con CartItem + totales
                val draft = InvoiceDraft(
                    items = state.items.map { it.toCartItem() },
                    subtotal = state.subtotal,
                    taxes = state.taxes,
                    total = state.total,
                    customerId = customerId,
                    customerName = customerName
                )

                val result = invoiceRepository.confirmInvoice(draft)
                cartRepository.clear()
                onSuccess(result.invoiceId, result.invoiceNumber)
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }


    fun cancelCheckout(onCanceled: () -> Unit) {
        viewModelScope.launch {
            // Podés decidir si limpiar o no; acá no limpiamos por si vuelve atrás
            onCanceled()
        }
    }
}


/**
 * [NUEVO] Estado de pantalla para el checkout, con todo lo que la UI requiere.
 */
data class CheckoutUiState(
    val items: List<CartRepository.CartLine>,
    val subtotal: Double,
    val taxes: Double,
    val total: Double
) {
    companion object {
        val EMPTY = CheckoutUiState(
            items = emptyList(),
            subtotal = 0.0,
            taxes = 0.0,
            total = 0.0
        )
    }
}



private fun CartRepository.CartLine.toCartItem(): CartItem =
    CartItem(
        productId = this.productId,
        name = this.name,
        quantity = this.quantity,
        unitPrice = this.unitPrice
    )