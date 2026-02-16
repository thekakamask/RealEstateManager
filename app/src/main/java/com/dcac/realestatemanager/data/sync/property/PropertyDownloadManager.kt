package com.dcac.realestatemanager.data.sync.property

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first
import android.util.Log

class PropertyDownloadManager(
    private val propertyRepository: PropertyRepository,
    private val propertyOnlineRepository: PropertyOnlineRepository
): PropertyDownloadInterfaceManager {

    override suspend fun downloadUnSyncedProperties(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineProperties = propertyOnlineRepository.getAllProperties()

            for (doc in onlineProperties) {
                val propertyOnline = doc.property
                val localId = propertyOnline.universalLocalId
                val localProperty =
                    propertyRepository.getPropertyByIdIncludeDeleted(localId).first()

                Log.d(
                    "SYNC_DEBUG",
                    "Remote property $localId | isDeleted=${propertyOnline.isDeleted} | " +
                            "updatedAt=${propertyOnline.updatedAt} | localExists=${localProperty != null}"
                )
                if (propertyOnline.isDeleted) {
                    if (localProperty != null) {
                        propertyRepository.deleteProperty(localProperty)
                        results.add(
                            SyncStatus.Success("Property $localId deleted locally (remote deleted)")
                        )
                    }
                    continue
                }

                if (localProperty?.isDeleted == true) {
                    results.add(
                        SyncStatus.Success("Property $localId locally deleted → skip download")
                    )
                    continue
                }

                val shouldDownload =
                    localProperty == null ||
                            propertyOnline.updatedAt > localProperty.updatedAt

                if (!shouldDownload) {
                    results.add(SyncStatus.Success("Property $localId already up-to-date"))
                    continue
                }

                if (localProperty == null) {
                    propertyRepository.insertPropertyInsertFromFirebase(
                        property = propertyOnline,
                        firebaseDocumentId = doc.firebaseId
                    )
                    results.add(SyncStatus.Success("Property $localId inserted"))
                } else {
                    propertyRepository.updatePropertyFromFirebase(
                        property = propertyOnline,
                        firebaseDocumentId = doc.firebaseId
                    )
                    results.add(SyncStatus.Success("Property $localId updated"))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global property download failed", e))
        }

        return results
    }


}
