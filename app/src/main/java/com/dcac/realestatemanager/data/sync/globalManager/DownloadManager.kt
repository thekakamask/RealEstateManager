package com.dcac.realestatemanager.data.sync.globalManager

import android.util.Log
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadInterfaceManager

class DownloadManager(
    private val propertyDownloadManager: PropertyDownloadInterfaceManager,
    private val photoDownloadManager: PhotoDownloadInterfaceManager,
    private val poiDownloadManager: PoiDownloadInterfaceManager,
    private val userDownloadManager: UserDownloadInterfaceManager,
    private val propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadInterfaceManager,
): DownloadInterfaceManager {

    override suspend fun downloadAll(): List<SyncStatus> {
        val userResults = userDownloadManager.downloadUnSyncedUsers()
        val photoResults = photoDownloadManager.downloadUnSyncedPhotos()
        val poiResults = poiDownloadManager.downloadUnSyncedPoiS()
        val crossResults = propertyPoiCrossDownloadManager.downloadUnSyncedPropertyPoiCross()
        val propertyResults = propertyDownloadManager.downloadUnSyncedProperties()

        val allResults = userResults + photoResults + poiResults + crossResults + propertyResults

        allResults.filterIsInstance<SyncStatus.Failure>().forEach {
            Log.e("DownloadManager", "Failed to download: ${it.label} — ${it.error.message}")
        }

        return allResults
    }
}