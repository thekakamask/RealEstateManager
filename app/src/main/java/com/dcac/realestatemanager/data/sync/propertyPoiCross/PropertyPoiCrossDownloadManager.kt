package com.dcac.realestatemanager.data.sync.propertyPoiCross

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyPoiCrossDownloadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,             // Local Room repo
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository  // Firestore repo
): PropertyPoiCrossDownloadInterfaceManager {

    override suspend fun downloadUnSyncedPropertyPoiCross(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineCrossRefs = propertyPoiCrossOnlineRepository.getAllCrossRefs()

            for (doc in onlineCrossRefs) {
                try {
                    val online = doc.cross
                    val propertyId = online.universalLocalPropertyId
                    val poiId = online.universalLocalPoiId
                    val firestoreId = doc.firebaseId

                    val local =
                        propertyPoiCrossRepository.getCrossByIds(propertyId, poiId).first()

                    val shouldDownload =
                        local == null || online.updatedAt > local.updatedAt

                    if (shouldDownload) {
                        propertyPoiCrossRepository.insertCrossRefInsertFromFirebase(
                            crossRef = online,
                            firebaseDocumentId = firestoreId
                        )
                        results.add(SyncStatus.Success("CrossRef $firestoreId downloaded"))
                    } else {
                        results.add(SyncStatus.Success("CrossRef $firestoreId already up-to-date"))
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("CrossRef ${doc.firebaseId}", e))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global CrossRef download failed", e))
        }

        return results
    }
}