package com.dcac.realestatemanager.data.sync.user

import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class UserDownloadManager(
    private val userRepository: UserRepository,               // Room repository (local)
    private val userOnlineRepository: UserOnlineRepository    // Firestore repository (remote)
) {

    // Downloads users from Firestore and syncs them into Room
    suspend fun downloadUnSyncedUsers(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        try {
            val onlineUsers = userOnlineRepository.getAllUsers()

            for (onlineUser in onlineUsers) {
                try {
                    val roomId = onlineUser.roomId
                    val localUser = userRepository.getUserEntityById(roomId).first()

                    val shouldDownload = localUser == null || onlineUser.updatedAt > localUser.updatedAt

                    if (shouldDownload) {
                        userRepository.downloadUserFromFirebase(onlineUser)
                        results.add(SyncStatus.Success("User $roomId downloaded"))
                    } else {
                        results.add(SyncStatus.Success("User $roomId already up-to-date"))
                    }
                } catch (e : Exception) {
                    results.add(SyncStatus.Failure("User ${onlineUser.roomId} failed to sync", e))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global user download failed", e))
        }
        return results
    }
}
