package com.dcac.realestatemanager.data.firebaseDatabase.photo


import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.PHOTOS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class FirebasePhotoOnlineRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PhotoOnlineRepository {

    override suspend fun uploadPhoto(photo: PhotoOnlineEntity, firebasePhotoId: String): PhotoOnlineEntity {
        try {
            if (photo.storageUrl.isBlank()) {
                throw FirebasePhotoUploadException(
                    "Photo URI is empty",
                    IllegalArgumentException("Photo URI is empty")
                )
            }

            val uri = photo.storageUrl.toUri()
            val storageRef = storage.reference.child("photos/${firebasePhotoId}.jpg")

            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val updatedPhoto = photo.copy(storageUrl = downloadUrl)

            firestore.collection(FirestoreCollections.PHOTOS)
                .document(firebasePhotoId)
                .set(updatedPhoto)
                .await()

            return updatedPhoto
        } catch (e: Exception) {
            // if it is already a FirebasePhotoUploadException, we re-throw it as is
            if (e is FirebasePhotoUploadException) throw e

            throw FirebasePhotoUploadException("Failed to upload photo: ${e.message}", e)
        }
    }

    override suspend fun getPhoto(firebasePhotoId: String): PhotoOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.PHOTOS)
                .document(firebasePhotoId)
                .get()
                .await()

            snapshot.toObject(PhotoOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to get photo: ${e.message}", e)
        }
    }

    override suspend fun getPhotosByPropertyId(firebasePropertyId: String): List<PhotoOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.PHOTOS)
                .whereEqualTo("propertyId", firebasePropertyId)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(PhotoOnlineEntity::class.java) }
        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to get photos: ${e.message}", e)
        }
    }

    override suspend fun getAllPhotos(): List<FirestorePhotoDocument> {
        return try {
            firestore.collection(FirestoreCollections.PHOTOS)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(PhotoOnlineEntity::class.java)?.let { entity ->
                        FirestorePhotoDocument(
                            firebaseId = doc.id,
                            photo = entity
                        )
                    }
                }
        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to get all photos: ${e.message}", e)
        }
    }

    override suspend fun deletePhoto(firebasePhotoId: String) {
        try {
            firestore.collection(FirestoreCollections.PHOTOS)
                .document(firebasePhotoId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePhotoDeleteException("Failed to delete photo: ${e.message}", e)
        }
    }

    override suspend fun deletePhotoFromStorage(storageUrl: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(storageUrl)
        storageRef.delete().await()
    }

    override suspend fun deletePhotosByPropertyId(firebasePropertyId: String) {
        try {
            val snapshots = firestore.collection(FirestoreCollections.PHOTOS)
                .whereEqualTo("propertyId", firebasePropertyId)
                .get()
                .await()

            snapshots.documents.forEach { it.reference.delete().await() }

        } catch (e: Exception) {
            throw FirebasePhotoDeleteException("Failed to delete photos by propertyId: ${e.message}", e)
        }
    }

    override suspend fun downloadImageLocally(storageUrl: String): String {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(storageUrl)
        val localFile = withContext(Dispatchers.IO) {
            File.createTempFile("photo_", ".jpg")
        }
        storageRef.getFile(localFile).await()
        return localFile.toURI().toString()
    }

    override suspend fun markPhotoAsDeleted(firebasePhotoId: String, updatedAt: Long) {
        firestore.collection(PHOTOS)
            .document(firebasePhotoId)
            .update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to updatedAt
                )
            )
            .await()
    }
}

class FirebasePhotoUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePhotoDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePhotoDeleteException(message: String, cause: Throwable?) : Exception(message, cause)

data class FirestorePhotoDocument(
    val firebaseId: String,                      // => Firebase UID (document ID)
    val photo: PhotoOnlineEntity                      // => Photo data
)