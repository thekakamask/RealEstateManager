package com.dcac.realestatemanager.data.sync.staticMap

import com.dcac.realestatemanager.data.sync.SyncStatus

interface StaticMapDownloadInterfaceManager {

    suspend fun downloadUnSyncedStaticMaps(): List<SyncStatus>
}