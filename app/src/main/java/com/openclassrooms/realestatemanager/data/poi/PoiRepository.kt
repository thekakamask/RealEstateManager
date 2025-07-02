package com.openclassrooms.realestatemanager.data.poi

import kotlinx.coroutines.flow.Flow

interface PoiRepository {

    fun getPoiForProperty(propertyId: Long): Flow<List<PoiEntity>>
    suspend fun insertAll(poi: List<PoiEntity>)
    suspend fun deletePoiForProperty(propertyId: Long)
    suspend fun deletePoi(poi: PoiEntity)
}