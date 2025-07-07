package com.dcac.realestatemanager.data.offlinedatabase.property

import com.dcac.realestatemanager.model.Property
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    fun getAllPropertiesByDate(): Flow<List<Property>>
    fun getAllPropertiesByAlphabetic(): Flow<List<Property>>
    fun getPropertyById(id: Long): Flow<Property?>
    fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<Property>>

    suspend fun insertProperty(property: PropertyEntity): Long
    suspend fun updateProperty(property: PropertyEntity)
    suspend fun deleteProperty(property: PropertyEntity)
    suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)
    suspend fun clearAll()
}