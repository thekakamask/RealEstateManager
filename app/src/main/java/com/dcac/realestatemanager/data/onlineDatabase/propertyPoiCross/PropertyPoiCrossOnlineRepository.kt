package com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross

import com.dcac.realestatemanager.model.PropertyPoiCross

interface PropertyPoiCrossOnlineRepository {

    suspend fun uploadCrossRef(crossRef: PropertyPoiCross): PropertyPoiCross
    suspend fun getCrossRefsByPropertyId(propertyId: Long): List<PropertyPoiCross>
    suspend fun deleteCrossRef(propertyId: Long, poiId: Long)
    suspend fun deleteAllCrossRefsForProperty(propertyId: Long)
    suspend fun deleteAllCrossRefsForPoi(poiId: Long)
}