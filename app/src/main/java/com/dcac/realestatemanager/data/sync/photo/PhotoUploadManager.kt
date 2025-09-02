package com.dcac.realestatemanager.data.sync.photo

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.onlineDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

// THIS CLASS HANDLES SYNCING LOCAL PHOTO DATA TO THE ONLINE FIRESTORE DATABASE
// ONLY PHOTOS MARKED ARE NOT SYNCED (ISSYNCED = FALSE) WILL BE UPLOADED.

class PhotoUploadManager(
    private val photoRepository: PhotoRepository,  // LOCAL PHOTO REPOSITORY (ROOM)
    private val photoOnlineRepository: PhotoOnlineRepository // REMOTE PHOTO REPOSITORY (FIRESTORE)
) {

    // SYNCHRONIZES ALL LOCAL PHOTOS THAT ARE MARKED AS NOT SYNCED
    suspend fun syncUnSyncedPhotos(): List <SyncStatus> {

        // FETCH THE LIST OF PHOTOS FROM ROOM THAT HAVE isSynced = false
        val unSyncedPhotos = photoRepository.getUnSyncedPhotos().first()

        // CREATE A LIST TO STORE SUCCESS OR FAILURE RESULTS FOR EACH PHOTO SYNC
        val results = mutableListOf<SyncStatus>()

        for (photo in unSyncedPhotos) {
            try {
                //UPLOAD PHOTO TO FIRESTORE
                val syncedPhoto = photoOnlineRepository.uploadPhoto(photo, photo.id.toString())
                //MARK AS SYNCED IN LOCAL DATABASE
                photoRepository.updatePhoto(syncedPhoto)
                Log.d("PhotoSyncManager", "Synced photo: ${photo.uri}")

                results.add(SyncStatus.Success("Photo ${photo.id}"))
            } catch (e: Exception) {
                results.add(SyncStatus.Failure("Photo ${photo.id}", e))
            }
        }
        return results
    }
}