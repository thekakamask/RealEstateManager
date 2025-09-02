package com.dcac.realestatemanager.fakeData.fakeDao

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiDao
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiWithPropertiesRelation
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * In-memory PoiDao:
 * - Owns POIs via BaseFakeDao (entityMap + entityFlow)
 * - Also keeps a local snapshot of Properties + a map of cross-refs (poiId -> propertyIds)
 * - Builds relations by combining these local flows (no dependency on other DAOs)
 */
class FakePoiDao : PoiDao,
    BaseFakeDao<Long, PoiEntity>({ it.id }) {

    // Local snapshot of properties (to build relations)
    internal val propertyStore = MutableStateFlow<List<PropertyEntity>>(emptyList())

    // Cross-refs: poiId -> set of propertyIds
    internal val poiToProperty = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())

    // Pre-fill with fake POIs
    init { seed(FakePoiEntity.poiEntityList) }

    // ---- Helpers for tests ----------------------------------------------------

    /** Seed properties snapshot used when building relations. */
    fun seedProperties(properties: List<PropertyEntity>) {
        propertyStore.value = properties
    }

    /** Link a POI to one or many properties. */
    fun linkPoiToProperties(poiId: Long, vararg propertyIds: Long) {
        val current = poiToProperty.value.toMutableMap()
        val merged = (current[poiId].orEmpty() + propertyIds.toSet()).toSet()
        current[poiId] = merged
        poiToProperty.value = current
    }

    /** Remove all links for a given poiId. */
    fun unlinkAllForPoi(poiId: Long) {
        poiToProperty.value -= poiId
        //poiToProperty.value = poiToProperty.value - poiId
    }

    // ---- PoiDao implementation -----------------------------------------------

    override fun getAllPoiS(): Flow<List<PoiEntity>> = entityFlow

    override fun getPoiById(id: Long): Flow<PoiEntity?> =
        entityFlow.map { list -> list.find { it.id == id } }

    override suspend fun insertAllPoiS(poiS: List<PoiEntity>) {
        poiS.forEach { upsert(it) }
    }

    override suspend fun insertPoi(poi: PoiEntity) {
        upsert(poi)
    }

    override suspend fun updatePoi(poi: PoiEntity) {
        upsert(poi)
    }

    override suspend fun savePoiFromFirebase(poi: PoiEntity) {
        upsert(poi)
    }

    override suspend fun deletePoi(poi: PoiEntity) {
        delete(poi)
        // Optionally clean links for this POI
        unlinkAllForPoi(poi.id)
    }

    override fun getPoiWithProperties(poiId: Long): Flow<PoiWithPropertiesRelation> =
        combine(entityFlow, propertyStore, poiToProperty) { poiS, properties, links ->
            val poi = poiS.firstOrNull { it.id == poiId }
                ?: error("POI not found for ID: $poiId")
            val ids = links[poiId].orEmpty()
            val related = properties.filter { it.id in ids }
            PoiWithPropertiesRelation(poi = poi, properties = related)
        }

    override fun getUnSyncedPoiS(): Flow<List<PoiEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }
}