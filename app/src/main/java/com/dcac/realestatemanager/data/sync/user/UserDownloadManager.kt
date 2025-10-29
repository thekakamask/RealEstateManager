package com.dcac.realestatemanager.data.sync.user

import com.dcac.realestatemanager.data.firebaseDatabase.user.FirestoreUserDocument
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class UserDownloadManager(
    private val userRepository: UserRepository,               // Room repository (local)
    private val userOnlineRepository: UserOnlineRepository    // Firestore repository (remote)
) : UserDownloadInterfaceManager {

    // Downloads users from Firestore and syncs them into Room
    override suspend fun downloadUnSyncedUsers(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        try {
            val onlineDocuments: List<FirestoreUserDocument> = userOnlineRepository.getAllUsers()

            for (doc in onlineDocuments) {
                val firebaseUid = doc.id
                val onlineUser = doc.user
                val roomId = onlineUser.roomId

                try {
                    val localUser = userRepository.getUserEntityById(roomId).first()

                    val shouldDownload = localUser == null || onlineUser.updatedAt > localUser.updatedAt

                    if (shouldDownload) {
                        userRepository.downloadUserFromFirebase(onlineUser, firebaseUid)
                        results.add(SyncStatus.Success("User $roomId downloaded"))
                    } else {
                        results.add(SyncStatus.Success("User $roomId already up-to-date"))
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("User $roomId failed to sync", e))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global user download failed", e))
        }

        return results
    }
}
