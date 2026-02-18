package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiDao
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiWithPropertiesRelation
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePoiDao : PoiDao,
    BaseFakeDao<String, PoiEntity>({ it.id }) {

    private val propertyStore = MutableStateFlow<List<PropertyEntity>>(emptyList())
    private val poiToProperty = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    init {
        seed(FakePoiEntity.poiEntityList)
    }

    // --- Helpers (Test only) ---

    fun seedProperties(properties: List<PropertyEntity>) {
        propertyStore.value = properties
    }

    fun linkPoiToProperties(poiId: String, vararg propertyIds: String) {
        poiToProperty.update { current ->
            current + (poiId to (current[poiId].orEmpty() + propertyIds.toSet()))
        }
    }

    fun unlinkAllForPoi(poiId: String) {
        poiToProperty.value -= poiId
    }

    // --- DAO Implementation ---

    override fun getPoiById(id: String): Flow<PoiEntity?> =
        entityFlow.map { list ->
            list.find { it.id == id && !it.isDeleted }
        }

    override fun getAllPoiS(): Flow<List<PoiEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isDeleted }
        }

    override fun uploadUnSyncedPoiS(): Flow<List<PoiEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isSynced }
        }

    override suspend fun firstPoiInsert(poi: PoiEntity) {
        if (!entityMap.containsKey(poi.id)) {
            upsert(poi)
        }
    }

    override suspend fun insertPoiIfNotExists(poi: PoiEntity) {
        if (!entityMap.containsKey(poi.id)) {
            upsert(poi)
        }
    }

    override suspend fun updatePoi(poi: PoiEntity) {
        upsert(poi)
    }

    override suspend fun markPoiAsDeleted(id: String, updatedAt: Long) {
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

    override suspend fun clearAllPoiSDeleted() {
        entityMap.values
            .filter { it.isDeleted }
            .toList()
            .forEach { delete(it) }
    }

    override suspend fun deletePoi(poi: PoiEntity) {
        delete(poi)
        unlinkAllForPoi(poi.id)
    }

    override fun getPoiByIdIncludeDeleted(id: String): Flow<PoiEntity?> =
        entityFlow.map { list ->
            list.find { it.id == id }
        }

    override fun getAllPoiSIncludeDeleted(): Flow<List<PoiEntity>> =
        entityFlow

    override suspend fun findExistingPoi(
        name: String,
        address: String
    ): PoiEntity? {
        return entityMap.values.firstOrNull {
            !it.isDeleted &&
                    it.name.lowercase() == name.lowercase() &&
                    it.address.lowercase() == address.lowercase()
        }
    }

    override fun getPoiWithProperties(poiId: String): Flow<PoiWithPropertiesRelation> =
        combine(entityFlow, propertyStore, poiToProperty) { pois, properties, links ->

            val poi = pois.firstOrNull { it.id == poiId && !it.isDeleted }
                ?: error("POI not found for ID: $poiId")

            val propertyIds = links[poiId].orEmpty()

            val relatedProperties = properties.filter {
                it.id in propertyIds && !it.isDeleted
            }

            PoiWithPropertiesRelation(
                poi = poi,
                properties = relatedProperties
            )
        }

    override fun getAllPoiSAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("Cursor not needed for unit tests.")
    }
}