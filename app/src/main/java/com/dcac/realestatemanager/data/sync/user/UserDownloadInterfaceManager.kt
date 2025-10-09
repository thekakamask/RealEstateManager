package com.dcac.realestatemanager.data.sync.user

import com.dcac.realestatemanager.data.sync.SyncStatus

interface UserDownloadInterfaceManager {

    suspend fun downloadUnSyncedUsers(): List<SyncStatus>
}