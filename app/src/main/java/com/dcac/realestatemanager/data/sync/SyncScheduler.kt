package com.dcac.realestatemanager.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class SyncScheduler(
    private val context: Context
) {
    fun scheduleSync() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.d("SYNC", "No user, sync skipped")
            return
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SYNC_WORK_TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
    }

    fun schedulePeriodicSync() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.d("SYNC", "No user, sync skipped")
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSyncRequest =
        PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(PERIODIC_SYNC_WORK_TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )


    }

    companion object {
        const val UNIQUE_SYNC_WORK_NAME = "global_sync_work"
        const val SYNC_WORK_TAG = "sync_work"

        const val UNIQUE_PERIODIC_SYNC_WORK_NAME =
            "periodic_sync_work"

        const val PERIODIC_SYNC_WORK_TAG =
            "periodic_sync_work_tag"
    }
}