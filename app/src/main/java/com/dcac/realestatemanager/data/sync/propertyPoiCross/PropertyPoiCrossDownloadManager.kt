package com.dcac.realestatemanager.data.sync.propertyPoiCross

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import kotlinx.coroutines.flow.first

class PropertyPoiCrossDownloadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,             // Local Room repo
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository  // Firestore repo
): PropertyPoiCrossDownloadInterfaceManager {

    override suspend fun downloadUnSyncedPropertyPoiCross(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val crossRefsToSync = propertyPoiCrossRepository.uploadUnSyncedCrossRefsToFirebase().first()

        for (crossRef in crossRefsToSync) {
            val propertyId = crossRef.universalLocalPropertyId
            val poiId = crossRef.universalLocalPoiId
            val firestoreId = "$propertyId-$poiId"

            try {
                if (crossRef.isDeleted) {
                    // Delete from Firebase if present
                    propertyPoiCrossOnlineRepository.deleteCrossRef(propertyId, poiId)

                    // Hard delete locally
                    propertyPoiCrossRepository.deleteCrossRef(crossRef)

                    results.add(SyncStatus.Success("CrossRef $firestoreId deleted from Firebase & Room"))
                } else {
                    val uploaded = propertyPoiCrossOnlineRepository.uploadCrossRef(
                        crossRef = crossRef.toOnlineEntity()
                    )

                    propertyPoiCrossRepository.updateCrossRefFromFirebase(
                        crossRef = uploaded,
                        firebaseDocumentId = firestoreId
                    )

                    results.add(SyncStatus.Success("CrossRef $firestoreId uploaded to Firebase"))
                }

            } catch (e: Exception) {
                results.add(SyncStatus.Failure("CrossRef $firestoreId failed to sync", e))
            }
        }

        return results
    }
}