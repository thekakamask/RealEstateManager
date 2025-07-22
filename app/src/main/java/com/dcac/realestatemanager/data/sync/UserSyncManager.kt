package com.dcac.realestatemanager.data.sync

import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.userOnlineConnection.AuthRepository
import kotlinx.coroutines.flow.first

class UserSyncManager(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {
    suspend fun syncUnSyncedUsers(): List<SyncStatus> {
        val unSyncedUsers = userRepository.getUnSyncedUsers().first()
        val results = mutableListOf<SyncStatus>()

        for (user in unSyncedUsers) {
            val result = authRepository.signUpWithEmail(user.email, user.password)
            if (result.isSuccess) {
                userRepository.updateUser(user.copy(isSynced = true))
                results.add(SyncStatus.Success(user.email))
            } else {
                results.add(
                    SyncStatus.Failure(
                        user.email,
                        result.exceptionOrNull() ?: Exception("Unknown error")
                    )
                )
            }
        }

        return results
    }
}