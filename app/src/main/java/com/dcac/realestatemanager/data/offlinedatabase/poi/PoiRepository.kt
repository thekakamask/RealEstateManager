package com.dcac.realestatemanager.data.offlinedatabase.poi

import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.PoiWithProperties
import kotlinx.coroutines.flow.Flow

interface PoiRepository {

    fun getAllPoiS(): Flow<List<Poi>>

    suspend fun insertPoi(poi: Poi)
    suspend fun insertAllPoiS(poiS: List<Poi>)
    suspend fun deletePoi(poi: Poi)
    fun getPoiWithProperties(poiId: Long): Flow<PoiWithProperties>
}