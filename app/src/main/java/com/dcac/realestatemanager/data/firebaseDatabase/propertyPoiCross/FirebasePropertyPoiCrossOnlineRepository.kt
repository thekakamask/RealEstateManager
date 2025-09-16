package com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePropertyPoiCrossOnlineRepository(
    private val firestore: FirebaseFirestore
) : PropertyPoiCrossOnlineRepository {

    override suspend fun uploadCrossRef(crossRef: PropertyPoiCrossOnlineEntity): PropertyPoiCrossOnlineEntity {
        val documentId = "${crossRef.propertyId}-${crossRef.poiId}"

        try {
            firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .document(documentId)
                .set(crossRef)
                .await()
            return crossRef
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossUploadException("Failed to upload crossRef: ${e.message}", e)
        }
    }

    override suspend fun getCrossRefsByPropertyId(propertyId: Long): List<PropertyPoiCrossOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()
                .documents.mapNotNull {
                    it.toObject(PropertyPoiCrossOnlineEntity::class.java)
                }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch crossRefs: ${e.message}", e)
        }
    }

    override suspend fun getCrossRefsByPoiId(poiId: Long): List<PropertyPoiCrossOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .whereEqualTo("poiId", poiId)
                .get()
                .await()
                .documents.mapNotNull {
                    it.toObject(PropertyPoiCrossOnlineEntity::class.java)
                }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch crossRefs by poiId: ${e.message}", e)
        }
    }

    override suspend fun getAllCrossRefs(): List<PropertyPoiCrossOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .get()
                .await()
                .documents.mapNotNull {
                    it.toObject(PropertyPoiCrossOnlineEntity::class.java)
                }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch cross-references: ${e.message}", e)
        }
    }

    override suspend fun deleteCrossRef(propertyId: Long, poiId: Long) {
        val docId = "$propertyId-$poiId"
        try {
            firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .document(docId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDeleteException("Failed to delete crossRef: ${e.message}", e)
        }
    }

    override suspend fun deleteAllCrossRefsForProperty(propertyId: Long) {
        try {
            val snapshots = firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()

            snapshots.documents.forEach {
                it.reference.delete().await()
            }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDeleteException("Failed to delete all crossRefs for property: ${e.message}", e)
        }
    }

    override suspend fun deleteAllCrossRefsForPoi(poiId: Long) {
        try {
            val snapshots = firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .whereEqualTo("poiId", poiId)
                .get()
                .await()

            snapshots.documents.forEach {
                it.reference.delete().await()
            }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDeleteException("Failed to delete all crossRefs for POI: ${e.message}", e)
        }
    }
}

class FirebasePropertyPoiCrossUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePropertyPoiCrossDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePropertyPoiCrossDeleteException(message: String, cause: Throwable?) : Exception(message, cause)
