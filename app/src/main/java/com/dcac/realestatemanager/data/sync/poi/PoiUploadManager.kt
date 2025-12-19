package com.dcac.realestatemanager.data.sync.poi

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

        for (poiEntity in poiToSync) {
            val firebaseId = poiEntity.firestoreDocumentId
            val localId = poiEntity.id

            try {
                if (poiEntity.isDeleted) {
                    if (firebaseId != null) {
                        poiOnlineRepository.deletePoi(firebaseId)
                    }
                    poiRepository.deletePoi(poiEntity)
                    results.add(SyncStatus.Success("Poi $localId deleted from Firebase & Room"))
                } else {
                    val finalId = firebaseId ?: generateFirestoreId()
                    val uploadedPoi = poiOnlineRepository.uploadPoi(
                        poi = poiEntity.toOnlineEntity(currentUserUid),
                        firebasePoiId = finalId
                    )

                    poiRepository.updatePoiFromFirebase(
                        poi = uploadedPoi,
                        firebaseDocumentId = finalId
                    )

                    results.add(SyncStatus.Success("Poi $localId uploaded to Firebase"))
                }

            } catch (e: Exception) {
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
