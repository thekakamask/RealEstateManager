package com.dcac.realestatemanager.data.firebaseDatabase.poi

import android.util.Log
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.PHOTOS
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.POIS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePoiOnlineRepository(
    private val firestore: FirebaseFirestore
) : PoiOnlineRepository {

    override suspend fun uploadPoi(poi: PoiOnlineEntity, firebasePoiId: String): PoiOnlineEntity {
        try {
            val map = mapOf(
                "ownerUid" to poi.ownerUid,
                "universalLocalId" to poi.universalLocalId,
                "name" to poi.name,
                "type" to poi.type,
                "address" to poi.address,
                "latitude" to poi.latitude,
                "longitude" to poi.longitude,
                "updatedAt" to poi.updatedAt
            )

            Log.e("DEBUG_FIRESTORE_DATA", map.toString())

            firestore.collection(FirestoreCollections.POIS)
                .document(firebasePoiId)
                .set(map)
                .await()

            return poi
        } catch (e: Exception) {
            throw FirebasePoiUploadException("Failed to upload POI: ${e.message}", e)
        }
    }

    override suspend fun getPoi(firebasePoiId: String): PoiOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.POIS)
                .document(firebasePoiId)
                .get()
                .await()

            snapshot.toObject(PoiOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebasePoiDownloadException("Failed to get POI: ${e.message}", e)
        }
    }

    override suspend fun getAllPoiS(): List<FirestorePoiDocument> {
        return try {
            firestore.collection(FirestoreCollections.POIS)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(PoiOnlineEntity::class.java)?.let { entity ->
                        FirestorePoiDocument(
                            firebaseId = doc.id,
                            poi = entity
                        )
                    }
                }
        } catch (e: Exception) {
            throw FirebasePoiDownloadException("Failed to fetch POIs: ${e.message}", e)
        }
    }

    override suspend fun deletePoi(firebasePoiId: String) {
        try {
            firestore.collection(FirestoreCollections.POIS)
                .document(firebasePoiId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePoiDeleteException("Failed to delete POI: ${e.message}", e)
        }
    }

    override suspend fun markPoiAsDeleted(firebasePoiId: String, updatedAt: Long) {
        firestore.collection(POIS)
            .document(firebasePoiId)
            .update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to updatedAt
                )
            )
            .await()
    }
}

class FirebasePoiUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePoiDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePoiDeleteException(message: String, cause: Throwable?) : Exception(message, cause)

data class FirestorePoiDocument(
    val firebaseId: String,                      // => Firebase UID (document ID)
    val poi: PoiOnlineEntity        // => Poi data
)
