package com.dcac.realestatemanager.data.offlineDatabase.property

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyWithPoiS
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    //FOR UI

    fun getAllPropertiesByDate(): Flow<List<Property>>
    fun getAllPropertiesByAlphabetic(): Flow<List<Property>>
    fun getPropertyById(id: Long): Flow<Property?>
    fun getPropertiesByUserId(userId: Long): Flow<List<Property>>
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
    //suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)
    fun getPropertyWithPoiS(id: Long): Flow<PropertyWithPoiS>
    suspend fun markPropertyAsDeleted(property: Property)
    suspend fun markAllPropertyAsDeleted()

    //FOR FIREBASE SYNC

    fun getPropertyEntityById(id: Long): Flow<PropertyEntity?>
    suspend fun deleteProperty(property: PropertyEntity)
    suspend fun clearAllDeleted()
    fun uploadUnSyncedPropertiesToFirebase(): Flow<List<PropertyEntity>>
    suspend fun downloadPropertyFromFirebase(property: PropertyOnlineEntity)
}