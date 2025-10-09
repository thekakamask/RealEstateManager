package com.dcac.realestatemanager.data.sync.user

import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import kotlinx.coroutines.flow.first

class UserUploadManager(
    private val userRepository: UserRepository,              // Room repository (local)
    private val userOnlineRepository: UserOnlineRepository   // Firestore repository (remote)
): UserUploadInterfaceManager {

    // Uploads all local users that are not yet synced (isSynced == false)
    override suspend fun syncUnSyncedUsers(): List<SyncStatus> {

        val results = mutableListOf<SyncStatus>()

        try {
            val unSyncedUsers = userRepository.uploadUnSyncedUsers().first()

            for (userEntity in unSyncedUsers) {
                val userId = userEntity.id

                if (userEntity.isDeleted) {
                    userOnlineRepository.deleteUser(userId.toString())
                    userRepository.deleteUser(userEntity)
                    results.add(SyncStatus.Success("User $userId deleted"))
                } else {
                    val updatedUser = userEntity.copy(updatedAt = System.currentTimeMillis())
                    val uploadedUser = userOnlineRepository.uploadUser(
                        user = updatedUser.toOnlineEntity(),
                        userId = userId.toString()
                    )
                    userRepository.downloadUserFromFirebase(
                        uploadedUser
                    )
                    results.add(SyncStatus.Success("User $userId uploaded"))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global upload sync failed", e))
        }
        return results
    }
}
