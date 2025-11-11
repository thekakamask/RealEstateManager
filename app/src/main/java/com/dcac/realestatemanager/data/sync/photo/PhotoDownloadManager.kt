package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PhotoDownloadManager(
    private val photoRepository: PhotoRepository,
    private val photoOnlineRepository: PhotoOnlineRepository
) : PhotoDownloadInterfaceManager {

    override suspend fun downloadUnSyncedPhotos(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlinePhotos = photoOnlineRepository.getAllPhotos()

            for (doc in onlinePhotos) {
                try {
                    val photoOnline = doc.photo
                    val localId = photoOnline.universalLocalId
                    val localPhotoEntity = photoRepository.getPhotoByIdIncludeDeleted(localId).first()

                    val shouldDownload =
                        localPhotoEntity == null || photoOnline.updatedAt > localPhotoEntity.updatedAt

                    if (shouldDownload) {
                        val localUri = when {
                            photoOnline.storageUrl.isNotEmpty() ->
                                photoOnlineRepository.downloadImageLocally(photoOnline.storageUrl)
                            localPhotoEntity != null -> localPhotoEntity.uri
                            else -> {
                                results.add(SyncStatus.Failure("Missing URI for photo $localId", Exception("Invalid state")))
                                continue
                            }
                        }

                        if (localPhotoEntity == null) {
                            photoRepository.insertPhotoInsertFromFirebase(
                                photo = photoOnline,
                                firestoreId = doc.firebaseId,
                                localUri = localUri
                            )
                            results.add(SyncStatus.Success("Photo $localId inserted"))
                        } else {
                            photoRepository.updatePhotoFromFirebase(
                                photo = photoOnline,
                                firestoreId = doc.firebaseId
                            )
                            results.add(SyncStatus.Success("Photo $localId updated"))
                        }
                    } else {
                        results.add(SyncStatus.Success("Photo $localId already up-to-date"))
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("Photo ${doc.firebaseId}", e))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Photo download (global failure)", e))
        }

        return results
    }
}

