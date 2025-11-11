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

    override suspend fun syncUnSyncedUsers(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val usersToSync = userRepository.uploadUnSyncedUsersToFirebase().first()

        for (userEntity in usersToSync) {
            val firebaseId = userEntity.firebaseUid
            val localId = userEntity.id

            try {
                if (userEntity.isDeleted) {
                    userOnlineRepository.deleteUser(firebaseId)
                    userRepository.deleteUser(userEntity)
                    results.add(SyncStatus.Success("User $localId deleted from Firebase & Room"))
                } else {
                    val uploadedUser = userOnlineRepository.uploadUser(
                        user = userEntity.toOnlineEntity(),
                        firebaseUserId = firebaseId
                    )

                    userRepository.updateUserFromFirebase(
                        user = uploadedUser,
                        firebaseUid = firebaseId
                    )

                    results.add(SyncStatus.Success("User $localId uploaded to Firebase"))
                }

            } catch (e: Exception) {
                results.add(SyncStatus.Failure("User $localId", e))
            }
        }

        return results
    }

}
