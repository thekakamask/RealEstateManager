package com.dcac.realestatemanager.data.sync.poi

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.onlineDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PoiUploadManager(
    private val poiRepository: PoiRepository,                 // Local (Room) POI repository
    private val poiOnlineRepository: PoiOnlineRepository      // Remote (Firestore) POI repository
) {

    // Uploads all unsynced POIs to Firestore
    suspend fun syncUnSyncedPoiS(): List<SyncStatus> {
        // Get all POIs from Room that are not yet synced (isSynced = false)
        val unSyncedPoiS = poiRepository.getUnSyncedPoiS().first()
        val results = mutableListOf<SyncStatus>()             // To collect sync status

        for (poi in unSyncedPoiS) {
            try {
                // Update the timestamp for sync
                val updatedPoi = poi.copy(updatedAt = System.currentTimeMillis())

                // Upload to Firestore
                val syncedPoi = poiOnlineRepository.uploadPoi(updatedPoi, updatedPoi.id.toString())

                // Update local Room POI as synced
                poiRepository.updatePoi(syncedPoi)

                Log.d("PoiUploadManager", "Synced poi: ${poi.name}")
                results.add(SyncStatus.Success("Poi ${poi.id}"))

            } catch (e: Exception) {
                // Handle individual sync failure
                results.add(SyncStatus.Failure("Poi ${poi.id}", e))
            }
        }

        return results  // Return all success/failure sync results
    }
}
