package com.example.selliaapp.data.model

import com.example.selliaapp.data.local.entity.ProductEntity


/**
 * Línea del carrito durante la venta.
 * Se usa solo en memoria (no es entidad Room).
 */
data class CartLine(
    val product: ProductEntity,
    val quantity: Int
)
