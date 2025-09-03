package com.dcac.realestatemanager.data.sync.photo

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.onlineDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PhotoUploadManager(
    private val photoRepository: PhotoRepository,                // Local (Room) photo repository
    private val photoOnlineRepository: PhotoOnlineRepository     // Remote (Firestore) photo repository
) {

    // Uploads all unsynced photos (isSynced = false) to Firestore
    suspend fun syncUnSyncedPhotos(): List<SyncStatus> {
        // Fetch all unsynced photos from Room
        val unSyncedPhotos = photoRepository.getUnSyncedPhotos().first()
        val results = mutableListOf<SyncStatus>()                // To track success/failure of each upload

        for (photo in unSyncedPhotos) {
            try {
                // Update the photo with the current timestamp for sync tracking
                val updatedPhoto = photo.copy(updatedAt = System.currentTimeMillis())

                // Upload the updated photo to Firestore
                val syncedPhoto = photoOnlineRepository.uploadPhoto(updatedPhoto, updatedPhoto.id.toString())

                // Save the synced photo back to Room with isSynced = true
                photoRepository.updatePhoto(syncedPhoto)

                // Log and record successful sync
                Log.d("PhotoUploadManager", "Synced photo: ${photo.uri}")
                results.add(SyncStatus.Success("Photo ${photo.id}"))

            } catch (e: Exception) {
                // Record failure for this specific photo
                results.add(SyncStatus.Failure("Photo ${photo.id}", e))
            }
        }

        return results  // Return list of sync results
    }

}
