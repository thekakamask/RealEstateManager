package com.dcac.realestatemanager.fakeData.fakeDao

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

    // Local snapshot of POIs (to build relations)
    internal val poiStore = MutableStateFlow<List<PoiEntity>>(emptyList())

    // Cross-refs: propertyId -> set of poiIds
    internal val propertyToPoi = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())

    // Pre-fill with fake Properties
    init { seed(FakePropertyEntity.propertyEntityList) }

    // ---- Helpers for tests ----------------------------------------------------

    /** Seed POIs snapshot used when building relations. */
    fun seedPois(pois: List<PoiEntity>) {
        poiStore.value = pois
    }

    /** Link a Property to one or many POIs. */
    fun linkPropertyToPois(propertyId: Long, vararg poiIds: Long) {
        val current = propertyToPoi.value.toMutableMap()
        val merged = (current[propertyId].orEmpty() + poiIds.toSet()).toSet()
        current[propertyId] = merged
        propertyToPoi.value = current
    }

    /** Remove all links for a given propertyId. */
    fun unlinkAllForProperty(propertyId: Long) {
        propertyToPoi.value -= propertyId
        //propertyToPoi.value = propertyToPoi.value - propertyId
    }

    // ---- PropertyDao implementation ------------------------------------------

    override fun getAllPropertiesByDate(): Flow<List<PropertyEntity>> =
        entityFlow.map { list -> list.sortedByDescending { it.entryDate } }

    override fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>> =
        entityFlow.map { list -> list.sortedBy { it.title } }

    override fun getPropertyById(id: Long): Flow<PropertyEntity?> =
        entityFlow.map { list -> list.find { it.id == id } }

    override fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>> =
        entityFlow.map { list ->
            list.filter { property ->
                (minSurface == null || property.surface >= minSurface) &&
                        (maxSurface == null || property.surface <= maxSurface) &&
                        (minPrice == null || property.price >= minPrice) &&
                        (maxPrice == null || property.price <= maxPrice) &&
                        (type == null || property.type == type) &&
                        (isSold == null || property.isSold == isSold)
            }
        }

    override suspend fun insertProperty(property: PropertyEntity): Long {
        upsert(property)
        return property.id
    }

    override suspend fun updateProperty(property: PropertyEntity) {
        upsert(property)
    }

    override suspend fun deleteProperty(property: PropertyEntity) {
        delete(property)
        // Optionally clean links for this Property
        unlinkAllForProperty(property.id)
    }

    override suspend fun markPropertyAsSold(propertyId: Long, saleDate: String) {
        val property = entityMap[propertyId]
        if (property != null) {
            val updated = property.copy(isSold = true, saleDate = saleDate)
            upsert(updated)
        }
    }

    override suspend fun clearAll() {
        clear()
        // Optionally clear links as well
        propertyToPoi.value = emptyMap()
    }

    override fun getPropertyWithPoiS(propertyId: Long): Flow<PropertyWithPoiSRelation> =
        combine(entityFlow, poiStore, propertyToPoi) { properties, poiS, links ->
            val property = properties.firstOrNull { it.id == propertyId }
                ?: error("Property not found for ID: $propertyId")
            val poiIds = links[propertyId].orEmpty()
            val related = poiS.filter { it.id in poiIds }
            PropertyWithPoiSRelation(property = property, poiS = related)
        }

    override fun getUnSyncedProperties(): Flow<List<PropertyEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

}