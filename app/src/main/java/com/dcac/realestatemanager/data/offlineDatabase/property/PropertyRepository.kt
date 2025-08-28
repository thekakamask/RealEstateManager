package com.dcac.realestatemanager.data.offlineDatabase.property

import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyWithPoiS
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
    suspend fun insertProperty(property: Property): Long
    suspend fun updateProperty(property: Property)
    suspend fun deleteProperty(property: Property)
    suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)
    suspend fun clearAll()
    fun getPropertyWithPoiS(id: Long): Flow<PropertyWithPoiS>
    fun getUnSyncedProperties(): Flow<List<Property>>
}