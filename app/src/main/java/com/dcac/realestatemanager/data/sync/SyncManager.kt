package com.dcac.realestatemanager.data.sync

// CENTRAL MANAGER THAT TRIGGERS ALL SYNC TASKS IN THE APP
// USES INDIVIDUAL SYNC MANAGERS FOR EACH ENTITY TYPE (e.g., users, properties, etc.)
class SyncManager(
    private val userSyncManager: UserSyncManager
    // OTHERS SYNC MANAGERS CAN BE ADDED HERE (E.G. PROPERTY, PHOTO, ETC.)
) {

    // SYNCHRONIZES ALL UNSYNCED ENTITIES (CURRENTLY ONLY USERS)
    suspend fun syncAll() {
        // TRIGGERS SYNC OF UNSYNCED USERS FROM ROOM TO FIRESTORE
        userSyncManager.syncUnSyncedUsers()
    }
}
