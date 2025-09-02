package com.dcac.realestatemanager.data.sync.poi

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.onlineDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PoiUploadManager(
    private val poiRepository: PoiRepository,
    private val poiOnlineRepository: PoiOnlineRepository
) {

    suspend fun syncUnSyncedPoiS() : List<SyncStatus> {

        val unSyncedPoiS = poiRepository.getUnSyncedPoiS().first()

        val results = mutableListOf<SyncStatus>()

        for (poi in unSyncedPoiS) {
            try {
                val syncedPoi = poiOnlineRepository.uploadPoi(poi, poi.id.toString())
                poiRepository.updatePoi(syncedPoi)
                Log.d("PoiSyncManager", "Synced poi: ${poi.name}")

                results.add(SyncStatus.Success("Poi ${poi.id}"))
            } catch (e: Exception) {
                results.add(SyncStatus.Failure("Poi ${poi.id}", e))
            }
        }

        return results
    }
}