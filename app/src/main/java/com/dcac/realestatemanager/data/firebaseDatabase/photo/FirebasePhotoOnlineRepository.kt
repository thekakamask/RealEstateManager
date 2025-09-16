package com.dcac.realestatemanager.data.firebaseDatabase.photo


import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri


class FirebasePhotoOnlineRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PhotoOnlineRepository {

    override suspend fun uploadPhoto(photo: PhotoOnlineEntity, photoId: String): PhotoOnlineEntity {
        try {
            val storageRef = storage.reference.child("photos/${photoId}.jpg")
            val uri = photo.storageUrl.toUri()

            // 🔽 Upload to Firebase Storage
            storageRef.putFile(uri).await()

            // 🔁 Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // ⬆️ Save entity in Firestore with updated URL
            val updatedPhoto = photo.copy(storageUrl = downloadUrl)

            firestore.collection(FirestoreCollections.PHOTOS)
                .document(photoId)
                .set(updatedPhoto)
                .await()

            return updatedPhoto
        } catch (e: Exception) {
            throw FirebasePhotoUploadException("Failed to upload photo: ${e.message}", e)
        }
    }

    override suspend fun getPhoto(photoId: String): PhotoOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.PHOTOS)
                .document(photoId)
                .get()
                .await()

            snapshot.toObject(PhotoOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to get photo: ${e.message}", e)
        }
    }

    override suspend fun getPhotosByPropertyId(propertyId: Long): List<PhotoOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.PHOTOS)
                .whereEqualTo("propertyId", propertyId)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(PhotoOnlineEntity::class.java) }
        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to get photos: ${e.message}", e)
        }
    }

    override suspend fun getAllPhotos(): List<PhotoOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.PHOTOS)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(PhotoOnlineEntity::class.java) }
        } catch (e: Exception) {
            throw FirebasePhotoDownloadException("Failed to get all photos: ${e.message}", e)
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

            snapshots.documents.forEach { it.reference.delete().await() }

        } catch (e: Exception) {
            throw FirebasePhotoDeleteException("Failed to delete photos by propertyId: ${e.message}", e)
        }
    }
}

class FirebasePhotoUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePhotoDownloadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebasePhotoDeleteException(message: String, cause: Throwable?) : Exception(message, cause)