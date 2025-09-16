package com.dcac.realestatemanager.data.sync

import android.util.Log
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadManager
import com.dcac.realestatemanager.data.sync.user.UserUploadManager

// CENTRAL MANAGER THAT TRIGGERS ALL SYNC TASKS IN THE APP
// USES INDIVIDUAL SYNC MANAGERS FOR EACH ENTITY TYPE (e.g., users, properties, etc.)
class UploadManager(
    private val userUploadManager: UserUploadManager,
    private val photoUploadManager: PhotoUploadManager,
    private val poiUploadManager: PoiUploadManager,
    private val crossSyncManager: PropertyPoiCrossUploadManager,
    private val propertyUploadManager: PropertyUploadManager
    // OTHERS SYNC MANAGERS CAN BE ADDED HERE (E.G. PROPERTY, PHOTO, ETC.)
) {

    // SYNCHRONIZES ALL UNSYNCED ENTITIES (CURRENTLY ONLY USERS)
    suspend fun syncAll(): List<SyncStatus> {
        val userResults = userUploadManager.syncUnSyncedUsers()
        val photoResults = photoUploadManager.syncUnSyncedPhotos()
        val poiResults = poiUploadManager.syncUnSyncedPoiS()
        val crossResults = crossSyncManager.syncUnSyncedPropertyPoiCross()
        val propertyResults = propertyUploadManager.syncUnSyncedProperties()

        val allResults = userResults + photoResults + poiResults + crossResults + propertyResults

        allResults.filterIsInstance<SyncStatus.Failure>().forEach {
            Log.e("UploadManager", "Failed to upload: ${it.label} — ${it.error.message}")
        }

        return allResults
    }
}
