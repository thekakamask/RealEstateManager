package com.dcac.realestatemanager.data.sync.poi

import android.util.Log
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first

class PoiUploadManager(
    private val poiRepository: PoiRepository,
    private val poiOnlineRepository: PoiOnlineRepository
): PoiUploadInterfaceManager {

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to sync data")

    override suspend fun syncUnSyncedPoiS(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val poiToSync = poiRepository.uploadUnSyncedPoiSToFirebase().first()

        Log.e("SYNC_POI", "POI TO SYNC COUNT = ${poiToSync.size}")
        poiToSync.forEach {
            Log.e("SYNC_POI", "POI ENTITY = $it")
        }

        for (poiEntity in poiToSync) {
            val firebaseId = poiEntity.firestoreDocumentId
            val localId = poiEntity.id

            try {
                if (poiEntity.isDeleted) {
                    if (firebaseId != null) {
                        poiOnlineRepository.markPoiAsDeleted(
                            firebasePoiId = firebaseId,
                            updatedAt = poiEntity.updatedAt
                        )
                    }

                    poiRepository.deletePoi(poiEntity)

                    results.add(
                        SyncStatus.Success("Poi $localId marked deleted online & removed locally")
                    )
                }
                else {
                    val finalId = firebaseId ?: generateFirestoreId()

                    Log.e(
                        "SYNC_POI_UPLOAD",
                        "Uploading POI localId=${poiEntity.id} firestoreId=$firebaseId finalId=$finalId"
                    )

                    Log.e("DEBUG_AUTH", "AUTH UID = '${FirebaseAuth.getInstance().currentUser?.uid}'")
                    Log.e("DEBUG_OWNER", "OWNER UID = '$currentUserUid'")
                    Log.e("DEBUG_POI_UPLOAD", "POI ONLINE = ${poiEntity.toOnlineEntity(currentUserUid)}")
                    val uploadedPoi = poiOnlineRepository.uploadPoi(
                        poi = poiEntity.toOnlineEntity(currentUserUid),
                        firebasePoiId = finalId
                    )

                    poiRepository.updatePoiFromFirebase(
                        poi = uploadedPoi,
                        firebaseDocumentId = finalId
                    )

                    Log.e("SYNC_POI_UPLOAD", "Upload OK for POI $localId")

                    results.add(SyncStatus.Success("Poi $localId uploaded to Firebase"))
                }

            } catch (e: Exception) {
                Log.e("SYNC_POI_ERROR", "Upload failed for POI $localId : ${e.message}", e)
                results.add(SyncStatus.Failure("Poi $localId", e))
            }
        }

        return results
    }

    private fun generateFirestoreId(): String {
        return FirebaseFirestore.getInstance()
                .collection(FirestoreCollections.POIS)
            .document()
            .id
    }
}
