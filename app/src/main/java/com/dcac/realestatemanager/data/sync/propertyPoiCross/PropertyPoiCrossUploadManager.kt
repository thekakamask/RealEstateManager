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

        Log.e("SYNC_CROSSREF", "CROSSREF TO SYNC COUNT = ${crossRefsToSync.size}")
        crossRefsToSync.forEach {
            Log.e("SYNC_CROSSREF", "CROSSREF ENTITY = $it")
        }
        for (crossRef in crossRefsToSync) {
            val propertyId = crossRef.universalLocalPropertyId
            val poiId = crossRef.universalLocalPoiId
            val firestoreId = "$propertyId-$poiId"

            try {
                if (crossRef.isDeleted) {
                    propertyPoiCrossOnlineRepository.markCrossRefAsDeleted(
                        firebasePropertyId = propertyId,
                        firebasePoiId = poiId,
                        updatedAt = crossRef.updatedAt
                    )

                    propertyPoiCrossRepository.deleteCrossRef(crossRef)

                    results.add(
                        SyncStatus.Success("CrossRef $firestoreId marked deleted online & removed locally")
                    )
                }
                else {

                    Log.e(
                        "SYNC_CROSSREF_UPLOAD",
                        "Uploading crossRef propertyId=$propertyId poiId=$poiId firestoreId=$firestoreId"
                    )
                    val uploadedCrossRef = propertyPoiCrossOnlineRepository.uploadCrossRef(
                        crossRef = crossRef.toOnlineEntity(currentUserUid)
                    )

                    propertyPoiCrossRepository.updateCrossRefFromFirebase(
                        crossRef = uploadedCrossRef,
                        firebaseDocumentId = firestoreId
                    )

                    Log.e("SYNC_CROSSREF_UPLOAD", "Upload OK for crossRef $firestoreId")
                    results.add(SyncStatus.Success("CrossRef $firestoreId uploaded to Firebase"))
                }

            } catch (e: Exception) {
                Log.e("SYNC_CROSSREF_ERROR", "Upload failed for crossRef $firestoreId : ${e.message}", e)
                results.add(SyncStatus.Failure("CrossRef $firestoreId failed to sync", e))
            }
        }

        return results
    }
}
