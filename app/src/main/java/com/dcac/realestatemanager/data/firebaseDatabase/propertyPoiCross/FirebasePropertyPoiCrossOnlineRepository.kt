package com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.PROPERTY_POI_CROSS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePropertyPoiCrossOnlineRepository(
    private val firestore: FirebaseFirestore
) : PropertyPoiCrossOnlineRepository {

    override suspend fun uploadCrossRef(crossRef: PropertyPoiCrossOnlineEntity): PropertyPoiCrossOnlineEntity {
        val documentId = "${crossRef.universalLocalPropertyId}-${crossRef.universalLocalPoiId}"

        try {
            firestore.collection(PROPERTY_POI_CROSS)
                .document(documentId)
                .set(crossRef)
                .await()
            return crossRef
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossUploadException("Failed to upload crossRef: ${e.message}", e)
        }
    }

    override suspend fun getCrossRefsByPropertyId(firebasePropertyId: String): List<PropertyPoiCrossOnlineEntity> {
        return try {
            firestore.collection(PROPERTY_POI_CROSS)
                .whereEqualTo("propertyId", firebasePropertyId)
                .get()
                .await()
                .documents.mapNotNull {
                    it.toObject(PropertyPoiCrossOnlineEntity::class.java)
                }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch crossRefs: ${e.message}", e)
        }
    }

    override suspend fun getCrossRefsByPoiId(firebasePoiId: String): List<PropertyPoiCrossOnlineEntity> {
        return try {
            firestore.collection(PROPERTY_POI_CROSS)
                .whereEqualTo("poiId", firebasePoiId)
                .get()
                .await()
                .documents.mapNotNull {
                    it.toObject(PropertyPoiCrossOnlineEntity::class.java)
                }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch crossRefs by poiId: ${e.message}", e)
        }
    }

    override suspend fun getAllCrossRefs(): List<FirestoreCrossDocument> {
        return try {
            firestore.collection(PROPERTY_POI_CROSS)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(PropertyPoiCrossOnlineEntity::class.java)?.let { entity ->
                        FirestoreCrossDocument(
                            firebaseId = doc.id,
                            cross = entity
                        )
                    }
                }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch cross-references: ${e.message}", e)
        }
    }

    /*override suspend fun deleteCrossRef(firebasePropertyId: String, firebasePoiId: String) {
        val docId = "$firebasePropertyId-$firebasePoiId"
        try {
            firestore.collection(PROPERTY_POI_CROSS)
                .document(docId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDeleteException("Failed to delete crossRef: ${e.message}", e)
        }
    }

    override suspend fun deleteAllCrossRefsForProperty(firebasePropertyId: String) {
        try {
            val snapshots = firestore.collection(PROPERTY_POI_CROSS)
                .whereEqualTo("propertyId", firebasePropertyId)
                .get()
                .await()

            snapshots.documents.forEach {
                it.reference.delete().await()
            }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDeleteException("Failed to delete all crossRefs for property: ${e.message}", e)
        }
    }

    override suspend fun deleteAllCrossRefsForPoi(firebasePoiId: String) {
        try {
            val snapshots = firestore.collection(PROPERTY_POI_CROSS)
                .whereEqualTo("poiId", firebasePoiId)
                .get()
                .await()

            snapshots.documents.forEach {
                it.reference.delete().await()
            }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDeleteException("Failed to delete all crossRefs for POI: ${e.message}", e)
        }
    }*/

    override suspend fun markCrossRefAsDeleted(
        firebasePoiId: String,
        firebasePropertyId: String,
        updatedAt: Long
    ) {

        val docId = "$firebasePropertyId-$firebasePoiId"

        firestore.collection(PROPERTY_POI_CROSS)
            .document(docId)
            .update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to updatedAt
                )
            )
            .await()
    }
}

class FirebasePropertyPoiCrossUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePropertyPoiCrossDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
//class FirebasePropertyPoiCrossDeleteException(message: String, cause: Throwable?) : Exception(message, cause)

data class FirestoreCrossDocument(
    val firebaseId: String,                      // => Firebase UID (document ID)
    val cross: PropertyPoiCrossOnlineEntity           // => Cross data
)
