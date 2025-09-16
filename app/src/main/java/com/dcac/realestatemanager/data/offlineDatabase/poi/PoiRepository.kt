package com.dcac.realestatemanager.data.offlineDatabase.poi

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.PoiWithProperties
import kotlinx.coroutines.flow.Flow

interface PoiRepository {

    // FOR UI

    fun getAllPoiS(): Flow<List<Poi>>
    fun getPoiById(id: Long): Flow<Poi?>
    suspend fun insertPoi(poi: Poi)
    suspend fun insertAllPoiS(poiS: List<Poi>)
    suspend fun updatePoi(poi: Poi)
    suspend fun markPoiAsDeleted(poi: Poi)
    fun getPoiWithProperties(poiId: Long): Flow<PoiWithProperties>

    //FOR FIREBASE SYNC

    fun getPoiEntityById(id: Long): Flow<PoiEntity?>
    suspend fun deletePoi(poi: PoiEntity)
    fun uploadUnSyncedPoiSToFirebase(): Flow<List<PoiEntity>>
    suspend fun downloadPoiFromFirebase(poi: PoiOnlineEntity)

}