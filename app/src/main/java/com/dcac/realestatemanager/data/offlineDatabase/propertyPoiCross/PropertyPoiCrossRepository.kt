package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import com.dcac.realestatemanager.model.PropertyPoiCross
import kotlinx.coroutines.flow.Flow

interface PropertyPoiCrossRepository {

    suspend fun insertCrossRef(crossRef: PropertyPoiCross)

    suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCross>)

    suspend fun deleteCrossRefsForProperty(propertyId: Long)

    suspend fun deleteCrossRefsForPoi(poiId: Long)

    suspend fun clearAllCrossRefs()

    fun getCrossRefsForProperty(propertyId: Long): Flow<List<PropertyPoiCross>>

    fun getAllCrossRefs(): Flow<List<PropertyPoiCross>>

    fun getPoiIdsForProperty(propertyId: Long): Flow<List<Long>>

    fun getPropertyIdsForPoi(poiId: Long): Flow<List<Long>>
}