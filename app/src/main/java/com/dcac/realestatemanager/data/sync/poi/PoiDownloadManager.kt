package com.dcac.realestatemanager.data.sync.poi

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.onlineDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PoiDownloadManager(
    private val poiRepository: PoiRepository,
    private val poiOnlineRepository: PoiOnlineRepository
) {

    suspend fun downloadUnSyncedPoiS(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()

        try {
            val onlinePoiS = poiOnlineRepository.getAllPoiS()

            for (poi in onlinePoiS) {
                try {
                    val localPoi = poiRepository.getPoiById(poi.id).first()

                    if (localPoi == null) {
                        // 🔽 New POI from Firestore
                        poiRepository.cachePoiFromFirebase(poi.copy(isSynced = true))
                        Log.d("PoiDownloadManager", "Inserted poi: ${poi.name}")
                        results.add(SyncStatus.Success("Poi ${poi.name} inserted"))

                    } else {
                        val isSame = localPoi.name == poi.name &&
                                localPoi.type == poi.type

                        if (!isSame) {
                            // 🔁 Update local POI
                            poiRepository.updatePoi(poi.copy(isSynced = true))
                            Log.d("PoiDownloadManager", "Updated poi: ${poi.name}")
                            results.add(SyncStatus.Success("Poi ${poi.name} updated"))
                        } else {
                            // ✅ Already up-to-date
                            Log.d("PoiDownloadManager", "Poi already up-to-date: ${poi.name}")
                            results.add(SyncStatus.Success("Poi ${poi.name} already up-to-date"))
                        }
                    }

                } catch (e: Exception) {
                    results.add(SyncStatus.Failure("Poi ${poi.name}", e))
                }
            }

        } catch (e: Exception) {
            results.add(SyncStatus.Failure("PoiDownload (fetch failed)", e))
        }

        return results
    }
}