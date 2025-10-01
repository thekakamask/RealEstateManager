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

/**
 * In-memory PoiDao:
 * - Owns POIs via BaseFakeDao (entityMap + entityFlow)
 * - Also keeps a local snapshot of Properties + a map of cross-refs (poiId -> propertyIds)
 * - Builds relations by combining these local flows (no dependency on other DAOs)
 */
class FakePoiDao : PoiDao,
    BaseFakeDao<Long, PoiEntity>({ it.id }) {

    private val propertyStore = MutableStateFlow<List<PropertyEntity>>(emptyList())
    private val poiToProperty = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())

    init {
        seed(FakePoiEntity.poiEntityList)
    }

    // --- Helpers (Test only) ---

    fun seedProperties(properties: List<PropertyEntity>) {
        propertyStore.value = properties
    }

    fun linkPoiToProperties(poiId: Long, vararg propertyIds: Long) {
        poiToProperty.update { current ->
            current + (poiId to ((current[poiId].orEmpty() + propertyIds.toSet()).toSet()))
        }
    }

    fun unlinkAllForPoi(poiId: Long) {
        poiToProperty.value -= poiId
    }

    // --- DAO Implementation ---

    override fun getPoiById(id: Long): Flow<PoiEntity?> =
        entityFlow.map { list -> list.find { it.id == id && !it.isDeleted } }

    override fun getAllPoiS(): Flow<List<PoiEntity>> =
        entityFlow.map { list -> list.filter { !it.isDeleted } }

    override suspend fun insertPoiForcedSyncFalse(
        id: Long,
        name: String,
        type: String,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PoiEntity(
                id = id,
                name = name,
                type = type,
                isDeleted = isDeleted,
                isSynced = false, // 🟢 forced
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun insertPoi(poi: PoiEntity) {
        insertPoiForcedSyncFalse(
            id = poi.id,
            name = poi.name,
            type = poi.type,
            isDeleted = poi.isDeleted,
            updatedAt = poi.updatedAt
        )
    }

    override suspend fun insertAllPoiS(poiS: List<PoiEntity>) {
        poiS.forEach { insertPoi(it) } // 🟢 same as real DAO
    }

    override suspend fun updatePoiForcedSyncFalse(
        id: Long,
        name: String,
        type: String,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        insertPoiForcedSyncFalse(id, name, type, isDeleted, updatedAt)
    }

    override suspend fun updatePoi(poi: PoiEntity) {
        updatePoiForcedSyncFalse(
            id = poi.id,
            name = poi.name,
            type = poi.type,
            isDeleted = poi.isDeleted,
            updatedAt = poi.updatedAt
        )
    }

    override suspend fun markPoiAsDeleted(id: Long, updatedAt: Long) {
        entityMap[id]?.let { poi ->
            val updated = poi.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            upsert(updated)
        }
    }

    override suspend fun savePoiFromFirebaseForcedSyncTrue(
        id: Long,
        name: String,
        type: String,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PoiEntity(
                id = id,
                name = name,
                type = type,
                isDeleted = isDeleted,
                isSynced = true, // 🟢 from Firebase
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun savePoiFromFirebase(poi: PoiEntity) {
        savePoiFromFirebaseForcedSyncTrue(
            id = poi.id,
            name = poi.name,
            type = poi.type,
            isDeleted = poi.isDeleted,
            updatedAt = poi.updatedAt
        )
    }

    override suspend fun deletePoi(poi: PoiEntity) {
        delete(poi)
        unlinkAllForPoi(poi.id) // 🧹 simulate foreign key behavior
    }

    override fun getPoiByIdIncludeDeleted(id: Long): Flow<PoiEntity?> =
        entityFlow.map { list -> list.find { it.id == id } }

    override fun getAllPoiIncludeDeleted(): Flow<List<PoiEntity>> =
        entityFlow

    override fun getPoiWithProperties(poiId: Long): Flow<PoiWithPropertiesRelation> =
        combine(entityFlow, propertyStore, poiToProperty) { pois, properties, links ->
            val poi = pois.firstOrNull { it.id == poiId && !it.isDeleted }
                ?: error("POI not found for ID: $poiId")
            val ids = links[poiId].orEmpty()
            val related = properties.filter { it.id in ids }
            PoiWithPropertiesRelation(poi = poi, properties = related)
        }

    override fun uploadUnSyncedPoiSToFirebase(): Flow<List<PoiEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

    override fun getAllPoiSAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("getAllPoiSAsCursor is not used in unit tests.")
    }
}