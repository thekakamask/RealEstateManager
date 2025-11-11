package com.dcac.realestatemanager.data.sync.property

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyDownloadManager(
    private val propertyRepository: PropertyRepository,
    private val propertyOnlineRepository: PropertyOnlineRepository
): PropertyDownloadInterfaceManager {

   override suspend fun downloadUnSyncedProperties(): List<SyncStatus> {
       val results = mutableListOf<SyncStatus>()

       try {
           val onlineProperties = propertyOnlineRepository.getAllProperties()

           for (doc in onlineProperties){
               try {
                   val propertyOnline = doc.property
                   val localId = propertyOnline.universalLocalId
                   val localProperty = propertyRepository.getPropertyById(localId).first()

                   val shouldDownload = localProperty == null || propertyOnline.updatedAt > localProperty.updatedAt

                   if (shouldDownload) {
                       propertyRepository.insertPropertyInsertFromFirebase(propertyOnline, doc.firebaseId)
                       results.add(SyncStatus.Success("Property $localId downloaded"))
                   } else {
                       results.add(SyncStatus.Success("Property $localId already up-to-date"))
                   }
               } catch (e :Exception) {
                   results.add(SyncStatus.Failure("Property ${doc.firebaseId} failed to sync", e))
               }
           }
       } catch (e: Exception) {
           results.add(SyncStatus.Failure("Global property download failed", e))
       }
       return results
   }

}
