package com.dcac.realestatemanager.data.sync.propertyPoiCross

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import kotlinx.coroutines.flow.first

class PropertyPoiCrossUploadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,             // Local Room repo
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository  // Firestore repo
): PropertyPoiCrossUploadInterfaceManager {

    // Uploads all unsynced cross-references from Room to Firestore
    override suspend fun syncUnSyncedPropertyPoiCross(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val unSyncedCrossRefs = propertyPoiCrossRepository.uploadUnSyncedPropertiesPoiSCross().first()

            for (crossRefEntity in unSyncedCrossRefs) {
                val propertyId = crossRefEntity.propertyId
                val poiId = crossRefEntity.poiId

                if (crossRefEntity.isDeleted) {
                    // 🗑 Delete from Firebase then Room
                    propertyPoiCrossOnlineRepository.deleteCrossRef(propertyId, poiId)
                    propertyPoiCrossRepository.deleteCrossRef(crossRefEntity)
                    results.add(SyncStatus.Success("CrossRef ($propertyId, $poiId) deleted"))
                } else {
                    // 🔄 Update timestamp and upload to Firebase
                    val updatedCrossRef = crossRefEntity.copy(updatedAt = System.currentTimeMillis())
                    propertyPoiCrossOnlineRepository.uploadCrossRef(
                        crossRef = updatedCrossRef.toOnlineEntity() // ✅ Just pass the online entity
                    )

                    // ✅ Optional : could re-save to Room to mark isSynced = true
                    propertyPoiCrossRepository.downloadCrossRefFromFirebase(
                        crossRef = updatedCrossRef.toOnlineEntity()
                    )

                    results.add(SyncStatus.Success("CrossRef ($propertyId, $poiId) uploaded"))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global CrossRef upload failed", e))
        }

        return results
    }
}
