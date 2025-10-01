package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.model.PropertyPoiCross
import kotlinx.coroutines.flow.Flow

interface PropertyPoiCrossRepository {

    // FOR UI

    fun getCrossRefsForProperty(propertyId: Long): Flow<List<PropertyPoiCross>>
    fun getAllCrossRefs(): Flow<List<PropertyPoiCross>>
    fun getPoiIdsForProperty(propertyId: Long): Flow<List<Long>>
    fun getPropertyIdsForPoi(poiId: Long): Flow<List<Long>>
    fun getCrossByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCross?>
    suspend fun insertCrossRef(crossRef: PropertyPoiCross)
    suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCross>)
    suspend fun updateCrossRef(crossRef: PropertyPoiCross)
    suspend fun markCrossRefAsDeleted(propertyId: Long, poiId: Long)
    suspend fun markCrossRefsAsDeletedForProperty(propertyId: Long)
    suspend fun markCrossRefsAsDeletedForPoi(poiId: Long)
    suspend fun markAllCrossRefsAsDeleted()

    //FOR FIREBASE SYNC

    fun getCrossEntityByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?>
    suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity)
    suspend fun deleteCrossRefsForProperty(propertyId: Long)
    suspend fun deleteCrossRefsForPoi(poiId: Long)
    suspend fun clearAllDeleted()
    fun uploadUnSyncedPropertiesPoiSCross(): Flow<List<PropertyPoiCrossEntity>>
    suspend fun downloadCrossRefFromFirebase(crossRef: PropertyPoiCrossOnlineEntity)

    //FOR TEST HARD DELETE
    fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: Long): Flow<List<PropertyPoiCrossEntity>>
    fun getCrossRefsByPoiIdIncludeDeleted(poiId: Long): Flow<List<PropertyPoiCrossEntity>>
    fun getCrossRefsByIdsIncludedDeleted(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?>
    fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>>
}