package com.dcac.realestatemanager.data.sync.user

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class UserUploadManager(
    private val userRepository: UserRepository,              // Room repository (local)
    private val userOnlineRepository: UserOnlineRepository   // Firestore repository (remote)
) {

    // Uploads all local users that are not yet synced (isSynced == false)
    suspend fun syncUnSyncedUsers(): List<SyncStatus> {
        // 🔍 Get users from Room where isSynced == false
        val unSyncedUsers = userRepository.getUnSyncedUsers().first()
        val results = mutableListOf<SyncStatus>()  // Result list to track each upload

        for (user in unSyncedUsers) {
            // ❗ Cannot sync if firebaseUid is missing
            if (user.firebaseUid.isBlank()) {
                results.add(
                    SyncStatus.Failure(
                        label = "User ${user.email}",
                        error = Exception("firebaseUid is missing — cannot sync to Firestore")
                    )
                )
                continue
            }

            try {
                // ☁️ Upload user to Firestore (under users/{firebaseUid})
                val syncedUser = userOnlineRepository.uploadUser(user, user.firebaseUid)

                // ✅ Update local user as synced
                userRepository.updateUser(syncedUser)
                Log.d("UserSyncManager", "Synced user: ${user.email}")

                results.add(SyncStatus.Success(user.email))

            } catch (e: Exception) {
                // ❌ Upload failed
                results.add(SyncStatus.Failure(user.email, e))
            }
        }

        return results  // Return success/failure list
    }
}
