package com.dcac.realestatemanager.data.sync.user

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class UserDownloadManager(
    private val userRepository: UserRepository,               // Room repository (local)
    private val userOnlineRepository: UserOnlineRepository    // Firestore repository (remote)
) {

    // Downloads users from Firestore and syncs them into Room
    suspend fun downloadUnSyncedUsers(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()  // Result list for tracking sync status

        try {
            // 🔽 Get all users stored in Firestore
            val onlineUsers = userOnlineRepository.getAllUsers()

            // Loop through each Firestore user
            for (user in onlineUsers) {
                try {
                    // 🔍 Try to find corresponding local user
                    val localUser = userRepository.getUserById(user.id).first()

                    if (localUser == null) {
                        // ➕ New user → insert into Room
                        userRepository.cacheUserFromFirebase(user.copy(isSynced = true))
                        Log.d("UserDownloadManager", "Inserted user: ${user.email}")
                        results.add(SyncStatus.Success("User ${user.email} inserted"))
                    } else {
                        // 🔁 Compare fields to detect changes
                        val isSame = localUser.email == user.email &&
                                localUser.agentName == user.agentName &&
                                localUser.firebaseUid == user.firebaseUid

                        if (!isSame) {
                            // 📝 Data mismatch → update Room
                            userRepository.updateUser(user.copy(isSynced = true))
                            Log.d("UserDownloadManager", "Updated user: ${user.email}")
                            results.add(SyncStatus.Success("User ${user.email} updated"))
                        } else {
                            // ✅ User already up-to-date
                            Log.d("UserDownloadManager", "User already up-to-date: ${user.email}")
                            results.add(SyncStatus.Success("User ${user.email} already up-to-date"))
                        }
                    }

                } catch (e: Exception) {
                    // ❌ Error while handling individual user
                    results.add(SyncStatus.Failure("User ${user.email}", e))
                }
            }

        } catch (e: Exception) {
            // ❌ Error during global Firestore fetch
            results.add(SyncStatus.Failure("UserDownload (fetch failed)", e))
        }

        return results  // Return overall sync result
    }
}
