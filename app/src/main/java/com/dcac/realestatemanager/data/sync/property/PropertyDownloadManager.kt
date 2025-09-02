package com.dcac.realestatemanager.data.sync.property

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.onlineDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.first

class PropertyDownloadManager(
    private val propertyRepository: PropertyRepository,
    private val propertyOnlineRepository: PropertyOnlineRepository
) {

    suspend fun downloadUnSyncedProperties(userList: List<User>): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineProperties = propertyOnlineRepository.getAllProperties(userList)

            for (property in onlineProperties) {
                try {
                    val localProperty = propertyRepository.getPropertyById(property.id).first()

                    if (localProperty == null) {
                        propertyRepository.cachePropertyFromFirebase(property.copy(isSynced = true))
                        Log.d("PropertyDownloadManager", "Inserted property: ${property.id}")
                        results.add(SyncStatus.Success("Property ${property.id} inserted"))

                    } else {
                        val isSame = localProperty.title == property.title &&
                                localProperty.type == property.type &&
                                localProperty.price == property.price &&
                                localProperty.surface == property.surface &&
                                localProperty.rooms == property.rooms &&
                                localProperty.description == property.description &&
                                localProperty.address == property.address &&
                                localProperty.isSold == property.isSold &&
                                localProperty.entryDate == property.entryDate &&
                                localProperty.saleDate == property.saleDate &&
                                localProperty.staticMapPath == property.staticMapPath &&
                                localProperty.user.id == property.user.id

                        if (!isSame) {
                            propertyRepository.updateProperty(property.copy(isSynced = true))
                            Log.d("PropertyDownloadManager", "Updated property: ${property.id}")
                            results.add(SyncStatus.Success("Property ${property.id} updated"))
                        } else {
                            results.add(SyncStatus.Success("Property ${property.id} already up-to-date"))
                        }
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("Property ${property.id}", e))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("PropertyDownload (fetch failed)", e))
        }

        return results
    }
}
