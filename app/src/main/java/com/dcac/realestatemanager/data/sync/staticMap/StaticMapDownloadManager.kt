package com.dcac.realestatemanager.data.sync.staticMap

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class StaticMapDownloadManager(
    private val staticMapRepository: StaticMapRepository,
    private val staticMapOnlineRepository: StaticMapOnlineRepository
): StaticMapDownloadInterfaceManager {


    override suspend fun downloadUnSyncedStaticMaps(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineStaticMaps = staticMapOnlineRepository.getAllStaticMaps()

            for (doc in onlineStaticMaps) {
                try {
                    val staticMapOnline = doc.staticMap
                    val localId = staticMapOnline.universalLocalId
                    val localStaticMapEntity = staticMapRepository.getStaticMapByIdIncludeDeleted(localId).first()

                    val shouldDownload =
                        localStaticMapEntity == null || staticMapOnline.updatedAt > localStaticMapEntity.updatedAt

                    if (shouldDownload) {
                        val localUri = when {
                            staticMapOnline.storageUrl.isNotEmpty() ->
                                staticMapOnlineRepository.downloadImageLocally(staticMapOnline.storageUrl)
                            localStaticMapEntity != null -> localStaticMapEntity.uri
                            else -> {
                                results.add(SyncStatus.Failure("Missing URI for staticMap $localId", Exception("Invalid state")))
                                continue
                            }
                        }

                        if (localStaticMapEntity == null) {
                            staticMapRepository.insertStaticMapInsertFromFirebase(
                                staticMap = staticMapOnline,
                                firestoreId = doc.firebaseId,
                                localUri = localUri
                            )
                            results.add(SyncStatus.Success("StaticMap $localId inserted"))
                        } else {
                            staticMapRepository.updateStaticMapFromFirebase(
                                staticMap = staticMapOnline,
                                firestoreId = doc.firebaseId
                            )
                            results.add(SyncStatus.Success("StaticMap $localId updated"))
                        }
                    } else {
                        results.add(SyncStatus.Success("StaticMap $localId already up-to-date"))
                    }
                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("StaticMap ${doc.firebaseId}", e))
                }
            }
        } catch (e: Exception) {
            results.add(SyncStatus.Failure("StaticMap download (global failure)", e))
        }

        return results
    }
}