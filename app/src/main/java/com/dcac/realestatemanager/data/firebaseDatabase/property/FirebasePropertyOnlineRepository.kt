package com.dcac.realestatemanager.data.firebaseDatabase.property

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.poi.FirebasePoiDownloadException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePropertyOnlineRepository(
    private val firestore: FirebaseFirestore
) : PropertyOnlineRepository {

    override suspend fun uploadProperty(property: PropertyOnlineEntity, firebasePropertyId: String): PropertyOnlineEntity {
        try {
            firestore.collection(FirestoreCollections.PROPERTIES)
                .document(firebasePropertyId)
                .set(property)
                .await()
            return property
        } catch (e: Exception) {
            throw FirebasePropertyUploadException("Failed to upload property: ${e.message}", e)
        }
    }

    override suspend fun getProperty(firebasePropertyId: String): PropertyOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.PROPERTIES)
                .document(firebasePropertyId)
                .get()
                .await()

            snapshot.toObject(PropertyOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebasePropertyDownloadException("Failed to get property: ${e.message}", e)
        }
    }

    override suspend fun getAllProperties(): List<FirestorePropertyDocument> {
        return try {
            firestore.collection(FirestoreCollections.PROPERTIES)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(PropertyOnlineEntity::class.java)?.let { entity ->
                        FirestorePropertyDocument(
                            firebaseId = doc.id,
                            property = entity
                        )
                    }
                }
        } catch (e: Exception) {
            throw FirebasePoiDownloadException("Failed to fetch POIs: ${e.message}", e)
        }
    }

    override suspend fun deleteProperty(firebasePropertyId: String) {
        try {
            firestore.collection(FirestoreCollections.PROPERTIES)
                .document(firebasePropertyId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePropertyDeleteException("Failed to delete property: ${e.message}", e)
        }
    }

    override suspend fun deleteAllPropertiesForUser(firebaseUserId: Long) {
        try {
            val snapshots = firestore.collection(FirestoreCollections.PROPERTIES)
                .whereEqualTo("userId", firebaseUserId)
                .get()
                .await()

            snapshots.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            throw FirebasePropertyDeleteException("Failed to delete properties for user: ${e.message}", e)
        }
    }
}

class FirebasePropertyUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePropertyDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePropertyDeleteException(message: String, cause: Throwable?) : Exception(message, cause)

data class FirestorePropertyDocument(
    val firebaseId: String,                      // => Firebase UID (document ID)
    val property: PropertyOnlineEntity           // => property data
)