package com.dcac.realestatemanager.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dcac.realestatemanager.data.AppContainerProvider
import com.dcac.realestatemanager.utils.NetworkMonitor
import kotlinx.coroutines.flow.first

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
            // 2️⃣ Retrieve the AppContainer via the AppContainerProvider interface from the Application context
            val container = (appContext.applicationContext as? AppContainerProvider)?.container
                ?: throw IllegalStateException("AppContainer not initialized")

            // 3️⃣ Launch upload sync for each entity (users, photos, POIs, cross-links, properties)
            val uploadResults = listOf(
                container.userUploadManager.syncUnSyncedUsers(),
                container.photoUploadManager.syncUnSyncedPhotos(),
                container.poiUploadManager.syncUnSyncedPoiS(),
                container.crossSyncManager.syncUnSyncedPropertyPoiCross(),
                container.propertyUploadManager.syncUnSyncedProperties()
            ).flatten() // Merge lists into a single list of SyncStatus

            val users = container.userRepository.getAllUsers().first()

            // 4️⃣ Launch download sync for each entity
            val downloadResults = listOf(
                container.userDownloadManager.downloadUnSyncedUsers(),
                container.photoDownloadManager.downloadUnSyncedPhotos(),
                container.poiDownloadManager.downloadUnSyncedPoiS(),
                container.propertyPoiCrossDownloadManager.downloadUnSyncedPropertyPoiCross(),
                container.propertyDownloadManager.downloadUnSyncedProperties(users)
            ).flatten()

            // 5️⃣ Log how many items were synced
            Log.d("SyncWorker", "Uploaded: ${uploadResults.size} items")
            Log.d("SyncWorker", "Downloaded: ${downloadResults.size} items")

            // 6️⃣ If everything went well, return success to WorkManager
            Result.success()
        } catch (e: Exception) {
            // 7️⃣ If an error occurred, log it and ask WorkManager to retry later
            Log.e("SyncWorker", "Sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}