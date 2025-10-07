package com.example.selliaapp.ui.state


/**
 * Item del carrito listo para UI. Guardamos el stock máximo (maxStock) para validar sin ir a DB.
 */
data class CartItemUi(
    val productId: Int,
    val name: String,
    val barcode: String?,
    val unitPrice: Double,
    val qty: Int,
    val maxStock: Int
) {
    val lineTotal: Double
        get() = unitPrice * qty
}

/**
 * Estado de la pantalla de venta.
 * - stockViolations: productId -> stock disponible (si qty > disponible).
 * - canCheckout: habilita/deshabilita el botón Vender.
 */
data class SellUiState(
    val items: List<CartItemUi> = emptyList(),
    val subtotal: Double = 0.0,
    val discountPercent: Int = 0,
    val discountAmount: Double = 0.0,
    val surchargePercent: Int = 0,
    val surchargeAmount: Double = 0.0,
    val total: Double = 0.0,
    /** Mapa de violaciones: productId -> stockDisponible (cuando qty > stock) */
    val stockViolations: Map<Int, Int> = emptyMap(),
    val paymentMethod: PaymentMethod = PaymentMethod.EFECTIVO,
    val paymentNotes: String = ""
) {
    /** Habilita el checkout si no hay violaciones y hay al menos un ítem. */
    val canCheckout: Boolean
        get() = stockViolations.isEmpty() && items.isNotEmpty()
}

enum class PaymentMethod {
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA
}
