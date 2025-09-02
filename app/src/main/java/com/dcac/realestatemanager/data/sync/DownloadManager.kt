package com.dcac.realestatemanager.data.sync

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadManager
import kotlinx.coroutines.flow.first

class DownloadManager(
    private val propertyDownloadManager: PropertyDownloadManager,
    private val photoDownloadManager: PhotoDownloadManager,
    private val poiDownloadManager: PoiDownloadManager,
    private val userDownloadManager: UserDownloadManager,
    private val propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadManager,
    private val userRepository: UserRepository
) {

    suspend fun downloadAll() {
        val userResults = userDownloadManager.downloadUnSyncedUsers()
        val photoResults = photoDownloadManager.downloadUnSyncedPhotos()
        val poiResults = poiDownloadManager.downloadUnSyncedPoiS()
        val crossResults = propertyPoiCrossDownloadManager.downloadUnSyncedPropertyPoiCross()
        val userList = userRepository.getAllUsers().first()
        val propertyResults = propertyDownloadManager.downloadUnSyncedProperties(userList)


        // Log or analyze failures
        (userResults + photoResults + poiResults + crossResults + propertyResults)
            .filterIsInstance<SyncStatus.Failure>()
            .forEach {
                Log.e("DownloadManager", "Failed to download: ${it.label} — ${it.error.message}")
            }
    }
}