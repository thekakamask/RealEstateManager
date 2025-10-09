package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PhotoUploadInterfaceManager {

    suspend fun syncUnSyncedPhotos(): List<SyncStatus>
}