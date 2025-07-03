package com.dcac.realestatemanager.data.offlinedatabase.poi

import kotlinx.coroutines.flow.Flow

interface PoiRepository {

    fun getPoiForProperty(propertyId: Long): Flow<List<PoiEntity>>
    suspend fun insertAll(poi: List<PoiEntity>)
    suspend fun deletePoiForProperty(propertyId: Long)
    suspend fun deletePoi(poi: PoiEntity)
    fun getAllPoi(): Flow<List<PoiEntity>>
}