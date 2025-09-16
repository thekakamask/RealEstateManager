package com.dcac.realestatemanager.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dcac.realestatemanager.data.AppContainerProvider
import com.dcac.realestatemanager.network.NetworkMonitor

// Worker responsible for syncing data between offline Room DB and online Firebase.
// Runs in the background using WorkManager.
class SyncWorker(
    private val appContext: Context,  // Application context (not activity/fragment context)
    workerParams: WorkerParameters   // Parameters passed by WorkManager (e.g. input data, tags)
) : CoroutineWorker(appContext, workerParams) {

    // Main method that WorkManager runs
    override suspend fun doWork(): Result {
        val networkMonitor = NetworkMonitor(appContext) // Instantiate a helper to check network status

        // 1️⃣ Check network connection
        if (!networkMonitor.isConnected()) {
            Log.w("SyncWorker", "No internet connection. Sync aborted.")
            return Result.retry()    // Retry later if there's no network
        }

        return try {
            val container = (appContext.applicationContext as? AppContainerProvider)?.container
                ?: throw IllegalStateException("AppContainer not initialized")

            // 🆙 Upload all entities via global UploadManager
            val uploadResults = container.uploadManager.syncAll()

            // ⬇️ Download all entities via global DownloadManager
            val downloadResults = container.downloadManager.downloadAll()

            Log.d("SyncWorker", "Uploaded: ${uploadResults.size} items")
            Log.d("SyncWorker", "Downloaded: ${downloadResults.size} items")

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}