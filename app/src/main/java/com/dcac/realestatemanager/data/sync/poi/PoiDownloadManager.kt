package com.dcac.realestatemanager.data.sync.poi

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PoiDownloadManager(
    private val poiRepository: PoiRepository,                // Local (Room) POI repository
    private val poiOnlineRepository: PoiOnlineRepository     // Remote (Firestore) POI repository
): PoiDownloadInterfaceManager {

    override suspend fun downloadUnSyncedPoiS(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlinePoiS = poiOnlineRepository.getAllPoiS()

            for (doc in onlinePoiS) {
                try {
                    val poiOnline = doc.poi
                    val localId = poiOnline.universalLocalId
                    val localPoi = poiRepository.getPoiById(localId).first()

                    val shouldDownload = localPoi == null || poiOnline.updatedAt > localPoi.updatedAt

                    if (shouldDownload) {
                        poiRepository.insertPoiInsertFromFirebase(poiOnline, doc.firebaseId)
                        results.add(SyncStatus.Success("Poi $localId downloaded"))
                    } else {
                        results.add(SyncStatus.Success("Poi $localId already up-to-date"))
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("Poi ${doc.firebaseId} failed to sync", e))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global POI download failed", e))
        }

        return results
    }
}


