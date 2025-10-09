package com.dcac.realestatemanager.data.sync.globalManager

import com.dcac.realestatemanager.data.sync.SyncStatus

interface DownloadInterfaceManager {

    suspend fun downloadAll(): List<SyncStatus>
}