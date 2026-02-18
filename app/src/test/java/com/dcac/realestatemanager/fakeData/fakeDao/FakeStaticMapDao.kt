package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapDao
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeStaticMapEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakeStaticMapDao : StaticMapDao,
    BaseFakeDao<String, StaticMapEntity>({ it.id }) {

    init {
        seed(FakeStaticMapEntity.staticMapEntityList)
    }

    override fun getStaticMapById(id: String): Flow<StaticMapEntity?> =
        entityFlow.map { list ->
            list.find { it.id == id && !it.isDeleted }
        }

    override fun getStaticMapByPropertyId(propertyId: String): Flow<StaticMapEntity?> =
        entityFlow.map { list ->
            list.find { it.universalLocalPropertyId == propertyId && !it.isDeleted }
        }

    override fun getAllStaticMap(): Flow<List<StaticMapEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isDeleted }
        }

    override fun uploadUnSyncedStaticMap(): Flow<List<StaticMapEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isSynced }
        }

    override suspend fun firstStaticMapInsert(staticMap: StaticMapEntity) {
        if (!entityMap.containsKey(staticMap.id)) {
            upsert(staticMap)
        }
    }

    override suspend fun insertStaticMapIfNotExists(staticMap: StaticMapEntity) {
        if (!entityMap.containsKey(staticMap.id)) {
            upsert(staticMap)
        }
    }

    override suspend fun updateStaticMap(staticMap: StaticMapEntity) {
        upsert(staticMap)
    }

    override suspend fun markStaticMapsAsDeletedByProperty(propertyId: String, updatedAt: Long) {
        entityMap.values
            .filter { it.universalLocalPropertyId == propertyId }
            .forEach {
                upsert(
                    it.copy(
                        isDeleted = true,
                        isSynced = false,
                        updatedAt = updatedAt
                    )
                )
            }
    }

    override suspend fun markStaticMapAsDeleted(id: String, updatedAt: Long) {
        entityMap[id]?.let {
            upsert(
                it.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override suspend fun deleteStaticMap(staticMap: StaticMapEntity) {
        delete(staticMap)
    }

    override suspend fun deleteStaticMapByPropertyId(propertyId: String) {
        entityMap.values
            .filter { it.universalLocalPropertyId == propertyId }
            .toList()
            .forEach { delete(it) }
    }

    override suspend fun clearAllStaticMapsDeleted() {
        entityMap.values
            .filter { it.isDeleted }
            .toList()
            .forEach { delete(it) }
    }

    override fun getStaticMapByIdIncludeDeleted(id: String): Flow<StaticMapEntity?> =
        entityFlow.map { list ->
            list.find { it.id == id }
        }

    override fun getStaticMapByPropertyIdIncludeDeleted(propertyId: String): Flow<StaticMapEntity?> =
        entityFlow.map { list ->
            list.find { it.universalLocalPropertyId == propertyId }
        }


    override fun getAllStaticMapIncludeDeleted(): Flow<List<StaticMapEntity>> =
        entityFlow

    override fun getAllStaticMapAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("Cursor not needed for unit tests")
    }
}