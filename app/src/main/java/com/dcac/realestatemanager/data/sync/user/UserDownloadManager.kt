package com.dcac.realestatemanager.data.sync.user

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

// HANDLES DOWNLOADING USERS FROM FIRESTORE TO LOCAL ROOM DATABASE
class UserDownloadManager(
    private val userRepository: UserRepository,  // LOCAL ROOM REPOSITORY
    private val userOnlineRepository: UserOnlineRepository  // LOCAL ROOM REPOSITORY
) {

    // DOWNLOADS USERS FROM FIRESTORE, COMPARES WITH LOCAL DATA, INSERTS/UPDATES IF NECESSARY
    suspend fun downloadUnSyncedUsers(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()  // INITIALIZE LIST TO STORE SYNC RESULTS

        try {

            val onlineUsers = userOnlineRepository.getAllUsers() // FETCH ALL USERS FROM FIRESTORE

            for (user in onlineUsers) {   // ITERATE OVER EACH FIRESTORE USER
                try {
                    val localUser = userRepository.getUserById(user.id).first()   // FETCH LOCAL USER BY ID

                    if (localUser == null) {
                        // USER DOES NOT EXIST LOCALLY → INSERT IT
                        userRepository.cacheUserFromFirebase(user.copy(isSynced = true))
                        Log.d("UserDownloadManager", "Inserted user: ${user.email}")
                        results.add(SyncStatus.Success("User ${user.email} inserted"))

                    } else {
                        // USER EXISTS LOCALLY → COMPARE FIELDS TO CHECK FOR DIFFERENCES
                        val isSame = localUser.email == user.email &&
                                localUser.agentName == user.agentName &&
                                localUser.firebaseUid == user.firebaseUid

                        if (!isSame) {
                            // LOCAL USER IS DIFFERENT → UPDATE LOCAL RECORD
                            userRepository.updateUser(user.copy(isSynced = true))
                            Log.d("UserDownloadManager", "Updated user: ${user.email}")
                            results.add(SyncStatus.Success("User ${user.email} updated"))
                        } else {
                            // USER IS IDENTICAL → NO NEED TO UPDATE
                            Log.d("UserDownloadManager", "User already up-to-date: ${user.email}")
                            results.add(SyncStatus.Success("User ${user.email} already up-to-date"))
                        }
                    }

                } catch (e: Exception) {
                    // ERROR OCCURRED FOR A SPECIFIC USER → ADD FAILURE RESULT
                    results.add(SyncStatus.Failure("User ${user.email}", e))
                }
            }

        } catch (e: Exception) {
            // ERROR OCCURRED WHILE FETCHING USERS FROM FIRESTORE
            results.add(SyncStatus.Failure("UserDownload (fetch failed)", e))
        }

        return results // RETURN FINAL LIST OF SYNC STATUSES
    }
}