package com.dcac.realestatemanager.data.firebaseDatabase.poi

interface PoiOnlineRepository {
    suspend fun uploadPoi(poi: PoiOnlineEntity, firebasePoiId: String): PoiOnlineEntity
    suspend fun getPoi(firebasePoiId: String): PoiOnlineEntity?
    suspend fun getAllPoiS(): List<FirestorePoiDocument>
    suspend fun deletePoi(firebasePoiId: String)
    suspend fun markPoiAsDeleted(firebasePoiId: String, updatedAt: Long)
}