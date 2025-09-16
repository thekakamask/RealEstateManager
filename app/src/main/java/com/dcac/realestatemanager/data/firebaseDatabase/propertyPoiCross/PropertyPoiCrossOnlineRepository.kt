package com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross

interface PropertyPoiCrossOnlineRepository {
    suspend fun uploadCrossRef(crossRef: PropertyPoiCrossOnlineEntity): PropertyPoiCrossOnlineEntity
    suspend fun getCrossRefsByPropertyId(propertyId: Long): List<PropertyPoiCrossOnlineEntity>
    suspend fun getCrossRefsByPoiId(poiId: Long): List<PropertyPoiCrossOnlineEntity>
    suspend fun deleteCrossRef(propertyId: Long, poiId: Long)
    suspend fun deleteAllCrossRefsForProperty(propertyId: Long)
    suspend fun deleteAllCrossRefsForPoi(poiId: Long)
    suspend fun getAllCrossRefs(): List<PropertyPoiCrossOnlineEntity>
}