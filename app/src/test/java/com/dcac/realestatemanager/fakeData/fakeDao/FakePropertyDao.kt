package com.dcac.realestatemanager.fakeData.fakeDao

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyDao
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyWithPoiSRelation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePropertyDao(
    private val fakeCrossDao: FakePropertyPoiCrossDao,
    private val fakePoiDao: FakePoiDao
) : PropertyDao,
    BaseFakeDao<Long, PropertyEntity>({ it.id }) {

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
    }

    override fun getPropertyWithPoiS(propertyId: Long): Flow<PropertyWithPoiSRelation> {
        return fakeCrossDao.getPoiIdsForProperty(propertyId).map { poiIds ->
            val property = entityMap[propertyId]
            val poiS = fakePoiDao.entityFlow.value.filter { it.id in poiIds }
            PropertyWithPoiSRelation(
                property = property ?: error("Property not found for ID: $propertyId"),
                poiS = poiS
            )
        }
    }
}