package com.openclassrooms.realestatemanager.data.property

import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    fun getAllPropertiesByDate(): Flow<List<PropertyEntity>>
    fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>>
    fun getPropertyById(id: Long): Flow<PropertyEntity?>
    fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>>
    suspend fun insertProperty(property: PropertyEntity): Long
    suspend fun updateProperty(property: PropertyEntity)
    suspend fun deleteProperty(property: PropertyEntity)
    suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)
    suspend fun clearAll()
}