package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first
import java.io.File

class PhotoDownloadManager(
    private val photoRepository: PhotoRepository,
    private val photoOnlineRepository: PhotoOnlineRepository
) : PhotoDownloadInterfaceManager {

    override suspend fun downloadUnSyncedPhotos(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlinePhotos = photoOnlineRepository.getAllPhotos()

            for (doc in onlinePhotos) {
                val photoOnline = doc.photo
                val localId = photoOnline.universalLocalId
                val localPhotoEntity =
                    photoRepository.getPhotoByIdIncludeDeleted(localId).first()

                if (photoOnline.isDeleted) {
                    if (localPhotoEntity != null) {
                        photoRepository.deletePhoto(localPhotoEntity)
                        results.add(
                            SyncStatus.Success("Photo $localId deleted locally (remote deleted)")
                        )
                    }
                    continue
                }

                val shouldDownload =
                    localPhotoEntity == null ||
                            photoOnline.updatedAt > localPhotoEntity.updatedAt

                if (!shouldDownload) {
                    results.add(SyncStatus.Success("Photo $localId already up-to-date"))
                    continue
                }

                val localUri = when {
                    localPhotoEntity?.uri?.isNotBlank() == true &&
                            File(localPhotoEntity.uri).exists() -> {
                        localPhotoEntity.uri
                    }

                    photoOnline.storageUrl.isNotEmpty() -> {
                        photoOnlineRepository.downloadImageLocally(photoOnline.storageUrl)
                    }

                    else -> {
                        results.add(
                            SyncStatus.Failure(
                                "Missing URI for photo $localId",
                                Exception("No local file and no storageUrl")
                            )
                        )
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
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Photo download (global failure)", e))
        }

        return results
    }
}

