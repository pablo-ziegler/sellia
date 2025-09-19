package com.example.selliaapp.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker de sincronización inyectado por Hilt.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        // TODO: tu lógica de sync real:
        syncRepository.pushPending()
        syncRepository.pullRemote()
        Result.success()
    } catch (t: Throwable) {
        // Log.e(TAG, "Error en sync", t)
        Result.retry()
    }

    companion object {
        const val UNIQUE_NAME: String = "sync_work"
        const val TAG: String = "SyncWorker"
    }
}