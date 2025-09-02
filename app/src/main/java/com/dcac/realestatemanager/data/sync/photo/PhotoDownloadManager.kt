package com.dcac.realestatemanager.data.sync.photo

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.onlineDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PhotoDownloadManager(
    private val photoRepository: PhotoRepository,
    private val photoOnlineRepository: PhotoOnlineRepository
) {

    suspend fun downloadUnSyncedPhotos(): List <SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlinePhotos = photoOnlineRepository.getAllPhotos()

            for (photo in onlinePhotos) {
                try {
                    val localPhoto = photoRepository.getPhotoById(photo.id).first()

                    if (localPhoto == null) {
                        photoRepository.cachePhotoFromFirebase(photo.copy(isSynced = true))
                        Log.d("PhotoDownloadManager", "Inserted photo: ${photo.description}")
                        results.add(SyncStatus.Success("Photo ${photo.description} inserted"))
                    } else {
                        val isSame = localPhoto.uri == photo.uri &&
                                localPhoto.description == photo.description &&
                                localPhoto.propertyId == photo.propertyId
                        if (!isSame) {
                         photoRepository.updatePhoto(photo.copy(isSynced = true))
                         Log.d("PhotoDownloadManager", "Updated photo: ${photo.description}")
                         results.add(SyncStatus.Success("Photo ${photo.description} updated"))
                        } else {
                            // ✅ Already up-to-date
                            Log.d("PhotoDownloadManager", "Photo already up-to-date: ${photo.description}")
                            results.add(SyncStatus.Success("Photo ${photo.description} already up-to-date"))
                        }
                    }
                } catch (e: Exception) {
                results.add(SyncStatus.Failure("Photo ${photo.description}", e))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("PhotoDownload (fetch failed)", e))
        }

        return results

    }
}