package com.dcac.realestatemanager.data.sync.staticMap

import com.dcac.realestatemanager.data.sync.SyncStatus

interface StaticMapUploadInterfaceManager {

    suspend fun syncUnSyncedStaticMaps(): List<SyncStatus>
}