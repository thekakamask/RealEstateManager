package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.model.StaticMap
import com.dcac.realestatemanager.utils.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.first

class StaticMapLocalDataSource(
    private val dao: StaticMapDao
) {

    fun getStaticMapById(id: String): Flow<StaticMap?> =
        dao.getStaticMapById(id).map { it?.toModel() }


    fun getStaticMapByPropertyId(propertyId: String): Flow<StaticMap?> =
        dao.getStaticMapByPropertyId(propertyId).map { it?.toModel() }


    fun getAllStaticMap(): Flow<List<StaticMap>> =
        dao.getAllStaticMap().map { list -> list.map { it.toModel() } }


    fun uploadUnSyncedStaticMapToFirebase(): Flow<List<StaticMapEntity>> =
        dao.uploadUnSyncedStaticMap()

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertStaticMapInsertFromUI(staticMap: StaticMap): String =
        dao.insertStaticMapInsertFromUI(staticMap.toEntity())

    //INSERTIONS FROM FIREBASE
    suspend fun insertStaticMapInsertFromFirebase(
        staticMap: StaticMapOnlineEntity,
        firestoreId: String,
        localUri: String
    ) {
        val entity = staticMap.toEntity(firestoreId = firestoreId).copy(uri = localUri)
        dao.insertStaticMapInsertFromFirebase(entity)
    }

    //UPDATE
    suspend fun updateStaticMapFromUI(staticMap: StaticMap) {
        dao.updateStaticMapFromUIForceSyncFalse(staticMap.toEntity())
    }

    suspend fun updateStaticMapFromFirebase(
        staticMap: StaticMapOnlineEntity,
        firestoreId: String
    ) {
        val existing = dao
            .getStaticMapByIdIncludeDeleted(staticMap.universalLocalId)
            .first()

        val preservedUri = existing?.uri ?: ""

        dao.updateStateMapFromFirebaseForceSyncTrue(
            staticMap.toEntity(firestoreId = firestoreId)
                .copy(uri = preservedUri)
        )
    }

    //SOFT DELETE
    suspend fun markStaticMapAsDeleted(staticMap: StaticMap) {
        dao.markStaticMapAsDeleted(staticMap.universalLocalId, System.currentTimeMillis())
    }

    suspend fun markStaticMapAsDeletedByProperty(propertyId: String) {
        dao.markStaticMapsAsDeletedByProperty(propertyId, System.currentTimeMillis())
    }

    //HARD DELETE
    suspend fun deleteStaticMapByPropertyId(propertyId: String) {
        dao.deleteStaticMapByPropertyId(propertyId)
    }

    suspend fun deleteStaticMap(staticMap: StaticMapEntity) {
        dao.deleteStaticMap(staticMap)
    }

    suspend fun clearAllStaticMapsDeleted() {
        dao.clearAllStaticMapsDeleted()
    }

    // FOR SYNC AND TEST CHECK
    fun getStaticMapByIdIncludeDeleted(id: String): Flow<StaticMapEntity?> =
        dao.getStaticMapByIdIncludeDeleted(id)

    fun getStaticMapByPropertyIdIncludeDeleted(propertyId: String): Flow<StaticMapEntity?> =
        dao.getStaticMapByPropertyIdIncludeDeleted(propertyId)

    fun getAllStaticMapIncludeDeleted(): Flow<List<StaticMapEntity>> =
        dao.getAllStaticMapIncludeDeleted()
}