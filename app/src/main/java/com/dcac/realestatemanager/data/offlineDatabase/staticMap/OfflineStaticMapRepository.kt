package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import android.content.Context
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.model.StaticMap
import kotlinx.coroutines.flow.Flow

class OfflineStaticMapRepository(
    private val remote: StaticMapRemoteDataSource,
    private val local: StaticMapLocalDataSource
): StaticMapRepository {

    override suspend fun getStaticMapImage(config: StaticMapConfig): ByteArray? {
        return remote.getStaticMapImage(config)
    }

    override fun saveStaticMapToLocal(
        context: Context,
        fileName: String,
        bytes: ByteArray
    ): String? {
        return remote.saveStaticMapToLocal(context, fileName, bytes)
    }

    override fun getStaticMapById(id: String): Flow<StaticMap?> {
        return local.getStaticMapById(id)
    }

    override fun getStaticMapByPropertyId(propertyId: String): Flow<StaticMap?> {
        return local.getStaticMapByPropertyId(propertyId)
    }

    override fun getAllStaticMap(): Flow<List<StaticMap>> {
        return local.getAllStaticMap()
    }

    override fun uploadUnSyncedStaticMapToFirebase(): Flow<List<StaticMapEntity>> {
        return local.uploadUnSyncedStaticMapToFirebase()
    }

    override suspend fun insertStaticMapInsertFromUI(staticMap: StaticMap): String {
        return local.insertStaticMapInsertFromUI(staticMap)
    }

    override suspend fun insertStaticMapInsertFromFirebase(
        staticMap: StaticMapOnlineEntity,
        firestoreId: String,
        localUri: String
    ) {
        local.insertStaticMapInsertFromFirebase(staticMap, firestoreId, localUri)
    }

    override suspend fun updateStaticMapFromUI(staticMap: StaticMap) {
        local.updateStaticMapFromUI(staticMap)
    }

    override suspend fun updateStaticMapFromFirebase(
        staticMap: StaticMapOnlineEntity,
        firestoreId: String
    ) {
        local.updateStaticMapFromFirebase(staticMap, firestoreId)
    }

    override suspend fun markStaticMapAsDeleted(staticMap: StaticMap) {
        local.markStaticMapAsDeleted(staticMap)
    }

    override suspend fun markStaticMapAsDeletedByProperty(propertyId: String) {
        local.markStaticMapAsDeletedByProperty(propertyId)
    }

    override suspend fun deleteStaticMapByPropertyId(propertyId: String) {
        local.deleteStaticMapByPropertyId(propertyId)
    }

    override suspend fun deleteStaticMap(staticMap: StaticMapEntity) {
        local.deleteStaticMap(staticMap)
    }

    override suspend fun clearAllStaticMapsDeleted() {
        local.clearAllStaticMapsDeleted()
    }

    override fun getStaticMapByIdIncludeDeleted(id: String): Flow<StaticMapEntity?> {
        return local.getStaticMapByIdIncludeDeleted(id)
    }

    override fun getStaticMapByPropertyIdIncludeDeleted(propertyId: String): Flow<StaticMapEntity?> {
        return local.getStaticMapByPropertyIdIncludeDeleted(propertyId)
    }

    override fun getAllStaticMapIncludeDeleted(): Flow<List<StaticMapEntity>> {
        return local.getAllStaticMapIncludeDeleted()
    }
}