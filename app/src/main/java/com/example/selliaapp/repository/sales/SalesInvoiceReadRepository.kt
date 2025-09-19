package com.example.selliaapp.repository.sales

import com.example.selliaapp.data.model.sales.InvoiceDetail
import com.example.selliaapp.data.model.sales.InvoiceSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de LECTURA para facturas de venta (no toca stock ni procesa ventas).
 * Mantiene separado tu InvoiceRepository (clase) actual.
 */
interface SalesInvoiceReadRepository {
    fun observeSummaries(): Flow<List<InvoiceSummary>>
    suspend fun getDetail(id: Long): InvoiceDetail?
}
