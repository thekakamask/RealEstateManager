package com.dcac.realestatemanager.fakeData.fakeDao

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossDao
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePropertyPoiCrossDao : PropertyPoiCrossDao,
    BaseFakeDao<Pair<Long, Long>, PropertyPoiCrossEntity>({ Pair(it.propertyId, it.poiId) }) {


    // pre-filling with cross
    init {
        seed(FakePropertyPoiCrossEntity.propertyPoiCrossEntityList)
    }

    override suspend fun insertCrossRef(crossRef: PropertyPoiCrossEntity) {
        upsert(crossRef)
    }

    override suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCrossEntity>) {
        crossRefs.forEach { upsert(it) }
    }

    override suspend fun updateCrossRef(propertyPoiCrossEntity: PropertyPoiCrossEntity) {
        upsert(propertyPoiCrossEntity)
    }

    override suspend fun deleteCrossRefsForProperty(propertyId: Long) {
        val toDelete = entityMap.values.filter { it.propertyId == propertyId }
        toDelete.forEach { delete(it) }
    }

    override suspend fun deleteCrossRefsForPoi(poiId: Long) {
        val toDelete = entityMap.values.filter { it.poiId == poiId }
        toDelete.forEach { delete(it) }
    }

    override fun getCrossRefsForProperty(propertyId: Long): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list -> list.filter { it.propertyId == propertyId } }

    override fun getPoiIdsForProperty(propertyId: Long): Flow<List<Long>> =
        entityFlow.map { list -> list.filter { it.propertyId == propertyId }.map { it.poiId } }

    override fun getPropertyIdsForPoi(poiId: Long): Flow<List<Long>> =
        entityFlow.map { list -> list.filter { it.poiId == poiId }.map { it.propertyId } }

    override fun getAllCrossRefs(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow

    override suspend fun clearAllCrossRefs() {
        clear()
    }

    override fun getUnSyncedPropertiesPoiSCross(): Flow<List<PropertyPoiCrossEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

    override fun getCrossByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?> =
        entityFlow.map { list -> list.find { it.propertyId == propertyId && it.poiId == poiId } }

    override suspend fun saveCrossRefFromFirebase(crossRef: PropertyPoiCrossEntity) {
        upsert(crossRef)
    }

}