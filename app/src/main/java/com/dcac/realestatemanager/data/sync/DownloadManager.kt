package com.dcac.realestatemanager.data.sync

import android.util.Log
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadManager

class DownloadManager(
    private val propertyDownloadManager: PropertyDownloadManager,
    private val photoDownloadManager: PhotoDownloadManager,
    private val poiDownloadManager: PoiDownloadManager,
    private val userDownloadManager: UserDownloadManager,
    private val propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadManager,
) {

    suspend fun downloadAll(): List<SyncStatus> {
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