package com.dcac.realestatemanager.data.onlineDatabase.photo

import android.net.Uri
import com.dcac.realestatemanager.data.onlineDatabase.FirestoreCollections
import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.utils.toModel
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri


class FirebasePhotoOnlineRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
): PhotoOnlineRepository {

    override suspend fun uploadPhoto(photo: Photo, photoId: String): Photo {
        try {
            val fileUri = photo.uri.toUri()

            if (fileUri.toString().isEmpty()) {
                throw IllegalArgumentException("Photo URI is empty. Cannot upload to Firebase Storage.")
            }

            // 🔼 Upload image to firebase storage
            val storageRef = storage.reference.child("photos/${photoId}.jpg")
            storageRef.putFile(fileUri).await()

            // 🔁 Recover public URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // 🧱 Creation of the entity with the cloud URL
            val entity = photo.copy(storageUrl = downloadUrl).toOnlineEntity()

            // ☁️ Upload on Firestore
            firestore.collection(FirestoreCollections.PHOTOS)
                .document(photoId)
                .set(entity)
                .await()

            // ✅ return object with sync at true
            return photo.copy(
                isSynced = true,
                storageUrl = downloadUrl
            )

        } catch (e: Exception) {
            throw FirebasePhotoUploadException("Failed to upload photo: ${e.message}", e)
        }
    }


    override suspend fun getPhoto(photoId: String): Photo? {
        val idLong = photoId.toLongOrNull() ?: return null
        val snapshot = firestore.collection(FirestoreCollections.PHOTOS)
            .document(photoId)
            .get()
            .await()

        val entity = snapshot.toObject(PhotoOnlineEntity::class.java)
        return entity?.toModel(photoId = idLong)
    }

    override suspend fun getPhotosByPropertyId(propertyId: Long): List<Photo> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.PHOTOS)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                doc.toObject(PhotoOnlineEntity::class.java)
                    ?.toModel(photoId = doc.id.toLongOrNull() ?: return@mapNotNull null)
            }


        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to get photos: ${e.message}", e)
        }
    }

    override suspend fun getAllPhotos(): List<Photo> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.PHOTOS)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                val entity = doc.toObject(PhotoOnlineEntity::class.java)
                val photoId = doc.id.toLongOrNull()
                if (entity != null && photoId != null) {
                    entity.toModel(photoId = photoId)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to download photos: ${e.message}", e)
        }
    }

    override suspend fun deletePhoto(photoId: String) {
        try {
            firestore.collection(FirestoreCollections.PHOTOS)
                .document(photoId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebasePhotoDeleteException("Failed to delete photo: ${e.message}", e)
        }
    }

    override suspend fun deletePhotosByPropertyId(propertyId: Long) {
        try {
            val snapshots = firestore.collection(FirestoreCollections.PHOTOS)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()

            for (doc in snapshots.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            throw FirebasePhotoDeleteException("Failed to delete photos by propertyId: ${e.message}", e)
        }
    }
}

class FirebasePhotoUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePhotoDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePhotoDeleteException(message: String, cause: Throwable?) : Exception(message, cause)