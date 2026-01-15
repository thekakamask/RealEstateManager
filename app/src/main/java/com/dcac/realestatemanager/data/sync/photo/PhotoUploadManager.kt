package com.dcac.realestatemanager.data.sync.photo

import android.util.Log
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first


class PhotoUploadManager(
    private val photoRepository: PhotoRepository,
    private val photoOnlineRepository: PhotoOnlineRepository
) : PhotoUploadInterfaceManager {

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to sync data")

    override suspend fun syncUnSyncedPhotos(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val photosToSync = photoRepository.uploadUnSyncedPhotosToFirebase().first()


        Log.e("SYNC_PHOTO", "PHOTO TO SYNC COUNT = ${photosToSync.size}")
        photosToSync.forEach {
            Log.e("SYNC_PHOTO", "PHOTO ENTITY = $it")
        }

        for (photo in photosToSync) {
            try {
                val firebaseId = photo.firestoreDocumentId

                if (photo.isDeleted) {
                    if (firebaseId != null) {
                        photoOnlineRepository.markPhotoAsDeleted(
                            firebasePhotoId = firebaseId,
                            updatedAt = photo.updatedAt
                        )
                    }

                    photoRepository.deletePhoto(photo)

                    results.add(
                        SyncStatus.Success("Photo ${photo.id} marked deleted online & removed locally")
                    )
                } else {
                    val finalId = firebaseId ?: generateFirestoreId()

                   Log.e(
                       "SYNC_PHOTO_UPLOAD",
                       "Uploading photo localId=${photo.id} firestoreId=$firebaseId finalId=$finalId"
                   )
                    val updatedOnline = photoOnlineRepository.uploadPhoto(
                        photo.toOnlineEntity(currentUserUid),
                        finalId
                    )


                    photoRepository.updatePhotoFromFirebase(
                        photo = updatedOnline,
                        firestoreId = finalId
                    )

                    Log.e("SYNC_PHOTO_UPLOAD", "Upload OK for photo ${photo.id}")
                    results.add(SyncStatus.Success("Photo ${photo.id} uploaded to Firebase"))
                }

            } catch (e: Exception) {
                Log.e("SYNC_PHOTO_ERROR", "Upload failed for photo ${photo.id} : ${e.message}", e)
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