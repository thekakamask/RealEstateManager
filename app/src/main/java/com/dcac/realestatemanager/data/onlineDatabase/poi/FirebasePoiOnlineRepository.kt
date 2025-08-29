package com.dcac.realestatemanager.data.onlineDatabase.poi

import com.dcac.realestatemanager.data.onlineDatabase.FirestoreCollections
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.utils.toModel
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePoiOnlineRepository(
    private val firestore: FirebaseFirestore
) : PoiOnlineRepository {

    override suspend fun uploadPoi(poi: Poi, poiId: String): Poi {
        val entity = poi.toOnlineEntity()
        try {
            firestore.collection(FirestoreCollections.POIS)
                .document(poiId)
                .set(entity)
                .await()
        } catch (e: Exception) {
            throw FirebasePoiUploadException("Failed to upload POI: ${e.message}", e)
        }
        return poi.copy(isSynced = true)
    }

    override suspend fun getPoi(poiId: String): Poi? {
        val snapshot = firestore.collection(FirestoreCollections.POIS)
            .document(poiId)
            .get()
            .await()

        val entity = snapshot.toObject(PoiOnlineEntity::class.java)
        return entity?.toModel(poiId = poiId.toLong())
    }

    override suspend fun getAllPoiS(): List<Poi> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.POIS)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                doc.toObject(PoiOnlineEntity::class.java)
                    ?.toModel(poiId = doc.id.toLongOrNull() ?: return@mapNotNull null)
            }

        } catch (e: Exception) {
            throw FirebasePoiDownloadException("Failed to get POIs: ${e.message}", e)
        }
    }

    override suspend fun deletePoi(poiId: String) {
        try {
            firestore.collection(FirestoreCollections.POIS)
                .document(poiId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePoiDeleteException("Failed to delete POI: ${e.message}", e)
        }
    }

}

class FirebasePoiUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePoiDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePoiDeleteException(message: String, cause: Throwable?) : Exception(message, cause)
