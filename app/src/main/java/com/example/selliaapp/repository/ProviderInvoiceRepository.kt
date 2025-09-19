package com.example.selliaapp.repository


import com.example.selliaapp.data.dao.ProviderInvoiceDao
import com.example.selliaapp.data.dao.ProviderInvoiceWithItems
import com.example.selliaapp.data.model.ProviderInvoice
import com.example.selliaapp.data.model.ProviderInvoiceItem
import com.example.selliaapp.data.model.ProviderInvoiceStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderInvoiceRepository @Inject constructor(
    private val dao: ProviderInvoiceDao
) {
    fun observeByProvider(providerId: Int): Flow<List<ProviderInvoiceWithItems>> =
        dao.observeByProvider(providerId)

    fun observePending(): Flow<List<ProviderInvoiceWithItems>> =
        dao.observeByStatus(ProviderInvoiceStatus.IMPAGA)

    fun observeDetail(invoiceId: Int): Flow<ProviderInvoiceWithItems?> =
        dao.observeDetail(invoiceId)

    suspend fun create(
        invoice: ProviderInvoice,
        items: List<ProviderInvoiceItem>
    ): Long {
        val id = dao.insertInvoice(invoice)
        dao.insertItems(items.map { it.copy(invoiceId = id.toInt()) })
        return id
    }

    suspend fun markPaid(
        invoice: ProviderInvoice,
        ref: String,
        amount: Double,
        paymentDateMillis: Long
    ) {
        val updated = invoice.copy(
            status = ProviderInvoiceStatus.PAGA,
            paymentRef = ref,
            paymentAmount = amount,
            paymentDateMillis = paymentDateMillis
        )
        dao.updateInvoice(updated)
    }
}
