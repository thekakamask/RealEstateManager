package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.model.StaticMap
import kotlinx.coroutines.flow.Flow

interface StaticMapRepository {

    // FOR UI
    fun getStaticMapById(id: String): Flow<StaticMap?>
    fun getStaticMapByPropertyId(propertyId: String): Flow<StaticMap?>
    fun getAllStaticMap(): Flow<List<StaticMap>>

    //SYNC
    fun uploadUnSyncedStaticMapToFirebase(): Flow<List<StaticMapEntity>>

    //INSERTIONS FROM UI
    suspend fun insertStaticMapInsertFromUI(staticMap: StaticMap): String

    //INSERTIONS FROM FIREBASE
    suspend fun insertStaticMapInsertFromFirebase(staticMap: StaticMapOnlineEntity, firestoreId: String, localUri: String)

    //UPDATE
    suspend fun updateStaticMapFromUI(staticMap: StaticMap)
    suspend fun updateStaticMapFromFirebase(staticMap: StaticMapOnlineEntity, firestoreId: String)

    //SOFT DELETE
    suspend fun markStaticMapAsDeleted(staticMap: StaticMap)
    suspend fun markStaticMapAsDeletedByProperty(propertyId: String)

    //HARD DELETE
    suspend fun deleteStaticMapByPropertyId(propertyId: String)
    suspend fun deleteStaticMap(staticMap: StaticMapEntity)
    suspend fun clearAllStaticMapsDeleted()

    //FOR TEST HARD DELETE CHECK
    fun getStaticMapByIdIncludeDeleted(id: String): Flow<StaticMapEntity?>
    fun getStaticMapByPropertyIdIncludeDeleted(propertyId: String): Flow<StaticMapEntity?>
    fun getAllStaticMapIncludeDeleted(): Flow<List<StaticMapEntity>>

}