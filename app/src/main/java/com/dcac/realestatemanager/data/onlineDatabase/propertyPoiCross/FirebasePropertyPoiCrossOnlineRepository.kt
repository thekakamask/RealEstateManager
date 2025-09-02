package com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross

import com.dcac.realestatemanager.data.onlineDatabase.FirestoreCollections
import com.dcac.realestatemanager.model.PropertyPoiCross
import com.dcac.realestatemanager.utils.toModel
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePropertyPoiCrossOnlineRepository(
    private val firestore: FirebaseFirestore
) : PropertyPoiCrossOnlineRepository {

    override suspend fun uploadCrossRef(crossRef: PropertyPoiCross): PropertyPoiCross {
        val entity = crossRef.toOnlineEntity()
        val documentId = "${crossRef.propertyId}-${crossRef.poiId}" // Unique ID

        try {
            firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .document(documentId)
                .set(entity)
                .await()
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossUploadException("Failed to upload crossRef: ${e.message}", e)
        }

        return crossRef.copy(isSynced = true)
    }

    override suspend fun getCrossRefsByPropertyId(propertyId: Long): List<PropertyPoiCross> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                doc.toObject(PropertyPoiCrossOnlineEntity::class.java)?.toModel()
            }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch crossRefs: ${e.message}", e)
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

            snapshots.documents.forEach { doc ->
                doc.reference.delete().await()
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

            snapshots.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDeleteException("Failed to delete all crossRefs for POI: ${e.message}", e)
        }
    }

    override suspend fun getAllCrossRefs(): List<PropertyPoiCross> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                val entity = doc.toObject(PropertyPoiCrossOnlineEntity::class.java)
                entity?.let {
                    PropertyPoiCross(
                        propertyId = it.propertyId,
                        poiId = it.poiId
                    )
                }
            }
        } catch (e: Exception) {
            throw FirebasePropertyPoiCrossDownloadException("Failed to fetch cross-references: ${e.message}", e)
        }
    }


}

class FirebasePropertyPoiCrossUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePropertyPoiCrossDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePropertyPoiCrossDeleteException(message: String, cause: Throwable?) : Exception(message, cause)
