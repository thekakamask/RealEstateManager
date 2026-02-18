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
    BaseFakeDao<String, PropertyEntity>({ it.id }) {

    private val poiStore = MutableStateFlow<List<PoiEntity>>(emptyList())
    private val propertyToPoi = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    init {
        seed(FakePropertyEntity.propertyEntityList)
    }

    // --- Helpers for testing ---

    fun seedPois(pois: List<PoiEntity>) {
        poiStore.value = pois
    }

    fun linkPropertyToPois(propertyId: String, vararg poiIds: String) {
        val current = propertyToPoi.value.toMutableMap()
        val merged = (current[propertyId].orEmpty() + poiIds.toSet()).toSet()
        current[propertyId] = merged
        propertyToPoi.value = current
    }

    fun unlinkAllForProperty(propertyId: String) {
        propertyToPoi.value -= propertyId
    }

    override fun getAllPropertiesByDate(): Flow<List<PropertyEntity>> =
        entityFlow.map {
            it.filter { p -> !p.isDeleted }
                .sortedByDescending { p -> p.entryDate }
        }

    override fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>> =
        entityFlow.map {
            it.filter { p -> !p.isDeleted }
                .sortedBy { p -> p.title }
        }

    override fun getPropertyById(id: String): Flow<PropertyEntity?> =
        entityFlow.map {
            it.firstOrNull { p -> p.id == id && !p.isDeleted }
        }

    override fun getPropertyByUserIdAlphabetic(userId: String): Flow<List<PropertyEntity>> =
        entityFlow.map {
            it.filter { p -> p.universalLocalUserId == userId && !p.isDeleted }
                .sortedBy { p -> p.title }
        }

    override fun getPropertyByUserIdDate(userId: String): Flow<List<PropertyEntity>> =
        entityFlow.map {
            it.filter { p -> p.universalLocalUserId == userId && !p.isDeleted }
                .sortedByDescending { p -> p.entryDate }
        }

    override fun searchPropertiesByDate(
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
            }.sortedByDescending { it.entryDate }
        }

    override fun searchPropertiesByAlphabetic(
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
            }.sortedBy { it.title }
        }

    override fun searchUserPropertiesByAlphabetic(
        userId: String,
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>> =
        searchPropertiesByAlphabetic(minSurface, maxSurface, minPrice, maxPrice, type, isSold)
            .map { list -> list.filter { it.universalLocalUserId == userId } }

    override fun searchUserPropertiesByDate(
        userId: String,
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>> =
        searchPropertiesByDate(minSurface, maxSurface, minPrice, maxPrice, type, isSold)
            .map { list -> list.filter { it.universalLocalUserId == userId } }

    override suspend fun markPropertyAsSold(
        propertyId: String,
        saleDate: String,
        updatedAt: Long
    ) {
        entityMap[propertyId]?.let {
            upsert(
                it.copy(
                    isSold = true,
                    saleDate = saleDate,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override fun uploadUnSyncedProperties(): Flow<List<PropertyEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

    override suspend fun firstPropertyInsert(property: PropertyEntity) {
        if (!entityMap.containsKey(property.id)) {
            upsert(property)
        }
    }

    override suspend fun insertPropertyIfNotExists(property: PropertyEntity) {
        if (!entityMap.containsKey(property.id)) {
            upsert(property)
        }
    }

    override suspend fun updateProperty(property: PropertyEntity) {
        upsert(property)
    }

    override suspend fun markPropertyAsDeleted(id: String, updatedAt: Long) {
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

    override suspend fun markAllPropertiesAsDeleted(updatedAt: Long) {
        entityMap.values.forEach {
            upsert(
                it.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override suspend fun deleteProperty(property: PropertyEntity) {
        delete(property)
        unlinkAllForProperty(property.id)
    }

    override suspend fun clearAllDeleted() {
        entityMap.values
            .filter { it.isDeleted }
            .toList()
            .forEach { delete(it) }
    }

    override fun getPropertyByIdIncludeDeleted(id: String): Flow<PropertyEntity?> =
        entityFlow.map { list -> list.firstOrNull { it.id == id } }

    override fun getAllPropertiesIncludeDeleted(): Flow<List<PropertyEntity>> =
        entityFlow

    override fun getPropertyWithPoiS(propertyId: String): Flow<PropertyWithPoiSRelation> =
        combine(entityFlow, poiStore, propertyToPoi) { properties, pois, links ->

            val property = properties.firstOrNull {
                it.id == propertyId && !it.isDeleted
            } ?: error("Property not found: $propertyId")

            val relatedPoiIds = links[propertyId].orEmpty()

            val relatedPois = pois.filter {
                it.id in relatedPoiIds && !it.isDeleted
            }

            PropertyWithPoiSRelation(
                property = property,
                poiS = relatedPois
            )
        }

    override fun getAllPropertiesAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("Cursor not needed in unit tests.")
    }

}