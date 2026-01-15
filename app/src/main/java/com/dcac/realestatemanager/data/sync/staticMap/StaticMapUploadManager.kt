package com.dcac.realestatemanager.data.sync.staticMap

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first

class StaticMapUploadManager(
    private val staticMapRepository: StaticMapRepository,
    private val staticMapOnlineRepository: StaticMapOnlineRepository
): StaticMapUploadInterfaceManager {

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to sync data")

    override suspend fun syncUnSyncedStaticMaps(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val staticMapsToSync =
            staticMapRepository.uploadUnSyncedStaticMapToFirebase().first()

        for (staticMap in staticMapsToSync) {
            try {
                val firebaseId = staticMap.firestoreDocumentId

                if (staticMap.isDeleted) {
                    if (firebaseId != null) {
                        staticMapOnlineRepository.markStaticMapAsDeleted(
                            firebaseStaticMapId = firebaseId,
                            updatedAt = staticMap.updatedAt
                        )
                    }

                    staticMapRepository.deleteStaticMap(staticMap)

                    results.add(
                        SyncStatus.Success(
                            "StaticMap ${staticMap.id} marked deleted online & removed locally"
                        )
                    )
                    continue
                }

                val finalId = firebaseId ?: generateFirestoreId()

                val updatedOnline = staticMapOnlineRepository.uploadStaticMap(
                    staticMap.toOnlineEntity(currentUserUid),
                    finalId
                )

                staticMapRepository.updateStaticMapFromFirebase(
                    staticMap = updatedOnline,
                    firestoreId = finalId
                )

                results.add(
                    SyncStatus.Success("StaticMap ${staticMap.id} uploaded to Firebase")
                )

            } catch (e: Exception) {
                results.add(
                    SyncStatus.Failure("StaticMap ${staticMap.id}", e)
                )
            }
        }

        return results
    }

    private fun generateFirestoreId(): String {
        return FirebaseFirestore.getInstance()
            .collection(FirestoreCollections.STATIC_MAPS)
            .document()
            .id
    }
}