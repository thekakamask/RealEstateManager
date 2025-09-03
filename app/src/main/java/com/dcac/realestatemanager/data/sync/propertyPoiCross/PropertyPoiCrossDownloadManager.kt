package com.dcac.realestatemanager.data.sync.propertyPoiCross

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyPoiCrossDownloadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,             // Local Room repo
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository  // Firestore repo
) {

    // Downloads all cross-references (Property ↔ POI) from Firestore to Room
    suspend fun downloadUnSyncedPropertyPoiCross(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()  // List of success/failure logs

        try {
            // Fetch all cross-references from Firestore
            val onlineCrossRefs = propertyPoiCrossOnlineRepository.getAllCrossRefs()

            for (crossRef in onlineCrossRefs) {
                try {
                    // Check if the crossRef exists locally
                    val localCross = propertyPoiCrossRepository
                        .getCrossByIds(crossRef.propertyId, crossRef.poiId)
                        .first()

                    if (localCross == null) {
                        // 🔽 New link: insert it into Room
                        propertyPoiCrossRepository.cacheCrossRefFromFirebase(
                            crossRef.copy(isSynced = true)
                        )
                        Log.d("CrossDL", "Inserted crossRef: ${crossRef.propertyId}-${crossRef.poiId}")
                        results.add(SyncStatus.Success("Inserted ${crossRef.propertyId}-${crossRef.poiId}"))

                    } else if (!localCross.isSynced) {
                        // 🔁 Existing but not marked as synced ➜ update it
                        propertyPoiCrossRepository.updateCrossRef(
                            crossRef.copy(isSynced = true)
                        )
                        Log.d("CrossDL", "Updated crossRef: ${crossRef.propertyId}-${crossRef.poiId}")
                        results.add(SyncStatus.Success("Updated ${crossRef.propertyId}-${crossRef.poiId}"))

                    } else {
                        // ✅ Already synced ➜ nothing to do
                        Log.d("CrossDL", "Already up-to-date: ${crossRef.propertyId}-${crossRef.poiId}")
                        results.add(SyncStatus.Success("Already up-to-date: ${crossRef.propertyId}-${crossRef.poiId}"))
                    }

                } catch (e: Exception) {
                    // ❌ Error during sync of specific crossRef
                    results.add(SyncStatus.Failure("CrossRef ${crossRef.propertyId}-${crossRef.poiId}", e))
                }
            }

        } catch (e: Exception) {
            // ❌ General error fetching from Firestore
            results.add(SyncStatus.Failure("CrossRefDownload (fetch failed)", e))
        }

        return results  // Return sync status list
    }
}
