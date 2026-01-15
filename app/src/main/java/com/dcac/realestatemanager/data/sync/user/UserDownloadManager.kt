package com.dcac.realestatemanager.data.sync.user

import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class UserDownloadManager(
    private val userRepository: UserRepository,
    private val userOnlineRepository: UserOnlineRepository
) : UserDownloadInterfaceManager {

    override suspend fun downloadUnSyncedUsers(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineUsers = userOnlineRepository.getAllUsers()

            for (doc in onlineUsers) {
                val online = doc.user
                val localId = online.universalLocalId

                val local =
                    userRepository.getUserByIdIncludeDeleted(localId).first()

                if (online.isDeleted) {
                    if (local != null) {
                        userRepository.deleteUser(local)
                        results.add(
                            SyncStatus.Success(
                                "User $localId deleted locally (remote deleted)"
                            )
                        )
                    }
                    continue
                }

                val shouldDownload =
                    local == null || online.updatedAt > local.updatedAt

                if (!shouldDownload) {
                    results.add(
                        SyncStatus.Success("User $localId already up-to-date")
                    )
                    continue
                }

                if (local == null) {
                    userRepository.insertUserInsertFromFirebase(
                        user = online,
                        firebaseUid = doc.firebaseId
                    )
                    results.add(
                        SyncStatus.Success("User $localId inserted")
                    )
                } else {
                    userRepository.updateUserFromFirebase(
                        user = online,
                        firebaseUid = doc.firebaseId
                    )
                    results.add(
                        SyncStatus.Success("User $localId updated")
                    )
                }
            }

        } catch (e: Exception) {
            results.add(
                SyncStatus.Failure("Global user download failed", e)
            )
        }

        return results
    }
}

