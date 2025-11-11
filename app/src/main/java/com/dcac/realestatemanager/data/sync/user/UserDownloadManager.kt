package com.dcac.realestatemanager.data.sync.user

import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class UserDownloadManager(
    private val userRepository: UserRepository,               // Room repository (local)
    private val userOnlineRepository: UserOnlineRepository    // Firestore repository (remote)
) : UserDownloadInterfaceManager {

    override suspend fun downloadUnSyncedUsers(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineUsers = userOnlineRepository.getAllUsers()

            for (doc in onlineUsers) {
                try {
                    val userOnline = doc.user
                    val localId = userOnline.universalLocalId
                    val localUser = userRepository.getUserById(localId).first()

                    val shouldDownload = localUser == null || userOnline.updatedAt > localUser.updatedAt

                    if (shouldDownload) {
                        userRepository.insertUserInsertFromFirebase(
                            userOnline,doc.firebaseId)
                        results.add(SyncStatus.Success("User $localId downloaded"))
                    } else {
                        results.add(SyncStatus.Success("User $localId already up-to-date"))
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("User ${doc.firebaseId} failed to sync", e))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global user download failed", e))
        }

        return results
    }
}
