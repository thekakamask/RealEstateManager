package com.dcac.realestatemanager.data.sync.property

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.toOnlineEntity
import kotlinx.coroutines.flow.first

class PropertyDownloadManager(
    private val propertyRepository: PropertyRepository,                 // Local Room repository
    private val propertyOnlineRepository: PropertyOnlineRepository      // Firestore repository
): PropertyDownloadInterfaceManager {

    // Downloads all properties from Firestore and syncs to Room
    override suspend fun downloadUnSyncedProperties(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineProperties = propertyOnlineRepository.getAllProperties()

            for (onlineProperty in onlineProperties) {
                try {
                    val roomId = onlineProperty.roomId
                    val localProperty = propertyRepository.getPropertyEntityById(roomId).first()

                    val shouldDownload = localProperty == null || onlineProperty.updatedAt > localProperty.updatedAt

                    if (shouldDownload) {
                        propertyRepository.downloadPropertyFromFirebase(onlineProperty)
                        results.add(SyncStatus.Success("Property $roomId downloaded"))
                    } else {
                        results.add(SyncStatus.Success("Property $roomId already up-to-date"))
                    }
                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("Property ${onlineProperty.roomId} failed to sync", e))
                }
            }
        } catch (e:Exception){
            results.add(SyncStatus.Failure("Global PROPERTY download failed", e))
        }
        return results

    }

}
