package com.example.selliaapp.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.selliaapp.data.csv.CsvUtils
import com.example.selliaapp.data.csv.ProductCsvImporter
import com.example.selliaapp.data.model.ImportResult
import com.example.selliaapp.di.IoDispatcher
import com.example.selliaapp.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * VM para el wizard de importación:
 * - preview: primeras filas
 * - dryRun: simulación (sin persistir)
 * - importing: bandera de importación real
 */
@HiltViewModel
class StockImportViewModel @Inject constructor(
    private val repo: ProductRepository,
    @IoDispatcher private val io: CoroutineDispatcher,   // <-- usar el qualifier, SIN valor default
    @ApplicationContext private val appContext: Context   // <-- calificado

) : ViewModel() {

    data class DryRunResult(
        val inserted: Int,
        val updated: Int,
        val errors: List<String>
    )

    private val _ui = MutableStateFlow("idle")
    val ui: StateFlow<String> = _ui

    private val _preview = MutableStateFlow<List<List<String>>>(emptyList())
    val preview: StateFlow<List<List<String>>> = _preview

    private val _dryRun = MutableStateFlow<DryRunResult?>(null)
    val dryRun: StateFlow<DryRunResult?> = _dryRun

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing

    private var currentUri: Uri? = null
    private var cachedRows: List<ProductCsvImporter.Row> = emptyList()

    fun loadPreview(uri: Uri) {
        currentUri = uri
        viewModelScope.launch {
            appContext.contentResolver.openInputStream(uri)?.use { ins ->
                _preview.value = CsvUtils.readAll(ins).take(20)
            }
        }
    }

    fun runDryRun() {
        viewModelScope.launch {
            val uri = currentUri ?: return@launch
            val (inserted, updated, errors) = repo.simulateImport(appContext, uri)
            _dryRun.value = DryRunResult(inserted, updated, errors)
        }
    }

    fun doImport() {
        viewModelScope.launch {
            _importing.value = true
            val uri = currentUri ?: return@launch
            repo.importProductsInBackground(appContext, uri) // WorkManager
            _importing.value = false
        }
    }

    /**
     * Importa desde un CSV referenciado por [uri].
     * Usa la estrategia que le pasás desde la UI (Append / Upsert).
     *
     * @param context Se usa sólo para resolver el ContentResolver.
     * @param uri Uri del documento CSV (Storage Access Framework).
     * @param strategy Estrategia de importación (Append por defecto).
     * @param onCompleted Callback en Main con el resultado (ImportResult).
     */
    fun importFromCsv(
        context: Context,
        uri: Uri,
        strategy: ProductRepository.ImportStrategy = ProductRepository.ImportStrategy.Append,
        onCompleted: (ImportResult) -> Unit
    ) {
        val resolver: ContentResolver = context.contentResolver
        viewModelScope.launch(io) {
            val result = repo.importFromCsv(resolver, uri, strategy)
            withContext(Dispatchers.Main) {
                onCompleted(result)
            }
        }
    }


}
