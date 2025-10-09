package com.dcac.realestatemanager.data.sync.poi

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PoiDownloadInterfaceManager {

    suspend fun downloadUnSyncedPoiS(): List<SyncStatus>
}