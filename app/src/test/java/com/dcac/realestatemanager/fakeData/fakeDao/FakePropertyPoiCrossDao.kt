package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossDao
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePropertyPoiCrossDao : PropertyPoiCrossDao,
    BaseFakeDao<Pair<String, String>, PropertyPoiCrossEntity>({ Pair(it.universalLocalPropertyId, it.universalLocalPoiId) }) {

    init {
        seed(FakePropertyPoiCrossEntity.allCrossRefs)
    }

    override fun getCrossRefsForProperty(propertyId: String): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list ->
            list.filter { it.universalLocalPropertyId== propertyId && !it.isDeleted }
        }

    override fun getPoiIdsForProperty(propertyId: String): Flow<List<String>> =
        entityFlow.map { list ->
            list.filter { it.universalLocalPropertyId == propertyId && !it.isDeleted }.map { it.universalLocalPoiId }
        }

    override fun getPropertyIdsForPoi(poiId: String): Flow<List<String>> =
        entityFlow.map { list ->
            list.filter { it.universalLocalPoiId == poiId && !it.isDeleted }.map { it.universalLocalPropertyId }
        }

    override fun getAllCrossRefs(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list -> list.filter { !it.isDeleted } }

    override fun getCrossByIds(propertyId: String, poiId: String): Flow<PropertyPoiCrossEntity?> =
        entityFlow.map { list ->
            list.find { it.universalLocalPropertyId == propertyId && it.universalLocalPoiId == poiId && !it.isDeleted }
        }

    override fun uploadUnSyncedCrossRefs(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isSynced }
        }

    override suspend fun firstCrossRefInsert(crossRef: PropertyPoiCrossEntity) {
        upsert(crossRef)
    }

    override suspend fun insertCrossRefIfNotExists(propertyPoiCrossEntity: PropertyPoiCrossEntity) {
        val key = Pair(
            propertyPoiCrossEntity.universalLocalPropertyId,
            propertyPoiCrossEntity.universalLocalPoiId
        )
        if (!entityMap.containsKey(key)) {
            upsert(propertyPoiCrossEntity)
        }
    }

    override suspend fun updateCrossRef(crossRef: PropertyPoiCrossEntity) {
        upsert(crossRef)
    }

    override suspend fun markCrossRefAsDeleted(
        propertyId: String,
        poiId: String,
        updatedAt: Long
    ) {
        val key = Pair(propertyId, poiId)

        entityMap[key]?.let {
            upsert(
                it.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override suspend fun markCrossRefsAsDeletedForProperty(
        propertyId: String,
        updatedAt: Long
    ) {
        entityMap.values
            .filter { it.universalLocalPropertyId == propertyId }
            .toList()
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

    override suspend fun markCrossRefsAsDeletedForPoi(
        poiId: String,
        updatedAt: Long
    ) {
        entityMap.values
            .filter { it.universalLocalPoiId == poiId }
            .toList()
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

    override suspend fun markAllCrossRefsAsDeleted(updatedAt: Long) {
        entityMap.values.toList().forEach {
            upsert(
                it.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity) {
        delete(crossRef)
    }

    override suspend fun deleteCrossRefsForProperty(propertyId: String) {
        entityMap.values
            .filter { it.universalLocalPropertyId == propertyId }
            .toList()
            .forEach { delete(it) }
    }

    override suspend fun deleteCrossRefsForPoi(poiId: String) {
        entityMap.values
            .filter { it.universalLocalPoiId == poiId }
            .toList()
            .forEach { delete(it) }
    }

    override suspend fun clearAllDeleted() {
        entityMap.values
            .filter { it.isDeleted }
            .toList()
            .forEach { delete(it) }
    }

    override fun getCrossRefsByIdsIncludedDeleted(
        propertyId: String,
        poiId: String
    ): Flow<PropertyPoiCrossEntity?> =
        entityFlow.map { list ->
            list.find {
                it.universalLocalPropertyId == propertyId &&
                        it.universalLocalPoiId == poiId
            }
        }

    override fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: String)
            : Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list ->
            list.filter { it.universalLocalPropertyId == propertyId }
        }

    override fun getCrossRefsByPoiIdIncludeDeleted(poiId: String)
            : Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list ->
            list.filter { it.universalLocalPoiId == poiId }
        }

    override fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow

    override fun getAllCrossRefsAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("Cursor not needed in unit tests.")
    }
}