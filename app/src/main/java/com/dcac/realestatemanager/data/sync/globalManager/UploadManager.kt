package com.dcac.realestatemanager.data.sync.globalManager

import android.util.Log
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserUploadInterfaceManager

// CENTRAL MANAGER THAT TRIGGERS ALL SYNC TASKS IN THE APP
// USES INDIVIDUAL SYNC MANAGERS FOR EACH ENTITY TYPE (e.g., users, properties, etc.)
class UploadManager(
    private val userUploadManager: UserUploadInterfaceManager,
    private val photoUploadManager: PhotoUploadInterfaceManager,
    private val poiUploadManager: PoiUploadInterfaceManager,
    private val crossSyncManager: PropertyPoiCrossUploadInterfaceManager,
    private val propertyUploadManager: PropertyUploadInterfaceManager
    // OTHERS SYNC MANAGERS CAN BE ADDED HERE (E.G. PROPERTY, PHOTO, ETC.)
): UploadInterfaceManager {

    // SYNCHRONIZES ALL UNSYNCED ENTITIES (CURRENTLY ONLY USERS)
    override suspend fun syncAll(): List<SyncStatus> {
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
