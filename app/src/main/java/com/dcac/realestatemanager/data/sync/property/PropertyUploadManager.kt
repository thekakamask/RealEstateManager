package com.dcac.realestatemanager.data.sync.property

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.onlineDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyUploadManager(
    private val propertyRepository: PropertyRepository,
    private val propertyOnlineRepository: PropertyOnlineRepository
) {

    suspend fun syncUnSyncedProperties() : List<SyncStatus> {
        val unSyncedProperties = propertyRepository.getUnSyncedProperties().first()
        val results = mutableListOf<SyncStatus>()

        for (property in unSyncedProperties) {
            try {
                val syncedProperty = propertyOnlineRepository.uploadProperty(property, property.id.toString())
                propertyRepository.updateProperty(syncedProperty)
                Log.d("PropertySyncManager", "Synced property: ${property.title}")

                results.add(SyncStatus.Success("Property ${property.id}"))
            } catch (e: Exception) {
                results.add(SyncStatus.Failure("Property ${property.id}", e))
            }
        }

        return results
    }
}