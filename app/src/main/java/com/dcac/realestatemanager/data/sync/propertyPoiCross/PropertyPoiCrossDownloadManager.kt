package com.dcac.realestatemanager.data.sync.propertyPoiCross

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyPoiCrossDownloadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository
) {

    suspend fun downloadUnSyncedPropertyPoiCross(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineCrossRefs = propertyPoiCrossOnlineRepository.getAllCrossRefs()

            for (crossRef in onlineCrossRefs) {
                try {
                    val localCross = propertyPoiCrossRepository
                        .getCrossByIds(crossRef.propertyId, crossRef.poiId)
                        .first()

                    if (localCross == null) {
                        // 🔽 New crossRef from Firebase
                        propertyPoiCrossRepository.cacheCrossRefFromFirebase(
                            crossRef.copy(isSynced = true)
                        )
                        Log.d("CrossDL", "Inserted crossRef: ${crossRef.propertyId}-${crossRef.poiId}")
                        results.add(SyncStatus.Success("Inserted ${crossRef.propertyId}-${crossRef.poiId}"))
                    } else {
                        val isSame = localCross.isSynced // only field to compare

                        if (!isSame) {
                            // 🔁 Update local if not synced
                            propertyPoiCrossRepository.updateCrossRef(
                                crossRef.copy(isSynced = true)
                            )
                            Log.d("CrossDL", "Updated crossRef: ${crossRef.propertyId}-${crossRef.poiId}")
                            results.add(SyncStatus.Success("Updated ${crossRef.propertyId}-${crossRef.poiId}"))
                        } else {
                            // ✅ Already up-to-date
                            Log.d("CrossDL", "Already up-to-date: ${crossRef.propertyId}-${crossRef.poiId}")
                            results.add(SyncStatus.Success("Already up-to-date: ${crossRef.propertyId}-${crossRef.poiId}"))
                        }
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("CrossRef ${crossRef.propertyId}-${crossRef.poiId}", e))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("CrossRefDownload (fetch failed)", e))
        }

        return results
    }
}
