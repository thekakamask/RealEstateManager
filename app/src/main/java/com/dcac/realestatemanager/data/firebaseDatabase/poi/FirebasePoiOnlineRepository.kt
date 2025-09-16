package com.dcac.realestatemanager.data.firebaseDatabase.poi

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePoiOnlineRepository(
    private val firestore: FirebaseFirestore
) : PoiOnlineRepository {

    override suspend fun uploadPoi(poi: PoiOnlineEntity, poiId: String): PoiOnlineEntity {
        try {
            firestore.collection(FirestoreCollections.POIS)
                .document(poiId)
                .set(poi)
                .await()
            return poi
        } catch (e: Exception) {
            throw FirebasePoiUploadException("Failed to upload POI: ${e.message}", e)
        }
    }

    override suspend fun getPoi(poiId: String): PoiOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.POIS)
                .document(poiId)
                .get()
                .await()

            snapshot.toObject(PoiOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebasePoiDownloadException("Failed to get POI: ${e.message}", e)
        }
    }

    override suspend fun getAllPoiS(): List<PoiOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.POIS)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(PoiOnlineEntity::class.java)
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
