package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.onlineDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class PhotoDownloadManager(
    private val photoRepository: PhotoRepository,                // Local (Room) photo repository
    private val photoOnlineRepository: PhotoOnlineRepository     // Remote (Firestore) photo repository
) {

    // Downloads all photos from Firestore and updates Room if newer
    suspend fun downloadUnSyncedPhotos(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()                // To track success/failure of each sync

        try {
            // Fetch all photos from Firestore
            val onlinePhotos = photoOnlineRepository.getAllPhotos()

            for (photo in onlinePhotos) {
                try {
                    // Check if this photo already exists locally
                    val localPhoto = photoRepository.getPhotoById(photo.id).first()

                    if (localPhoto == null) {
                        // download locally if storage url exists
                        val uri = if (photo.storageUrl.isNotEmpty()) {
                            downloadImageLocally(photo.storageUrl)
                        } else {
                            ""
                        }

                        val newPhoto = photo.copy(
                            isSynced = true,
                            uri = uri
                        )

                        photoRepository.cachePhotoFromFirebase(newPhoto)
                        results.add(SyncStatus.Success("Photo ${photo.description} inserted"))

                    } else if (photo.updatedAt > localPhoto.updatedAt) {
                        // download again if needed
                        val uri = if (photo.storageUrl.isNotEmpty()) {
                            downloadImageLocally(photo.storageUrl)
                        } else {
                            localPhoto.uri // keep previous if no storage url
                        }

                        val updatedPhoto = photo.copy(
                            isSynced = true,
                            uri = uri
                        )

                        photoRepository.updatePhoto(updatedPhoto)
                        results.add(SyncStatus.Success("Photo ${photo.description} updated"))

                    } else {
                        results.add(SyncStatus.Success("Photo ${photo.description} already up-to-date"))
                    }


                } catch (e: Exception) {
                    // Handle sync failure for this specific photo
                    results.add(SyncStatus.Failure("Photo ${photo.description}", e))
                }
            }
        } catch (e: Exception) {
            // Handle total fetch failure
            results.add(SyncStatus.Failure("PhotoDownload (fetch failed)", e))
        }

        return results  // Return list of sync results
    }

    private suspend fun downloadImageLocally(storageUrl: String): String {
        //download image from firebase storage and save it locally
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(storageUrl)
        val localFile = withContext(Dispatchers.IO) {
            File.createTempFile("photo_", ".jpg")
        }

        storageRef.getFile(localFile).await()

        return localFile.toURI().toString() // or localFile.toUri().toString() if content://
    }
}
