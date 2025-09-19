package com.example.selliaapp.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager


object SyncScheduler {
    fun enqueueNow(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val req = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SyncWorker.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SyncWorker.UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                req
            )
    }
}