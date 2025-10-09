package com.dcac.realestatemanager.data.sync.propertyPoiCross

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyPoiCrossDownloadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,             // Local Room repo
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository  // Firestore repo
): PropertyPoiCrossDownloadInterfaceManager {

    // Downloads all cross-references (Property ↔ POI) from Firestore to Room
    override suspend fun downloadUnSyncedPropertyPoiCross(): List<SyncStatus> {

        val results = mutableListOf<SyncStatus>()

        try {
            val onlineCrossRefs = propertyPoiCrossOnlineRepository.getAllCrossRefs()

            for (onlineCrossRef in onlineCrossRefs) {
                try {
                    val propertyId = onlineCrossRef.propertyId
                    val poiId = onlineCrossRef.poiId

                    val localCrossRef = propertyPoiCrossRepository.getCrossEntityByIds(propertyId, poiId).first()

                    val shouldDownload = localCrossRef == null || onlineCrossRef.updatedAt > localCrossRef.updatedAt

                    if (shouldDownload) {
                        propertyPoiCrossRepository.downloadCrossRefFromFirebase(onlineCrossRef)
                        results.add(SyncStatus.Success("CrossRef ($propertyId, $poiId) downloaded"))
                    } else {
                        results.add(SyncStatus.Success("CrossRef ($propertyId, $poiId) already up-to-date"))
                    }
                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("CrossRef (${onlineCrossRef.propertyId}, ${onlineCrossRef.poiId}) failed to sync", e))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("Global cross-reference download failed", e))

        }
        return results
    }
}
