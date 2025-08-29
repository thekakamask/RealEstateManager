package com.dcac.realestatemanager.data.onlineDatabase.property

import android.util.Log
import com.dcac.realestatemanager.data.onlineDatabase.FirestoreCollections
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.toModel
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePropertyOnlineRepository(
    private val firestore: FirebaseFirestore
) : PropertyOnlineRepository {

    override suspend fun uploadProperty(property: Property, propertyId: String): Property {
        val entity = property.toOnlineEntity()
        try {
            firestore.collection(FirestoreCollections.PROPERTIES)
                .document(propertyId)
                .set(entity)
                .await()
        } catch (e: Exception) {
            throw FirebasePropertyUploadException("Failed to upload property: ${e.message}", e)
        }
        return property.copy(isSynced = true)
    }

    override suspend fun getProperty(propertyId: String, user: User): Property? {
        val snapshot = firestore.collection(FirestoreCollections.PROPERTIES)
            .document(propertyId)
            .get()
            .await()

        val entity = snapshot.toObject(PropertyOnlineEntity::class.java)
        return entity?.toModel(propertyId = propertyId.toLong(), user = user)
    }

    override suspend fun getAllProperties(userList: List<User>): List<Property> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.PROPERTIES)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                val entity = doc.toObject(PropertyOnlineEntity::class.java)
                val userId = entity?.userId ?: return@mapNotNull null

                val user = userList.find { it.id == userId }
                if (user == null) {
                    Log.w("FirebaseRepo", "User with ID $userId not found for property ${doc.id}")
                    return@mapNotNull null
                }

                entity.toModel(
                    propertyId = doc.id.toLongOrNull() ?: return@mapNotNull null,
                    user = user
                )
            }
        } catch (e: Exception) {
            throw FirebasePropertyDownloadException("Failed to get properties: ${e.message}", e)
        }
    }

    override suspend fun deleteProperty(propertyId: String) {
        try {
            firestore.collection(FirestoreCollections.PROPERTIES)
                .document(propertyId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePropertyDeleteException("Failed to delete property: ${e.message}", e)
        }
    }
    override suspend fun deleteAllPropertiesForUser(userId: Long) {
        try {
            val snapshots = firestore.collection(FirestoreCollections.PROPERTIES)
                .whereEqualTo("userId", userId)
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
