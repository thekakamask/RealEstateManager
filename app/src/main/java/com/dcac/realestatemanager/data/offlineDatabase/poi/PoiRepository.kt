package com.dcac.realestatemanager.data.offlineDatabase.poi

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.PoiWithProperties
import kotlinx.coroutines.flow.Flow

interface PoiRepository {

    // FOR UI
    fun getPoiById(id: String): Flow<Poi?>
    fun getAllPoiS(): Flow<List<Poi>>

    //SYNC
    fun uploadUnSyncedPoiSToFirebase(): Flow<List<PoiEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertPoiInsertFromUI(poi: Poi)
    suspend fun insertPoiSInsertFromUi(poiS: List<Poi>)
    //INSERTIONS FROM FIREBASE
    suspend fun insertPoiInsertFromFirebase(poi : PoiOnlineEntity, firebaseDocumentId: String)
    suspend fun insertPoiSInsertFromFirebase(poiS: List<Pair<PoiOnlineEntity, String>>)

    //UPDATE
    suspend fun updatePoiFromUI(poi: Poi)
    suspend fun updatePoiFromFirebase(poi: PoiOnlineEntity,  firebaseDocumentId: String)
    suspend fun updateAllPoiSFromFirebase(poiS: List<Pair<PoiOnlineEntity, String>>)

    //SOFT DELETE
    suspend fun markPoiAsDeleted(poi: Poi)

    //HARD DELETE
    suspend fun deletePoi(poi:PoiEntity)
    suspend fun clearAllPoiSDeleted()

    // FOR SYNC AND TEST CHECK
    fun getPoiByIdIncludeDeleted(id: String): Flow<PoiEntity?>
    fun getAllPoiIncludeDeleted(): Flow<List<PoiEntity>>

    fun getPoiWithProperties(poiId: String): Flow<PoiWithProperties>
}