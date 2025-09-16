package com.dcac.realestatemanager.data.sync.property

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import kotlinx.coroutines.flow.first

class PropertyUploadManager(
    private val propertyRepository: PropertyRepository,                 // Local Room repository
    private val propertyOnlineRepository: PropertyOnlineRepository      // Firestore repository
) {

    // Uploads all unsynced local properties to Firestore
    suspend fun syncUnSyncedProperties(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()                      // List of success/failure results

        try {
            val unSyncedProperties = propertyRepository.uploadUnSyncedPropertiesToFirebase().first()

            for (propertyEntity in unSyncedProperties) {
                val propertyId = propertyEntity.id

                if (propertyEntity.isDeleted) {
                    propertyOnlineRepository.deleteProperty(propertyId.toString())
                    propertyRepository.deleteProperty(propertyEntity)
                    results.add(SyncStatus.Success("Property $propertyId deleted"))
                } else {
                    val updatedProperty = propertyEntity.copy(updatedAt = System.currentTimeMillis())
                    val uploadedProperty = propertyOnlineRepository.uploadProperty(
                        property = updatedProperty.toOnlineEntity(),
                        propertyId = propertyId.toString()
                    )
                    propertyRepository.downloadPropertyFromFirebase(
                        uploadedProperty
                    )

                    results.add(SyncStatus.Success("Property $propertyId uploaded"))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global download sync failed", e))
        }
        return results
    }
}
