package com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross

interface PropertyPoiCrossOnlineRepository {
    suspend fun uploadCrossRef(crossRef: PropertyPoiCrossOnlineEntity): PropertyPoiCrossOnlineEntity
    suspend fun getCrossRefsByPropertyId(firebasePropertyId: String): List<PropertyPoiCrossOnlineEntity>
    suspend fun getCrossRefsByPoiId(firebasePoiId: String): List<PropertyPoiCrossOnlineEntity>
    suspend fun deleteCrossRef(firebasePropertyId: String, firebasePoiId: String)
    suspend fun deleteAllCrossRefsForProperty(firebasePropertyId: String)
    suspend fun deleteAllCrossRefsForPoi(firebasePoiId: String)
    suspend fun getAllCrossRefs(): List<FirestoreCrossDocument>
    suspend fun markCrossRefAsDeleted(firebasePoiId: String, firebasePropertyId: String, updatedAt: Long)
}