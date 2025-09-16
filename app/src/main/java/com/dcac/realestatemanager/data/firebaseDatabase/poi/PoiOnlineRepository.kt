package com.dcac.realestatemanager.data.firebaseDatabase.poi

interface PoiOnlineRepository {
    suspend fun uploadPoi(poi: PoiOnlineEntity, poiId: String): PoiOnlineEntity
    suspend fun getPoi(poiId: String): PoiOnlineEntity?
    suspend fun getAllPoiS(): List<PoiOnlineEntity>
    suspend fun deletePoi(poiId: String)
}