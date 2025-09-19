package com.example.selliaapp.repository

import com.example.selliaapp.data.dao.ExpenseRecordDao
import com.example.selliaapp.data.dao.ExpenseTemplateDao
import com.example.selliaapp.data.model.ExpenseRecord
import com.example.selliaapp.data.model.ExpenseStatus
import com.example.selliaapp.data.model.ExpenseTemplate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val tDao: ExpenseTemplateDao,
    private val rDao: ExpenseRecordDao
) {
    // Plantillas
    fun observeTemplates(): Flow<List<ExpenseTemplate>> = tDao.observeAll()
    suspend fun upsertTemplate(t: ExpenseTemplate) = tDao.upsert(t)
    suspend fun deleteTemplate(t: ExpenseTemplate) = tDao.delete(t)

    // Registros (instancias)
    fun observeRecords(
        name: String?,
        month: Int?,
        year: Int?,
        status: ExpenseStatus?
    ): Flow<List<ExpenseRecord>> = rDao.observeFiltered(name, month, year, status)

    suspend fun upsertRecord(r: ExpenseRecord) = rDao.upsert(r)
    suspend fun deleteRecord(r: ExpenseRecord) = rDao.delete(r)
}
