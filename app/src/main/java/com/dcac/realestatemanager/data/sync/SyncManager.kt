package com.dcac.realestatemanager.data.sync

class SyncManager(
    private val userSyncManager: UserSyncManager
    // Others sync managers here
) {
    suspend fun syncAll() {
        userSyncManager.syncUnSyncedUsers()
    }
}