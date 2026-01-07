package com.dcac.realestatemanager.data.sync.staticMap

import android.util.Log
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
        val staticMapsToSync = staticMapRepository.uploadUnSyncedStaticMapToFirebase().first()

        Log.e("SYNC_STATIC_MAP", "STATIC MAP TO SYNC COUNT = ${staticMapsToSync.size}")
        staticMapsToSync.forEach {
            Log.e("SYNC_STATIC_MAP", "STATIC MAP ENTITY = $it")
        }

        for (staticMap in staticMapsToSync) {
            try {
                val firebaseId = staticMap.firestoreDocumentId

                if(staticMap.isDeleted) {
                    if (firebaseId != null) {
                        staticMapOnlineRepository.deleteStaticMap(firebaseId)
                    }

                    staticMapRepository.deleteStaticMap(staticMap)
                    results.add(SyncStatus.Success("StaticMap ${staticMap.id} deleted from Firebase & Room"))
                } else {
                    val finalId = firebaseId ?: generateFirestoreId()

                    Log.e(
                        "SYNC_STATIC_MAP_UPLOAD",
                        "Uploading staticMap localId=${staticMap.id} firestoreId=$firebaseId finalId=$finalId"
                    )

                    val online = staticMap.toOnlineEntity(currentUserUid)

                    Log.d(
                        "STATIC_MAP_UPLOAD",
                        "Room uri=${staticMap.uri} -> Online storageUrl(before upload)=${online.storageUrl}"
                    )

                    val updatedOnline = staticMapOnlineRepository.uploadStaticMap(online, finalId)


                    Log.d(
                        "STATIC_MAP_UPLOAD",
                        "Online storageUrl(after upload)=${updatedOnline.storageUrl}"
                    )

                    staticMapRepository.updateStaticMapFromFirebase(
                        staticMap = updatedOnline,
                        firestoreId = finalId
                    )

                    Log.e("SYNC_STATIC_MAP_UPLOAD", "Upload OK for staticMap ${staticMap.id}")
                    results.add(SyncStatus.Success("StaticMap ${staticMap.id} uploaded to Firebase"))
                }
            } catch (e: Exception) {
                Log.e("SYNC_STATIC_MAP_ERROR", "Upload failed for staticMap ${staticMap.id} : ${e.message}", e)
                results.add(SyncStatus.Failure("StaticMap ${staticMap.id}", e))
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