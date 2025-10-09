package com.dcac.realestatemanager.data.sync.property

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PropertyDownloadInterfaceManager {

    suspend fun downloadUnSyncedProperties(): List<SyncStatus>
}