package com.dcac.realestatemanager.data.offlinedatabase.poi

import com.dcac.realestatemanager.model.Poi
import kotlinx.coroutines.flow.Flow

interface PoiRepository {

    fun getPoiSByPropertyId(propertyId: Long): Flow<List<Poi>>
    fun getAllPoiS(): Flow<List<Poi>>

    suspend fun insertPoi(poi: PoiEntity)
    suspend fun insertAllPoiS(poiS: List<PoiEntity>)
    suspend fun deletePoiSForProperty(propertyId: Long)
    suspend fun deletePoi(poi: PoiEntity)
}