package com.dcac.realestatemanager.data.firebaseDatabase.staticMap

interface StaticMapOnlineRepository {

    suspend fun uploadStaticMap(staticMap: StaticMapOnlineEntity, firebaseStaticMapId: String): StaticMapOnlineEntity
    suspend fun getStaticMap(firebaseStaticMapId: String): StaticMapOnlineEntity?
    suspend fun getStaticMapByPropertyId(firebasePropertyId: String): StaticMapOnlineEntity?
    suspend fun getAllStaticMaps(): List<FirestoreStaticMapDocument>
    //suspend fun deleteStaticMap(firebaseStaticMapId: String)
    //suspend fun deleteStaticMapByPropertyId(firebasePropertyId: String)
    suspend fun downloadImageLocally(storageUrl: String): String
    suspend fun markStaticMapAsDeleted(firebaseStaticMapId: String, updatedAt: Long)
}