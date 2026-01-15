package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.model.PropertyPoiCross
import kotlinx.coroutines.flow.Flow

interface PropertyPoiCrossRepository {

    // FOR UI
    fun getCrossRefsForProperty(propertyId: String): Flow<List<PropertyPoiCross>>
    fun getPoiIdsForProperty(propertyId: String): Flow<List<String>>
    fun getPropertyIdsForPoi(poiId: String): Flow<List<String>>
    fun getAllCrossRefs(): Flow<List<PropertyPoiCross>>
    fun getCrossByIds(propertyId: String, poiId: String): Flow<PropertyPoiCross?>

    //SYNC
    fun uploadUnSyncedCrossRefsToFirebase(): Flow<List<PropertyPoiCrossEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertCrossRefInsertFromUI(crossRef: PropertyPoiCross)
    suspend fun insertAllCrossRefsInsertFromUI(crossRefs: List<PropertyPoiCross>)
    //INSERTIONS FROM FIREBASE
    suspend fun insertCrossRefInsertFromFirebase(crossRef: PropertyPoiCrossOnlineEntity, firebaseDocumentId: String)
    suspend fun insertAllCrossRefInsertFromFirebase(crossRefs: List<Pair<PropertyPoiCrossOnlineEntity, String>>)

    //UPDATE
    suspend fun updateCrossRefFromUI(crossRef: PropertyPoiCross)
    suspend fun updateAllCrossRefsFromUI(crossRefs: List<PropertyPoiCross>)
    suspend fun updateCrossRefFromFirebase(crossRef: PropertyPoiCrossOnlineEntity, firebaseDocumentId: String)
    suspend fun updateAllCrossRefFromFirebase(crossRefs: List<Pair<PropertyPoiCrossOnlineEntity, String>>)

    //SOFT DELETE
    suspend fun markCrossRefAsDeleted(propertyId: String, poiId: String)
    suspend fun markCrossRefsAsDeletedForProperty(propertyId: String)
    suspend fun markCrossRefsAsDeletedForPoi(poiId: String)
    suspend fun markAllCrossRefsAsDeleted()

    //HARD DELETE
    suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity)
    suspend fun deleteCrossRefsForProperty(propertyId: String)
    suspend fun deleteCrossRefsForPoi(poiId: String)
    suspend fun clearAllDeleted()

    // FOR SYNC AND TEST CHECK
    fun getCrossRefsByIdsIncludedDeleted(propertyId: String, poiId: String): Flow<PropertyPoiCrossEntity?>
    fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: String): Flow<List<PropertyPoiCrossEntity>>
    fun getCrossRefsByPoiIdIncludeDeleted(poiId: String): Flow<List<PropertyPoiCrossEntity>>
    fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>>
}