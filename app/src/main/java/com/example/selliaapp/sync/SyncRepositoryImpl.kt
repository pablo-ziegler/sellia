
package com.example.selliaapp.sync

/* [NUEVO] Implementación base sin dependencias (placeholder). Inyectá DAOs/APIs reales luego. */
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    // TODO: DAOs/APIs reales acá, p.ej. private val productDao: ProductDao, private val api: SelliaApi
) : SyncRepository {
    override suspend fun pushPending() { /* TODO subir pendientes */ }
    override suspend fun pullRemote() { /* TODO bajar/aplicar cambios */ }
}
