 package com.example.selliaapp.repository.impl

 import androidx.room.withTransaction // <-- IMPORTANTE: withTransaction suspend de Room KTX
 import com.example.selliaapp.data.AppDatabase
import com.example.selliaapp.data.dao.CustomerDao
import com.example.selliaapp.data.dao.InvoiceDao
import com.example.selliaapp.data.dao.InvoiceWithItems
import com.example.selliaapp.data.dao.ProductDao
 import com.example.selliaapp.data.local.entity.StockMovementEntity
 import com.example.selliaapp.data.model.Invoice
import com.example.selliaapp.data.model.InvoiceItem
import com.example.selliaapp.data.model.sales.InvoiceDetail
import com.example.selliaapp.data.model.sales.InvoiceDraft
import com.example.selliaapp.data.model.sales.InvoiceItemRow
import com.example.selliaapp.data.model.sales.InvoiceResult
import com.example.selliaapp.data.model.sales.InvoiceSummary
import com.example.selliaapp.di.IoDispatcher
import com.example.selliaapp.repository.InvoiceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
 import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

 @Singleton
 class InvoiceRepositoryImpl @Inject constructor(
     private val db: AppDatabase,
     private val invoiceDao: InvoiceDao,
     private val productDao: ProductDao,
     private val customerDao: CustomerDao,
     @IoDispatcher private val io: CoroutineDispatcher
 ) : InvoiceRepository {

     // ----------------------------
     // Escritura principal
     // ----------------------------
     override suspend fun confirmInvoice(draft: InvoiceDraft): InvoiceResult = withContext(io) {
         val now = System.currentTimeMillis()
         val number = "A-" + now.toString().takeLast(6)

         db.withTransaction  {
             // 1) Insert invoice
             val invId = invoiceDao.insertInvoice(
                 Invoice(
                     id = 0L,
                     dateMillis = now,
                     customerId = draft.customerId?.toInt(),
                     customerName = draft.customerName,
                     total = draft.total
                 )
             )

             // 2) Insert items
             val items = draft.items.map { li ->
                 InvoiceItem(
                     id = 0L,
                     invoiceId = invId,
                     productId = li.productId.toInt(),
                     productName = li.name,
                     quantity = li.quantity,
                     unitPrice = li.unitPrice,
                     lineTotal = li.quantity * li.unitPrice
                 )
             }
             invoiceDao.insertItems(items)

             // 3) Descontar stock y auditar movimiento (SALE)
             val movementDao = db.stockMovementDao()
             for (it in items) {
                 val affected = productDao.decrementStockIfEnough(
                     productId = it.productId,
                     qty = it.quantity
                 )
                 // Si querés que falle la venta cuando no hay stock suficiente:
                 require(affected == 1) { "Stock insuficiente o producto inexistente (id=${it.productId})" }

                 movementDao.insert(
                     StockMovementEntity(
                         productId = it.productId,
                         delta = -it.quantity,
                         reason = "SALE",
                         ts = Instant.ofEpochMilli(now),
                         user = null
                     )
                 )
             }
         }

         InvoiceResult(invoiceId = now, invoiceNumber = number)
     }

     // Compat con VMs viejos: versión plana
     override suspend fun addInvoiceAndAdjustStock(invoice: Invoice, items: List<InvoiceItem>) = withContext(io) {
         db.withTransaction  {
             val invId = invoiceDao.insertInvoice(invoice.copy(id = 0L))
             val itemsWithFk = items.map { it.copy(id = 0L, invoiceId = invId) }
             invoiceDao.insertItems(itemsWithFk)
             // [PENDIENTE] Ajuste de stock y última compra de cliente.
         }
     }

     // ----------------------------
     // Lecturas
     // ----------------------------
     override fun observeInvoicesWithItems(): Flow<List<InvoiceWithItems>> =
         invoiceDao.observeInvoicesWithItems()

     override suspend fun getInvoiceDetail(id: Long): InvoiceDetail? = withContext(io) {
         invoiceDao.getInvoiceWithItemsById(id)?.let { mapToDetail(it) }
     }

     // ----------------------------
     // Reporte simple
     // ----------------------------
     override suspend fun sumThisMonth(): Double = withContext(io) {
         val today = LocalDate.now()
         val start = today.withDayOfMonth(1)
             .atStartOfDay(ZoneId.systemDefault())
             .toInstant()
             .toEpochMilli()
         val end = today.plusMonths(1)
             .withDayOfMonth(1)
             .minusDays(1)
             .atTime(23, 59, 59)
             .atZone(ZoneId.systemDefault())
             .toInstant()
             .toEpochMilli()
         invoiceDao.sumTotalBetween(start, end)
     }

     // ----------------------------
     // Mappers
     // ----------------------------
     private fun mapToSummary(rel: InvoiceWithItems): InvoiceSummary {
         val inv = rel.invoice
         val ld = Instant.ofEpochMilli(inv.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
         return InvoiceSummary(
             id = inv.id,
             number = formatNumber(inv.id),
             customerName = inv.customerName ?: "Consumidor Final",
             date = ld,
             total = inv.total
         )
     }

     private fun mapToDetail(rel: InvoiceWithItems): InvoiceDetail {
         val inv = rel.invoice
         val itemsUi = rel.items.map {
             InvoiceItemRow(
                 productId = (it.productId ?: 0).toLong(),    // ajustá si tu entity usa Int?
                 name = it.productName ?: "(s/n)",
                 quantity = it.quantity,
                 unitPrice = it.unitPrice
             )
         }
         val ld = Instant.ofEpochMilli(inv.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
         return InvoiceDetail(
             id = inv.id,
             number = formatNumber(inv.id),
             customerName = inv.customerName ?: "Consumidor Final",
             date = ld,
             total = inv.total,
             items = itemsUi,
             notes = null
         )
     }

     private fun formatNumber(id: Long): String =
         "F-" + id.toString().padStart(8, '0')


     override fun observeAll(): Flow<List<InvoiceSummary>> =
         invoiceDao.observeInvoicesWithItems().map { list -> list.map { mapToSummary(it) } }

     // Búsqueda por cliente
     override fun observeInvoicesByCustomerQuery(query: String) =
         invoiceDao.observeInvoicesWithItemsByCustomerQuery(query)




 }
