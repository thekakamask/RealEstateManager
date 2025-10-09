package com.dcac.realestatemanager.data.sync.property

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PropertyUploadInterfaceManager {

    suspend fun syncUnSyncedProperties(): List<SyncStatus>
}