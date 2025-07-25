package com.dcac.realestatemanager.data.sync

import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.userOnlineConnection.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

// RESPONSIBLE FOR SYNCHRONIZING UNSYNCED USERS FROM ROOM TO FIREBASE AUTH AND FIRESTORE
// RESPONSIBLE FOR SYNCHRONIZING LOCALLY MODIFIED USERS TO FIRESTORE (NO ACCOUNT CREATION HERE)
class UserSyncManager(
    private val userRepository: UserRepository,               // LOCAL USER DATA SOURCE (ROOM)
    private val userOnlineRepository: UserOnlineRepository    // INTERFACE TO FIREBASE FIRESTORE
) {

    // SYNCHRONIZES LOCALLY UPDATED USERS TO FIRESTORE IF NOT YET SYNCED
    suspend fun syncUnSyncedUsers(): List<SyncStatus> {
        // FETCHES ALL LOCAL USERS WHO ARE NOT YET SYNCED (isSynced = false)
        val unSyncedUsers = userRepository.getUnSyncedUsers().first()

        // STORES THE RESULTS OF SYNC OPERATIONS FOR REPORTING PURPOSES
        val results = mutableListOf<SyncStatus>()

        // PROCESS EACH UNSYNCED USER
        for (user in unSyncedUsers) {

            // SAFETY CHECK: ENSURE THE USER HAS A FIREBASE UID (ACCOUNT MUST EXIST IN FIREBASE AUTH)
            if (user.firebaseUid.isBlank()) {
                // IF UID IS MISSING, WE CANNOT SYNC TO FIRESTORE ➝ REPORT FAILURE
                results.add(
                    SyncStatus.Failure(user.email, Exception("firebaseUid is missing — cannot sync to Firestore"))
                )
                continue
            }

            try {
                // CREATE A FIRESTORE ENTITY REPRESENTATION OF THE USER
                val firestoreUser = UserOnlineEntity(
                    email = user.email,
                    agentName = user.agentName,
                    uid = user.firebaseUid
                )

                // UPLOAD THE USER PROFILE TO FIRESTORE UNDER `users/{uid}`
                userOnlineRepository.uploadUser(firestoreUser, user.firebaseUid)

                // MARK THE USER LOCALLY AS SYNCED
                userRepository.updateUser(user.copy(isSynced = true))

                // TRACK SUCCESSFUL SYNC
                results.add(SyncStatus.Success(user.email))

            } catch (e: Exception) {
                // TRACK ANY FAILURE DURING FIRESTORE UPLOAD
                results.add(SyncStatus.Failure(user.email, e))
            }
        }

        // RETURN THE FULL LIST OF SYNC RESULTS
        return results
    }
}