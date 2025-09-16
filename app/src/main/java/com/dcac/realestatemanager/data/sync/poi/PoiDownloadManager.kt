package com.dcac.realestatemanager.data.sync.poi

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PoiDownloadManager(
    private val poiRepository: PoiRepository,                // Local (Room) POI repository
    private val poiOnlineRepository: PoiOnlineRepository     // Remote (Firestore) POI repository
) {

    // Downloads all POIs from Firestore and updates Room if needed
    suspend fun downloadUnSyncedPoiS(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>() // Collects the sync results

        try {
            val onlinePoiS = poiOnlineRepository.getAllPoiS() // Step 1: Get all POIs from Firebase

            for (onlinePoi in onlinePoiS) {
                try {
                    val roomId = onlinePoi.roomId // Room ID used to find matching local POI

                    // Step 2: Get local POI from Room (null if not found)
                    val localPoi = poiRepository.getPoiEntityById(roomId).first()

                    // Step 3: Sync if local data doesn't exist or is outdated
                    val shouldDownload = localPoi == null || onlinePoi.updatedAt > localPoi.updatedAt

                    if (shouldDownload) {
                        poiRepository.downloadPoiFromFirebase(onlinePoi) // Save POI to Room
                        results.add(SyncStatus.Success("Poi $roomId downloaded"))
                    } else {
                        results.add(SyncStatus.Success("Poi $roomId already up-to-date"))
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("Poi ${onlinePoi.roomId} failed to sync", e))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global POI download failed", e))
        }

        return results
    }
}


