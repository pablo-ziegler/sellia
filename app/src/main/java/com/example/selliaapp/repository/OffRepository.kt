package com.example.selliaapp.repository

import com.example.selliaapp.data.remote.off.OffProduct
import com.example.selliaapp.data.remote.off.OffResult

/**
 * [NUEVO] Interfaz única para OFF.
 * - getByBarcode → flujo tipado con OffResult (para UI/errores)
 * - getProductSuggestion → devuelve DTO crudo por si querés pre-llenar formularios
 */
interface OffRepository {
    suspend fun getByBarcode(barcode: String): OffResult
    suspend fun getProductSuggestion(barcode: String): OffProduct?
}
