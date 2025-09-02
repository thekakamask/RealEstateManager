package com.dcac.realestatemanager.data.sync.user

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

// THIS CLASS HANDLES SYNCING LOCAL USER DATA TO THE ONLINE FIRESTORE DATABASE
// IT IS NOT RESPONSIBLE FOR ACCOUNT CREATION — ONLY SYNCING PROFILE DATA TO FIRESTORE
class UserUploadManager(
    private val userRepository: UserRepository,             // LOCAL USER REPOSITORY (ROOM)
    private val userOnlineRepository: UserOnlineRepository  // REMOTE USER REPOSITORY (FIRESTORE)
) {

    // SYNCHRONIZES ALL LOCAL USERS THAT ARE MARKED AS NOT SYNCED
    suspend fun syncUnSyncedUsers(): List<SyncStatus> {

        // FETCH THE LIST OF USERS FROM ROOM THAT HAVE isSynced = false
        val unSyncedUsers = userRepository.getUnSyncedUsers().first()

        // CREATE A LIST TO STORE SUCCESS OR FAILURE RESULTS FOR EACH USER SYNC
        val results = mutableListOf<SyncStatus>()

        // LOOP THROUGH EACH UNSYNCED USER
        for (user in unSyncedUsers) {

            // CHECK IF USER HAS A VALID FIREBASE UID
            if (user.firebaseUid.isBlank()) {
                // WITHOUT A UID, FIRESTORE SYNC IS NOT POSSIBLE
                results.add(
                    SyncStatus.Failure(
                        label = "User ${user.email}",
                        error = Exception("firebaseUid is missing — cannot sync to Firestore")
                    )
                )

                continue
            }

            try {
                // UPLOAD THE USER DATA TO FIRESTORE UNDER users/{uid}
                // MARK THE USER AS SYNCED IN THE LOCAL DATABASE
                val syncedUser = userOnlineRepository.uploadUser(user, user.firebaseUid)
                userRepository.updateUser(syncedUser)

                Log.d("UserSyncManager", "Synced user: ${user.email}")

                // ADD A SUCCESSFUL SYNC RESULT
                results.add(SyncStatus.Success(user.email))

            } catch (e: Exception) {
                // IF UPLOAD FAILS, STORE FAILURE RESULT
                results.add(SyncStatus.Failure(user.email, e))
            }
        }

        // RETURN THE COMPLETE RESULT LIST FOR LOGGING OR RETRYING
        return results
    }
}
