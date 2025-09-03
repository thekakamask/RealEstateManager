package com.dcac.realestatemanager.data.sync.property

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.onlineDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.first

class PropertyDownloadManager(
    private val propertyRepository: PropertyRepository,                 // Local Room repository
    private val propertyOnlineRepository: PropertyOnlineRepository      // Firestore repository
) {

    // Downloads all properties from Firestore and syncs to Room
    suspend fun downloadUnSyncedProperties(userList: List<User>): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()                      // List of success/failure results

        try {
            // Fetch all properties from Firestore, mapped with correct User references
            val onlineProperties = propertyOnlineRepository.getAllProperties(userList)

            for (property in onlineProperties) {
                try {
                    // Get local version of the property, if any
                    val localProperty = propertyRepository.getPropertyById(property.id).first()

                    if (localProperty == null) {
                        // Local property doesn't exist ➜ insert it
                        propertyRepository.cachePropertyFromFirebase(property.copy(isSynced = true))
                        Log.d("PropertyDownloadManager", "Inserted property: ${property.id}")
                        results.add(SyncStatus.Success("Property ${property.id} inserted"))

                    } else if (property.updatedAt > localProperty.updatedAt) {
                        // Firestore property is newer ➜ update local version
                        propertyRepository.updateProperty(property.copy(isSynced = true))
                        Log.d("PropertyDownloadManager", "Updated property: ${property.id}")
                        results.add(SyncStatus.Success("Property ${property.id} updated"))

                    } else {
                        // Already up-to-date ➜ no change needed
                        Log.d("PropertyDownloadManager", "Property already up-to-date: ${property.id}")
                        results.add(SyncStatus.Success("Property ${property.id} already up-to-date"))
                    }

                } catch (e: Exception) {
                    // Handle error syncing individual property
                    results.add(SyncStatus.Failure("Property ${property.id}", e))
                }
            }

        } catch (e: Exception) {
            // Handle general Firestore fetch failure
            results.add(SyncStatus.Failure("PropertyDownload (fetch failed)", e))
        }

        return results  // Return all success/failure sync results
    }
}
