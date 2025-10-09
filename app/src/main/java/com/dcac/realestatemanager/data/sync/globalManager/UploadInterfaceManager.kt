package com.dcac.realestatemanager.data.sync.globalManager

import com.dcac.realestatemanager.data.sync.SyncStatus

interface UploadInterfaceManager {

    suspend fun syncAll(): List<SyncStatus>
}