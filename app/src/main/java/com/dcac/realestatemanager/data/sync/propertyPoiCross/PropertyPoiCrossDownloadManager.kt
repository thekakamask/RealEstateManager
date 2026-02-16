package com.dcac.realestatemanager.data.sync.propertyPoiCross

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyPoiCrossDownloadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository,
    private val propertyRepository: PropertyRepository,
    private val poiRepository: PoiRepository
): PropertyPoiCrossDownloadInterfaceManager {

    override suspend fun downloadUnSyncedPropertyPoiCross(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineCrossRefs = propertyPoiCrossOnlineRepository.getAllCrossRefs()

            for (doc in onlineCrossRefs) {
                val online = doc.cross
                val propertyId = online.universalLocalPropertyId
                val poiId = online.universalLocalPoiId
                val firestoreId = doc.firebaseId

                val local = propertyPoiCrossRepository
                    .getCrossRefsByIdsIncludedDeleted(propertyId, poiId)
                    .first()

                if (online.isDeleted) {
                    if (local != null) {
                        propertyPoiCrossRepository.deleteCrossRef(local)
                        results.add(
                            SyncStatus.Success(
                                "CrossRef $firestoreId deleted locally (remote deleted)"
                            )
                        )
                    }
                    continue
                }

                if (local?.isDeleted == true) {
                    results.add(
                        SyncStatus.Success(
                            "CrossRef ($propertyId-$poiId) locally deleted → skip download"
                        )
                    )
                    continue
                }

                val propertyDeleted =
                    propertyRepository
                        .getPropertyByIdIncludeDeleted(propertyId)
                        .first()
                        ?.isDeleted == true

                val poiDeleted =
                    poiRepository
                        .getPoiByIdIncludeDeleted(poiId)
                        .first()
                        ?.isDeleted == true

                if (propertyDeleted || poiDeleted) {
                    results.add(
                        SyncStatus.Success("CrossRef ($propertyId-$poiId) skipped (parent deleted)")
                    )
                    continue
                }

                val shouldDownload =
                    local == null || online.updatedAt > local.updatedAt

                if (!shouldDownload) {
                    results.add(
                        SyncStatus.Success("CrossRef $firestoreId already up-to-date")
                    )
                    continue
                }

                if (local == null) {
                    propertyPoiCrossRepository.insertCrossRefInsertFromFirebase(
                        crossRef = online,
                        firebaseDocumentId = firestoreId
                    )
                    results.add(
                        SyncStatus.Success("CrossRef $firestoreId inserted")
                    )
                } else {
                    propertyPoiCrossRepository.updateCrossRefFromFirebase(
                        crossRef = online,
                        firebaseDocumentId = firestoreId
                    )
                    results.add(
                        SyncStatus.Success("CrossRef $firestoreId updated")
                    )
                }
            }

        } catch (e: Exception) {
            results.add(
                SyncStatus.Failure("Global CrossRef download failed", e)
            )
        }

        return results
    }
}