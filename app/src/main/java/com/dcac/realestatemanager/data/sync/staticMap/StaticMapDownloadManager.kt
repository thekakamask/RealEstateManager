package com.dcac.realestatemanager.data.sync.staticMap

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first
import java.io.File

class StaticMapDownloadManager(
    private val staticMapRepository: StaticMapRepository,
    private val staticMapOnlineRepository: StaticMapOnlineRepository
): StaticMapDownloadInterfaceManager {


    override suspend fun downloadUnSyncedStaticMaps(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlineStaticMaps =
                staticMapOnlineRepository.getAllStaticMaps()

            for (doc in onlineStaticMaps) {
                val online = doc.staticMap
                val localId = online.universalLocalId

                val local =
                    staticMapRepository
                        .getStaticMapByIdIncludeDeleted(localId)
                        .first()

                if (online.isDeleted) {
                    if (local != null) {
                        staticMapRepository.deleteStaticMap(local)
                        results.add(
                            SyncStatus.Success(
                                "StaticMap $localId deleted locally (remote deleted)"
                            )
                        )
                    }
                    continue
                }

                val shouldDownload =
                    local == null || online.updatedAt > local.updatedAt

                if (!shouldDownload) {
                    results.add(
                        SyncStatus.Success("StaticMap $localId already up-to-date")
                    )
                    continue
                }

                val localUri = when {
                    local?.uri?.isNotBlank() == true &&
                            File(local.uri).exists() -> local.uri

                    online.storageUrl.isNotEmpty() ->
                        staticMapOnlineRepository
                            .downloadImageLocally(online.storageUrl)

                    else -> {
                        results.add(
                            SyncStatus.Failure(
                                "Missing URI for staticMap $localId",
                                Exception("No local file and no storageUrl")
                            )
                        )
                        continue
                    }
                }

                if (local == null) {
                    staticMapRepository.insertStaticMapInsertFromFirebase(
                        staticMap = online,
                        firestoreId = doc.firebaseId,
                        localUri = localUri
                    )
                    results.add(
                        SyncStatus.Success("StaticMap $localId inserted")
                    )
                } else {
                    staticMapRepository.updateStaticMapFromFirebase(
                        staticMap = online,
                        firestoreId = doc.firebaseId
                    )
                    results.add(
                        SyncStatus.Success("StaticMap $localId updated")
                    )
                }
            }

        } catch (e: Exception) {
            results.add(
                SyncStatus.Failure("StaticMap download (global failure)", e)
            )
        }

        return results
    }
}