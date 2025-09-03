package com.dcac.realestatemanager.data.sync.property

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.onlineDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyUploadManager(
    private val propertyRepository: PropertyRepository,                 // Local Room repository
    private val propertyOnlineRepository: PropertyOnlineRepository      // Firestore repository
) {

    // Uploads all unsynced local properties to Firestore
    suspend fun syncUnSyncedProperties(): List<SyncStatus> {
        // Get local properties with isSynced = false
        val unSyncedProperties = propertyRepository.getUnSyncedProperties().first()
        val results = mutableListOf<SyncStatus>()                      // To store result of each sync

        for (property in unSyncedProperties) {
            try {
                // Update timestamp before uploading
                val updatedProperty = property.copy(updatedAt = System.currentTimeMillis())

                // Upload to Firestore
                val syncedProperty = propertyOnlineRepository.uploadProperty(updatedProperty, property.id.toString())

                // Mark local copy as synced
                propertyRepository.updateProperty(syncedProperty)

                Log.d("PropertyUploadManager", "Synced property: ${property.title}")
                results.add(SyncStatus.Success("Property ${property.id}"))

            } catch (e: Exception) {
                // Handle individual upload failure
                results.add(SyncStatus.Failure("Property ${property.id}", e))
            }
        }

        return results  // Return list of all sync attempts
    }
}
