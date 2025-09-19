package com.example.selliaapp.repository


import com.example.selliaapp.data.dao.InvoiceDao
import com.example.selliaapp.data.dao.ReportDataDao
import com.example.selliaapp.data.model.ReportPoint
import com.example.selliaapp.viewmodel.ReportsFilter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arma datos para los reportes a partir de InvoiceRepository.
 */
data class SalesReport(
    val total: Double,
    val series: List<Pair<String, Double>> // etiqueta (fecha) -> monto
)

@Singleton
class ReportsRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val reportDataDao: ReportDataDao? = null
) {

    suspend fun getSalesSeries(
        from: LocalDate,
        to: LocalDate,
        bucket: String
    ): List<ReportPoint> {
        val zone = ZoneId.systemDefault()
        val startMillis = from.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = to.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

        return if (bucket == "HOUR") {
            val rows = invoiceDao.salesGroupedByHour(startMillis, endMillis)
            val hourFmt = DateTimeFormatter.ofPattern("HH:mm")
            rows.map { row ->
                val ldt = Instant.ofEpochMilli(row.hour).atZone(zone).toLocalDateTime()
                ReportPoint(
                    label = ldt.format(hourFmt),
                    amount = row.total,
                    dateTime = ldt
                )
            }
        } else {
            val rows = invoiceDao.salesGroupedByDay(startMillis, endMillis)
            rows.map { row ->
                val date = Instant.ofEpochMilli(row.day).atZone(zone).toLocalDate()
                ReportPoint(
                    label = date.toString(),
                    amount = row.total,
                    date = date
                )
            }
        }
    }
    /**
     * API friendly para la UI: decide bucket según filtro.
     */
    suspend fun getSalesSeries(
        from: LocalDate,
        to: LocalDate,
        filter: ReportsFilter
    ): List<ReportPoint> {
        val bucket = when (filter) {
            ReportsFilter.DAY   -> "HOUR"
            ReportsFilter.WEEK  -> "DAY"
            ReportsFilter.MONTH -> "DAY"
        }
        return getSalesSeries(from, to, bucket)
    }

}
