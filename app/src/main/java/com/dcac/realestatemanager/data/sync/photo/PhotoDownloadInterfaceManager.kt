package com.dcac.realestatemanager.data.sync.photo

import com.dcac.realestatemanager.data.sync.SyncStatus

interface PhotoDownloadInterfaceManager {

    suspend fun downloadUnSyncedPhotos(): List<SyncStatus>
}