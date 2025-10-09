package com.dcac.realestatemanager.data.sync.poi

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PoiUploadInterfaceManager {

    suspend fun syncUnSyncedPoiS(): List<SyncStatus>
}