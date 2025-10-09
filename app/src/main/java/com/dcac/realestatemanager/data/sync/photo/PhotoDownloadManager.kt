package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class PhotoDownloadManager(
    private val photoRepository: PhotoRepository,                // Local Room repository
    private val photoOnlineRepository: PhotoOnlineRepository     // Remote Firebase repository
)  : PhotoDownloadInterfaceManager {

    // Syncs all online photos to local database if they are new or updated
    override suspend fun downloadUnSyncedPhotos(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>() // Holds the result for each photo sync

        try {
            // 🔁 Step 1: Get all photos from Firebase Firestore
            val onlinePhotos = photoOnlineRepository.getAllPhotos()

            for (photoOnline in onlinePhotos) { // Iterate over each online photo
                try {
                    val roomId = photoOnline.roomId // This is the Room ID stored in Firebase

                    // 🔍 Step 2: Try to fetch the local photo from Room using Room ID
                    val localPhoto = photoRepository.getPhotoEntityById(roomId).first()

                    // 🧠 Step 3: Decide whether to sync
                    // - If the photo doesn't exist locally OR
                    // - If the online version is more recent (updatedAt comparison)
                    val shouldDownload = localPhoto == null || photoOnline.updatedAt > localPhoto.updatedAt

                    if (shouldDownload) {
                        // 📥 Step 4: Download the actual image from Firebase Storage
                        val localUri = if (photoOnline.storageUrl.isNotEmpty()) {
                            downloadImageLocally(photoOnline.storageUrl) // Download image and get its local URI
                        } else {
                            localPhoto?.uri ?: "" // Fallback if no storageUrl available
                        }

                        // 💾 Step 5: Save the photo to Room database with the downloaded URI
                        photoRepository.downloadPhotoFromFirebase(
                            photo = photoOnline,        // Online metadata
                            localUri = localUri         // Path to image saved locally
                        )

                        // ✅ Step 6: Log the result
                        val status = if (localPhoto == null) "inserted" else "updated"
                        results.add(SyncStatus.Success("Photo $roomId $status"))
                    } else {
                        // ⚠️ No sync needed, already up-to-date
                        results.add(SyncStatus.Success("Photo $roomId already up-to-date"))
                    }

                } catch (e: Exception) {
                    // ❌ Error for this specific photo
                    results.add(SyncStatus.Failure("Photo ${photoOnline.roomId}", e))
                }
            }

        } catch (e: Exception) {
            // ❌ Global failure (e.g. cannot fetch from Firebase)
            results.add(SyncStatus.Failure("Photo download (global failure)", e))
        }

        return results // 🔚 Return all individual results (success or failure)
    }

    // Downloads the image from Firebase Storage and saves it as a temporary file
    private suspend fun downloadImageLocally(storageUrl: String): String {
        // 🔗 Get the Firebase Storage reference from the public URL
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(storageUrl)

        // 📁 Create a temporary file on the device (e.g., photo_12345.jpg)
        val localFile = withContext(Dispatchers.IO) {
            File.createTempFile("photo_", ".jpg")
        }

        // 📦 Download the image and block until complete (suspend function)
        storageRef.getFile(localFile).await()

        // 🧾 Return the file URI as a String (Room uses this as `uri`)
        return localFile.toURI().toString()
    }
}
