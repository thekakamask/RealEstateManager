package com.dcac.realestatemanager.fakeData.fakeDao

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiDao
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiWithPropertiesRelation
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePoiDao(
    private val fakeCrossDao: FakePropertyPoiCrossDao,
    private val fakePropertyDao: FakePropertyDao
) : PoiDao,
    BaseFakeDao<Long, PoiEntity>({ it.id }) {

    // pre-filling with photos
    init {
        seed(FakePoiEntity.poiEntityList)
    }

    override fun getAllPoiS(): Flow<List<PoiEntity>> =
        entityFlow

    override suspend fun insertAllPoiS(poiS: List<PoiEntity>) {
        poiS.forEach { upsert(it) }
    }

    override suspend fun insertPoi(poi: PoiEntity) {
        upsert(poi)
    }

    override suspend fun deletePoi(poi: PoiEntity) {
        delete(poi)
    }

    override fun getPoiWithProperties(poiId: Long): Flow<PoiWithPropertiesRelation> {
        return fakeCrossDao.getPropertyIdsForPoi(poiId).map { propertyIds ->
            val poi = entityMap[poiId]
            val properties = fakePropertyDao.entityFlow.value.filter { it.id in propertyIds }
            PoiWithPropertiesRelation(
                poi = poi ?: error("POI not found for ID: $poiId"),
                properties = properties
            )
        }
    }
}