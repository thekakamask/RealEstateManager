package com.dcac.realestatemanager.data.sync

class SyncManager(
    private val userSyncManager: UserSyncManager,
) {
    suspend fun syncAll() {
        userSyncManager.syncUnSyncedUsers()
        //OTHER DATA TO SYNC AFTER
    }
}