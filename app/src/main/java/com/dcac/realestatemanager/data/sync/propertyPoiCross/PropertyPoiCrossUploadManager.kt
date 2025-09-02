package com.dcac.realestatemanager.data.sync.propertyPoiCross

import android.util.Log
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import kotlinx.coroutines.flow.first

class PropertyPoiCrossUploadManager(
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository,
    private val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository
) {

    suspend fun syncUnSyncedPropertyPoiCross() : List<SyncStatus> {
        val unSyncedPropertyPoiCross = propertyPoiCrossRepository.getUnSyncedPropertiesPoiSCross().first()
        val results = mutableListOf<SyncStatus>()

        for (propertyPoiCross in unSyncedPropertyPoiCross) {
            try {
                val syncedPropertyPoiCross = propertyPoiCrossOnlineRepository.uploadCrossRef(propertyPoiCross)

                propertyPoiCrossRepository.updateCrossRef(syncedPropertyPoiCross)
                Log.d("CrossSyncManager", "Synced cross: ${propertyPoiCross.propertyId}")
                results.add(SyncStatus.Success("Cross ${propertyPoiCross.propertyId}"))
            } catch (e: Exception) {
                results.add(SyncStatus.Failure("Cross ${propertyPoiCross.propertyId}", e))

            }
        }
        return results
    }
}