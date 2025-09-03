package com.dcac.realestatemanager.data.sync.propertyPoiCross

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyPoiCrossUploadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,             // Local Room repo
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository  // Firestore repo
) {

    // Uploads all unsynced cross-references from Room to Firestore
    suspend fun syncUnSyncedPropertyPoiCross(): List<SyncStatus> {
        // Get local crossRefs where isSynced = false
        val unSyncedPropertyPoiCross = propertyPoiCrossRepository.getUnSyncedPropertiesPoiSCross().first()
        val results = mutableListOf<SyncStatus>()  // List to store sync results

        for (crossRef in unSyncedPropertyPoiCross) {
            try {
                // Upload the crossRef to Firestore
                val syncedCrossRef = propertyPoiCrossOnlineRepository.uploadCrossRef(crossRef)

                // Update local entry to mark as synced
                propertyPoiCrossRepository.updateCrossRef(syncedCrossRef)

                Log.d("CrossSyncManager", "Synced cross: ${crossRef.propertyId}-${crossRef.poiId}")
                results.add(SyncStatus.Success("Cross ${crossRef.propertyId}-${crossRef.poiId}"))

            } catch (e: Exception) {
                // ❌ Failed to sync this crossRef
                results.add(SyncStatus.Failure("Cross ${crossRef.propertyId}-${crossRef.poiId}", e))
            }
        }

        return results  // Return the list of sync outcomes
    }
}
