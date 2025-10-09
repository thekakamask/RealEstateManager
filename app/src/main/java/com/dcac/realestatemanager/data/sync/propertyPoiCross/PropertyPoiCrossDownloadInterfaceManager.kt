package com.dcac.realestatemanager.data.sync.propertyPoiCross

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PropertyPoiCrossDownloadInterfaceManager {

    suspend fun downloadUnSyncedPropertyPoiCross(): List<SyncStatus>
}