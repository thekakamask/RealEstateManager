package com.dcac.realestatemanager.data.sync.propertyPoiCross

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PropertyPoiCrossUploadInterfaceManager {

    suspend fun syncUnSyncedPropertyPoiCross(): List<SyncStatus>
}