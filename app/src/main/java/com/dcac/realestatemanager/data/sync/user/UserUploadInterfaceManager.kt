package com.dcac.realestatemanager.data.sync.user

import com.dcac.realestatemanager.data.sync.SyncStatus

interface UserUploadInterfaceManager {

    suspend fun syncUnSyncedUsers(): List<SyncStatus>
}