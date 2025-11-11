package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first


class PhotoUploadManager(
    private val photoRepository: PhotoRepository,
    private val photoOnlineRepository: PhotoOnlineRepository
) : PhotoUploadInterfaceManager {

    override suspend fun syncUnSyncedPhotos(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val photosToSync = photoRepository.uploadUnSyncedPhotosToFirebase().first()

        for (photo in photosToSync) {
            try {
                val firebaseId = photo.firestoreDocumentId

                if (photo.isDeleted) {
                    if (firebaseId != null) {
                        photoOnlineRepository.deletePhoto(firebaseId)
                    }

                    photoRepository.deletePhoto(photo)

                    results.add(SyncStatus.Success("Photo ${photo.id} deleted from Firebase & Room"))
                } else {
                    val finalId = firebaseId ?: generateFirestoreId()
                    val updatedOnline = photoOnlineRepository.uploadPhoto(
                        photo.toOnlineEntity(),
                        finalId
                    )

                    photoRepository.updatePhotoFromFirebase(
                        photo = updatedOnline,
                        firestoreId = finalId
                    )

                    results.add(SyncStatus.Success("Photo ${photo.id} uploaded to Firebase"))
                }

            } catch (e: Exception) {
                results.add(SyncStatus.Failure("Photo ${photo.id}", e))
            }
        }

        return results
    }

    private fun generateFirestoreId(): String {
        return FirebaseFirestore.getInstance()
            .collection(FirestoreCollections.PHOTOS)
            .document()
            .id
    }
}