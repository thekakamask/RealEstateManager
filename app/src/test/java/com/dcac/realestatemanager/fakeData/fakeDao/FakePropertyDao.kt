package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyDao
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyWithPoiSRelation
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * In-memory PropertyDao:
 * - Owns Properties via BaseFakeDao
 * - Also keeps a local snapshot of POIs + a map of cross-refs (propertyId -> poiIds)
 * - Builds relations locally (no dependency on other DAOs)
 */
class FakePropertyDao : PropertyDao,
    BaseFakeDao<Long, PropertyEntity>({ it.id }) {

    private val poiStore = MutableStateFlow<List<PoiEntity>>(emptyList())
    private val propertyToPoi = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())

    init {
        seed(FakePropertyEntity.propertyEntityList)
    }

    // --- Helpers for testing ---

    fun seedPois(pois: List<PoiEntity>) {
        poiStore.value = pois
    }

    fun linkPropertyToPois(propertyId: Long, vararg poiIds: Long) {
        val current = propertyToPoi.value.toMutableMap()
        val merged = (current[propertyId].orEmpty() + poiIds.toSet()).toSet()
        current[propertyId] = merged
        propertyToPoi.value = current
    }

    fun unlinkAllForProperty(propertyId: Long) {
        propertyToPoi.value -= propertyId
    }


    override fun getAllPropertiesByDate(): Flow<List<PropertyEntity>> =
        entityFlow.map { it.filter { p -> !p.isDeleted }.sortedBy { it.entryDate }}

    override fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>> =
        entityFlow.map { it.filter { p -> !p.isDeleted }.sortedBy { it.title } }

    override fun getPropertyById(id: Long): Flow<PropertyEntity?> =
        entityFlow.map { it.find { p -> p.id == id && !p.isDeleted } }

    override fun getPropertyByUserId(userId: Long): Flow<List<PropertyEntity>> =
        entityFlow.map { it.filter { p -> p.userId == userId && !p.isDeleted } }

    override fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>> =
        entityFlow.map { list ->
            list.filter {
                !it.isDeleted &&
                        (minSurface == null || it.surface >= minSurface) &&
                        (maxSurface == null || it.surface <= maxSurface) &&
                        (minPrice == null || it.price >= minPrice) &&
                        (maxPrice == null || it.price <= maxPrice) &&
                        (type == null || it.type == type) &&
                        (isSold == null || it.isSold == isSold)
            }
        }

    override suspend fun insertPropertyForcedSyncFalse(
        id: Long,
        title: String,
        type: String,
        price: Int,
        surface: Int,
        rooms: Int,
        description: String,
        address: String,
        isSold: Boolean,
        entryDate: String,
        saleDate: String?,
        userId: Long,
        staticMapPath: String?,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PropertyEntity(
                id = id,
                title = title,
                type = type,
                price = price,
                surface = surface,
                rooms = rooms,
                description = description,
                address = address,
                isSold = isSold,
                entryDate = entryDate,
                saleDate = saleDate,
                userId = userId,
                staticMapPath = staticMapPath,
                isDeleted = isDeleted,
                isSynced = false,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun insertProperty(property: PropertyEntity): Long {
        insertPropertyForcedSyncFalse(
            id = property.id,
            title = property.title,
            type = property.type,
            price = property.price,
            surface = property.surface,
            rooms = property.rooms,
            description = property.description,
            address = property.address,
            isSold = property.isSold,
            entryDate = property.entryDate,
            saleDate = property.saleDate,
            userId = property.userId,
            staticMapPath = property.staticMapPath,
            isDeleted = property.isDeleted,
            updatedAt = property.updatedAt
        )
        return property.id
    }

    override suspend fun updatePropertyForcedSyncFalse(
        id: Long,
        title: String,
        type: String,
        price: Int,
        surface: Int,
        rooms: Int,
        description: String,
        address: String,
        isSold: Boolean,
        entryDate: String,
        saleDate: String?,
        userId: Long,
        staticMapPath: String?,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        insertPropertyForcedSyncFalse(
            id, title, type, price, surface, rooms, description, address,
            isSold, entryDate, saleDate, userId, staticMapPath, isDeleted, updatedAt
        )
    }

    override suspend fun updateProperty(property: PropertyEntity) {
        updatePropertyForcedSyncFalse(
            id = property.id,
            title = property.title,
            type = property.type,
            price = property.price,
            surface = property.surface,
            rooms = property.rooms,
            description = property.description,
            address = property.address,
            isSold = property.isSold,
            entryDate = property.entryDate,
            saleDate = property.saleDate,
            userId = property.userId,
            staticMapPath = property.staticMapPath,
            isDeleted = property.isDeleted,
            updatedAt = property.updatedAt
        )
    }

    override suspend fun savePropertyFromFirebaseForcedSyncTrue(
        id: Long,
        title: String,
        type: String,
        price: Int,
        surface: Int,
        rooms: Int,
        description: String,
        address: String,
        isSold: Boolean,
        entryDate: String,
        saleDate: String?,
        userId: Long,
        staticMapPath: String?,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PropertyEntity(
                id = id,
                title = title,
                type = type,
                price = price,
                surface = surface,
                rooms = rooms,
                description = description,
                address = address,
                isSold = isSold,
                entryDate = entryDate,
                saleDate = saleDate,
                userId = userId,
                staticMapPath = staticMapPath,
                isDeleted = isDeleted,
                isSynced = true,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun savePropertyFromFirebase(property: PropertyEntity) {
        savePropertyFromFirebaseForcedSyncTrue(
            id = property.id,
            title = property.title,
            type = property.type,
            price = property.price,
            surface = property.surface,
            rooms = property.rooms,
            description = property.description,
            address = property.address,
            isSold = property.isSold,
            entryDate = property.entryDate,
            saleDate = property.saleDate,
            userId = property.userId,
            staticMapPath = property.staticMapPath,
            isDeleted = property.isDeleted,
            updatedAt = property.updatedAt
        )
    }

    override suspend fun deleteProperty(property: PropertyEntity) {
        delete(property)
        unlinkAllForProperty(property.id)
    }

    override suspend fun markPropertyAsDeleted(id: Long, updatedAt: Long) {
        entityMap[id]?.let {
            val updated = it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            upsert(updated)
        }
    }

    override suspend fun clearAllDeleted() {
        val toDelete = entityMap.values.filter { it.isDeleted }
        toDelete.forEach { delete(it) }
    }

    override suspend fun markAllPropertiesAsDeleted(updatedAt: Long) {
        val updated = entityMap.values.map {
            it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
        }
        seed(updated)
    }

    override fun getPropertyByIdIncludeDeleted(id: Long): Flow<PropertyEntity?> =
        entityFlow.map { it.find { p -> p.id == id } }

    override fun getAllPropertiesIncludeDeleted(): Flow<List<PropertyEntity>> =
        entityFlow

    override fun getPropertyWithPoiS(propertyId: Long): Flow<PropertyWithPoiSRelation?> =
        combine(entityFlow, poiStore, propertyToPoi) { properties, poiS, links ->
            val property = properties.firstOrNull { it.id == propertyId && !it.isDeleted }
                ?: return@combine null  // 

            val related = poiS.filter { it.id in links[propertyId].orEmpty() }
            PropertyWithPoiSRelation(property = property, poiS = related)
        }


    override fun uploadUnSyncedPropertiesToFirebase(): Flow<List<PropertyEntity>> =
        entityFlow.map { it.filter { p -> !p.isSynced } }

    override fun getAllPropertiesAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("getAllPropertiesAsCursor is not used in unit tests.")
    }
}