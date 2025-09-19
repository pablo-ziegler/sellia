package com.example.selliaapp.repository

import com.example.selliaapp.data.dao.CustomerDao
import com.example.selliaapp.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de clientes con búsqueda y borrado.
 */
@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun observeAll(): Flow<List<CustomerEntity>> = customerDao.observeAll()

    suspend fun upsert(c: CustomerEntity): Int {
        // Si es alta nueva (id=0), el createdAt ya viene con LocalDateTime.now() por default.
        // Si fuese un update, respetamos el createdAt existente.
        return customerDao.upsert(c)
    }

    /** Búsqueda por nombre/email/teléfono/apodo. */
    fun search(q: String): Flow<List<CustomerEntity>> = customerDao.search(q)

    /** Borrado de cliente. */
    suspend fun delete(c: CustomerEntity) = customerDao.delete(c)

    // ---------- Métricas helpers ----------
    private fun ldtToMillis(ldt: LocalDateTime): Long =
        ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    suspend fun countBetween(start: LocalDateTime, end: LocalDateTime): Int =
        customerDao.countBetweenMillis(ldtToMillis(start), ldtToMillis(end))

    suspend fun countToday(now: LocalDateTime = LocalDateTime.now()): Int {
        val start = now.toLocalDate().atStartOfDay()
        return countBetween(start, now)
    }

    suspend fun countThisWeek(now: LocalDateTime = LocalDateTime.now()): Int {
        val dow = now.toLocalDate().dayOfWeek.value  // 1..7 (Lunes=1)
        val start = now.toLocalDate().minusDays((dow - 1).toLong()).atStartOfDay()
        return countBetween(start, now)
    }

    suspend fun countThisMonth(now: LocalDateTime = LocalDateTime.now()): Int {
        val start = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        return countBetween(start, now)
    }

    suspend fun countThisYear(now: LocalDateTime = LocalDateTime.now()): Int {
        val start = LocalDate.of(now.year, 1, 1).atStartOfDay()
        return countBetween(start, now)
    }
}
