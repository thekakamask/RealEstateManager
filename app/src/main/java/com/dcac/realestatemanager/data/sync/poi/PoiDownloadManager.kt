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
                val poiOnline = doc.poi
                val localId = poiOnline.universalLocalId
                val localPoiS =
                    poiRepository.getAllPoiSByIdIncludeDeleted(localId).first()

                if (poiOnline.isDeleted) {
                    if (localPoiS != null) {
                        poiRepository.deletePoi(localPoiS)
                        results.add(
                            SyncStatus.Success("Poi $localId deleted locally (remote deleted)")
                        )
                    }
                    continue
                }

                val shouldDownload =
                    localPoiS == null || poiOnline.updatedAt > localPoiS.updatedAt

                if (!shouldDownload) {
                    results.add(SyncStatus.Success("Poi $localId already up-to-date"))
                    continue
                }

                if (localPoiS == null) {
                    poiRepository.insertPoiInsertFromFirebase(
                        poi = poiOnline,
                        firebaseDocumentId = doc.firebaseId
                    )
                    results.add(SyncStatus.Success("Poi $localId inserted"))
                } else {
                    poiRepository.updatePoiFromFirebase(
                        poi = poiOnline,
                        firebaseDocumentId = doc.firebaseId
                    )
                    results.add(SyncStatus.Success("Poi $localId updated"))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global POI download failed", e))
        }

        return results
    }
}


