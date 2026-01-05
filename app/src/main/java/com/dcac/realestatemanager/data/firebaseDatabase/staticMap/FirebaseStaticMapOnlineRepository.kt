package com.dcac.realestatemanager.data.firebaseDatabase.staticMap

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class FirebaseStaticMapOnlineRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : StaticMapOnlineRepository {


    override suspend fun uploadStaticMap(
        staticMap: StaticMapOnlineEntity,
        firebaseStaticMapId: String
    ): StaticMapOnlineEntity {
        try {
            if (staticMap.storageUrl.isBlank()) {
                throw FirebaseStaticMapUploadException(
                    "StaticMap URI is empty",
                    IllegalArgumentException("StaticMap URI is empty")
                )
            }

            val uri = staticMap.storageUrl.toUri()
            val storageRef = storage.reference.child("staticMaps/${firebaseStaticMapId}.jpg")

            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val updatedStaticMap = staticMap.copy(storageUrl = downloadUrl)

            firestore.collection(FirestoreCollections.STATIC_MAPS)
                .document(firebaseStaticMapId)
                .set(updatedStaticMap)
                .await()

            return updatedStaticMap
        } catch (e: Exception) {
            // if it is already a FirebaseStaticMapUploadException, we re-throw it as is
            if (e is FirebaseStaticMapUploadException) throw e

            throw FirebaseStaticMapUploadException("Failed to upload static map: ${e.message}", e)
        }
    }

    override suspend fun getStaticMap(firebaseStaticMapId: String): StaticMapOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.STATIC_MAPS)
                .document(firebaseStaticMapId)
                .get()
                .await()

            snapshot.toObject(StaticMapOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebaseStaticMapDownloadException("Failed to get static map: ${e.message}", e)
        }
    }

    override suspend fun getStaticMapByPropertyId(firebasePropertyId: String): StaticMapOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.STATIC_MAPS)
                .whereEqualTo("universalLocalPropertyId", firebasePropertyId) // ⚠️ champ exact à utiliser
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()
                ?.toObject(StaticMapOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebaseStaticMapDownloadException(
                "Failed to get static map by propertyId: ${e.message}", e
            )
        }
    }


    override suspend fun getAllStaticMaps(): List<FirestoreStaticMapDocument> {
        return try {
            firestore.collection(FirestoreCollections.STATIC_MAPS)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(StaticMapOnlineEntity::class.java)?.let { entity ->
                        FirestoreStaticMapDocument(
                            firebaseId = doc.id,
                            staticMap = entity
                        )
                    }
                }
        } catch (e: Exception) {
            throw FirebaseStaticMapDownloadException("Failed to get all static maps: ${e.message}", e)
        }
    }

    override suspend fun deleteStaticMap(firebaseStaticMapId: String) {
        try {
            firestore.collection(FirestoreCollections.STATIC_MAPS)
                .document(firebaseStaticMapId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebaseStaticMapDeleteException("Failed to delete static map: ${e.message}", e)
        }
    }

    override suspend fun deleteStaticMapByPropertyId(firebasePropertyId: String) {
        try {
            val snapshots = firestore.collection(FirestoreCollections.STATIC_MAPS)
                .whereEqualTo("propertyId", firebasePropertyId)
                .get()
                .await()
            snapshots.documents.forEach { it.reference.delete().await() }
        } catch (e: Exception) {
            throw FirebaseStaticMapDeleteException("Failed to delete static maps by propertyId: ${e.message}", e)
        }
    }

    override suspend fun downloadImageLocally(storageUrl: String): String {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(storageUrl)
        val localFile = withContext(Dispatchers.IO) {
            File.createTempFile("staticMap_", ".jpg")
        }
        storageRef.getFile(localFile).await()
        return localFile.toURI().toString()
    }
}

class FirebaseStaticMapUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebaseStaticMapDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebaseStaticMapDeleteException(message: String, cause: Throwable?) : Exception(message, cause)

data class FirestoreStaticMapDocument(
    val firebaseId: String,                      // => Firebase UID (document ID)
    val staticMap: StaticMapOnlineEntity
)