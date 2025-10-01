package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossDao
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePropertyPoiCrossDao : PropertyPoiCrossDao,
    BaseFakeDao<Pair<Long, Long>, PropertyPoiCrossEntity>({ Pair(it.propertyId, it.poiId) }) {

    init {
        seed(FakePropertyPoiCrossEntity.allCrossRefs)
    }

    override fun getCrossRefsForProperty(propertyId: Long): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list ->
            list.filter { it.propertyId == propertyId && !it.isDeleted }
        }

    override fun getPoiIdsForProperty(propertyId: Long): Flow<List<Long>> =
        entityFlow.map { list ->
            list.filter { it.propertyId == propertyId && !it.isDeleted }.map { it.poiId }
        }

    override fun getPropertyIdsForPoi(poiId: Long): Flow<List<Long>> =
        entityFlow.map { list ->
            list.filter { it.poiId == poiId && !it.isDeleted }.map { it.propertyId }
        }

    override fun getAllCrossRefs(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list -> list.filter { !it.isDeleted } }

    override fun getCrossByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?> =
        entityFlow.map { list ->
            list.find { it.propertyId == propertyId && it.poiId == poiId && !it.isDeleted }
        }

    override suspend fun insertCrossRefForcedSyncFalse(
        propertyId: Long,
        poiId: Long,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PropertyPoiCrossEntity(
                propertyId = propertyId,
                poiId = poiId,
                isDeleted = isDeleted,
                isSynced = false,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun insertCrossRef(crossRef: PropertyPoiCrossEntity) {
        insertCrossRefForcedSyncFalse(
            propertyId = crossRef.propertyId,
            poiId = crossRef.poiId,
            isDeleted = crossRef.isDeleted,
            updatedAt = crossRef.updatedAt
        )
    }

    override suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCrossEntity>) {
        crossRefs.forEach { insertCrossRef(it) }
    }

    override suspend fun updateCrossRefForcedSyncFalse(
        propertyId: Long,
        poiId: Long,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        insertCrossRefForcedSyncFalse(propertyId, poiId, isDeleted, updatedAt)
    }

    override suspend fun updateCrossRef(propertyPoiCrossEntity: PropertyPoiCrossEntity) {
        updateCrossRefForcedSyncFalse(
            propertyId = propertyPoiCrossEntity.propertyId,
            poiId = propertyPoiCrossEntity.poiId,
            isDeleted = propertyPoiCrossEntity.isDeleted,
            updatedAt = propertyPoiCrossEntity.updatedAt
        )
    }

    override suspend fun markCrossRefAsDeleted(propertyId: Long, poiId: Long, updatedAt: Long) {
        entityMap[Pair(propertyId, poiId)]?.let {
            val updated = it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            upsert(updated)
        }
    }

    override suspend fun markCrossRefsAsDeletedForProperty(propertyId: Long, updatedAt: Long) {
        val updated = entityMap.values.map {
            if (it.propertyId == propertyId)
                it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            else it
        }
        seed(updated)
    }

    override suspend fun markCrossRefsAsDeletedForPoi(poiId: Long, updatedAt: Long) {
        val updated = entityMap.values.map {
            if (it.poiId == poiId)
                it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            else it
        }
        seed(updated)
    }

    override suspend fun markAllCrossRefsAsDeleted(updatedAt: Long) {
        val updated = entityMap.values.map {
            it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
        }
        seed(updated)
    }


    override suspend fun clearAllDeleted() {
        val toDelete = entityMap.values.filter { it.isDeleted }
        toDelete.forEach { delete(it) }
    }

    override suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity) {
        delete(crossRef)
    }

    override suspend fun deleteCrossRefsForProperty(propertyId: Long) {
        val toDelete = entityMap.values.filter { it.propertyId == propertyId }
        toDelete.forEach { delete(it) }
    }

    override suspend fun deleteCrossRefsForPoi(poiId: Long) {
        val toDelete = entityMap.values.filter { it.poiId == poiId }
        toDelete.forEach { delete(it) }
    }

    override fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow

    override fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: Long): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list -> list.filter { it.propertyId == propertyId } }

    override fun getCrossRefsByPoiIdIncludeDeleted(poiId: Long): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list -> list.filter { it.poiId == poiId } }

    override fun getCrossRefsByIdsIncludedDeleted(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?> =
        entityFlow.map { list -> list.find { it.propertyId == propertyId && it.poiId == poiId } }

    override fun uploadUnSyncedPropertiesPoiSCross(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

    override suspend fun saveCrossRefFromFirebaseForcedSyncTrue(
        propertyId: Long,
        poiId: Long,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PropertyPoiCrossEntity(
                propertyId = propertyId,
                poiId = poiId,
                isDeleted = isDeleted,
                isSynced = true,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun saveCrossRefFromFirebase(crossRef: PropertyPoiCrossEntity) {
        saveCrossRefFromFirebaseForcedSyncTrue(
            propertyId = crossRef.propertyId,
            poiId = crossRef.poiId,
            isDeleted = crossRef.isDeleted,
            updatedAt = crossRef.updatedAt
        )
    }

    override fun getAllCrossRefsAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("getAllCrossRefsAsCursor is not used in unit tests.")
    }
}