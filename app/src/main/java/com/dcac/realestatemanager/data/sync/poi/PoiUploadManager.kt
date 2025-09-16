package com.dcac.realestatemanager.data.sync.poi

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import kotlinx.coroutines.flow.first

class PoiUploadManager(
    private val poiRepository: PoiRepository,                 // Local (Room) POI repository
    private val poiOnlineRepository: PoiOnlineRepository      // Remote (Firestore) POI repository
) {

    // Uploads all unsynced POIs to Firestore
    suspend fun syncUnSyncedPoiS(): List<SyncStatus> {

        val results = mutableListOf<SyncStatus>()

        try {
            val unSyncedPoiS = poiRepository.uploadUnSyncedPoiSToFirebase().first()

            for (poiEntity in unSyncedPoiS) {
                val poiId = poiEntity.id

                if (poiEntity.isDeleted) {
                    poiOnlineRepository.deletePoi(poiId.toString())
                    poiRepository.deletePoi(poiEntity)
                    results.add(SyncStatus.Success("Poi $poiId deleted"))
                } else {
                    val updatedPoi = poiEntity.copy(updatedAt = System.currentTimeMillis())
                    val uploadedPoi = poiOnlineRepository.uploadPoi(
                        poi = updatedPoi.toOnlineEntity(),
                        poiId = poiId.toString()
                    )
                    poiRepository.downloadPoiFromFirebase(
                        uploadedPoi
                    )

                    results.add(SyncStatus.Success("Poi $poiId uploaded"))

                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global upload sync failed", e))
        }
        return results
    }
}
