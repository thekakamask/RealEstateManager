package com.dcac.realestatemanager.data.sync.poi

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.onlineDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PoiDownloadManager(
    private val poiRepository: PoiRepository,                  // Local (Room) POI repository
    private val poiOnlineRepository: PoiOnlineRepository       // Remote (Firestore) POI repository
) {

    // Downloads all POIs from Firestore and updates Room if needed
    suspend fun downloadUnSyncedPoiS(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()              // To collect success/failure results

        try {
            // Get all POIs from Firestore
            val onlinePoiS = poiOnlineRepository.getAllPoiS()

            for (poi in onlinePoiS) {
                try {
                    // Fetch corresponding local POI if it exists
                    val localPoi = poiRepository.getPoiById(poi.id).first()

                    if (localPoi == null) {
                        // If POI doesn't exist locally, insert it
                        poiRepository.cachePoiFromFirebase(poi.copy(isSynced = true))
                        Log.d("PoiDownloadManager", "Inserted poi: ${poi.name}")
                        results.add(SyncStatus.Success("Poi ${poi.name} inserted"))
                    } else if (poi.updatedAt > localPoi.updatedAt) {
                        // If Firestore POI is newer, update the local one
                        poiRepository.updatePoi(poi.copy(isSynced = true))
                        Log.d("PoiDownloadManager", "Updated poi: ${poi.name}")
                        results.add(SyncStatus.Success("Poi ${poi.name} updated"))
                    } else {
                        // Local POI is up-to-date
                        Log.d("PoiDownloadManager", "Poi already up-to-date: ${poi.name}")
                        results.add(SyncStatus.Success("Poi ${poi.name} already up-to-date"))
                    }

                } catch (e: Exception) {
                    // Handle error for individual POI
                    results.add(SyncStatus.Failure("Poi ${poi.name}", e))
                }
            }

        } catch (e: Exception) {
            // Handle general fetch failure
            results.add(SyncStatus.Failure("PoiDownload (fetch failed)", e))
        }

        return results  // Return the list of sync results
    }
}
