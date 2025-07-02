package com.openclassrooms.realestatemanager.data.property

import kotlinx.coroutines.flow.Flow

class OfflinePropertyRepository(
    private val propertyDao: PropertyDao
): PropertyRepository {
    override fun getAllPropertiesByDate(): Flow<List<PropertyEntity>>
    = propertyDao.getAllPropertiesByDate()

    override fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>>
    = propertyDao.getAllPropertiesByAlphabetic()

    override fun getPropertyById(id: Long): Flow<PropertyEntity?>
    = propertyDao.getPropertyById(id)

    override fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>>
    = propertyDao.searchProperties(minSurface,maxSurface,minPrice,maxPrice,type,isSold)

    override suspend fun insertProperty(property: PropertyEntity): Long
    = propertyDao.insertProperty(property)

    override suspend fun updateProperty(property: PropertyEntity)
    = propertyDao.updateProperty(property)

    override suspend fun deleteProperty(property: PropertyEntity)
    = propertyDao.deleteProperty(property)

    override suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)
    = propertyDao.markPropertyAsSold(propertyId,saleDate)

    override suspend fun clearAll()
    = propertyDao.clearAll()

}