package com.dcac.realestatemanager.data.sync.propertyPoiCross

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

class PropertyPoiCrossUploadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,             // Local Room repo
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository  // Firestore repo
): PropertyPoiCrossUploadInterfaceManager {

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to sync data")

    override suspend fun syncUnSyncedPropertyPoiCross(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val crossRefsToSync = propertyPoiCrossRepository.uploadUnSyncedCrossRefsToFirebase().first()

        for (crossRef in crossRefsToSync) {
            val propertyId = crossRef.universalLocalPropertyId
            val poiId = crossRef.universalLocalPoiId
            val firestoreId = "$propertyId-$poiId"

            try {
                if (crossRef.isDeleted) {
                    propertyPoiCrossOnlineRepository.deleteCrossRef(propertyId, poiId)
                    propertyPoiCrossRepository.deleteCrossRef(crossRef)

                    results.add(SyncStatus.Success("CrossRef $firestoreId deleted from Firebase & Room"))

                } else {

                    Log.e("UploadCheck", "Uploading crossRef with UID: $currentUserUid and data: ${crossRef.toOnlineEntity(currentUserUid)}")

                    val uploadedCrossRef = propertyPoiCrossOnlineRepository.uploadCrossRef(
                        crossRef = crossRef.toOnlineEntity(currentUserUid)
                    )

                    propertyPoiCrossRepository.updateCrossRefFromFirebase(
                        crossRef = uploadedCrossRef,
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
