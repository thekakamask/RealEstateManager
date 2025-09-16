package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import kotlinx.coroutines.flow.first


class PhotoUploadManager(
    private val photoRepository: PhotoRepository, // Local Room repository for photos
    private val photoOnlineRepository: PhotoOnlineRepository // Firebase repository for photos
) {

    suspend fun syncUnSyncedPhotos(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>() // List to collect sync results

        try {
            // 🔽 Fetch all photos that are not yet synced (from Room)
            val unSyncedPhotos = photoRepository.uploadUnSyncedPhotosToFirebase().first()

            for (photoEntity in unSyncedPhotos) {
                val roomId = photoEntity.id // Local database ID (used as Firebase document ID)

                if (photoEntity.isDeleted) {
                    // ❌ If the photo is marked as deleted locally, delete it from Firebase
                    photoOnlineRepository.deletePhoto(roomId.toString())

                    // ❌ Then delete it from Room
                    photoRepository.deletePhoto(photoEntity)

                    // ✅ Add success status for deletion
                    results.add(SyncStatus.Success("Photo $roomId deleted"))

                } else {
                    // 🕒 Update the `updatedAt` timestamp before uploading
                    val updatedPhoto = photoEntity.copy(updatedAt = System.currentTimeMillis())

                    // ⬆️ Upload the photo to Firebase (Storage + Firestore)
                    val uploadedPhoto = photoOnlineRepository.uploadPhoto(
                        photo = updatedPhoto.toOnlineEntity(), // Map Room entity to Firebase model
                        photoId = roomId.toString() // Use local ID as Firebase document ID
                    )

                    // 💾 Save the uploaded Firebase photo to Room, keeping the local URI
                    photoRepository.downloadPhotoFromFirebase(
                        photo = uploadedPhoto,
                        localUri = photoEntity.uri // Retain the existing local image path
                    )

                    // ✅ Add success status for upload
                    results.add(SyncStatus.Success("Photo $roomId uploaded"))
                }
            }

        } catch (e: Exception) {
            // ❗ Catch any global error (e.g. fetch or general sync failure)
            results.add(SyncStatus.Failure("Global upload sync failed", e))
        }

        // ⏎ Return the list of results (success/failure for each photo)
        return results
    }
}