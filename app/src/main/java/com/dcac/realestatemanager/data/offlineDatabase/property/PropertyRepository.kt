package com.dcac.realestatemanager.data.offlineDatabase.property

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyWithPoiS
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {

    //FOR UI
    fun getAllPropertiesByDate(): Flow<List<Property>>
    fun getAllPropertiesByAlphabetic(): Flow<List<Property>>
    fun getPropertyById(id: String): Flow<Property?>
    fun getPropertiesByUserId(userId: String): Flow<List<Property>>
    fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<Property>>
    suspend fun markPropertyAsSold(propertyId: String, saleDate: String, updatedAt: Long)

    //SYNC
    fun uploadUnSyncedPropertiesToFirebase(): Flow<List<PropertyEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertPropertyFromUI(property: Property)
    suspend fun insertPropertiesFromUI(properties: List<Property>)
    //INSERTIONS FROM FIREBASE
    suspend fun insertPropertyInsertFromFirebase(property: PropertyOnlineEntity, firebaseDocumentId: String)
    suspend fun insertPropertiesInsertFromFirebase(properties: List<Pair<PropertyOnlineEntity, String>>)

    //UPDATE
    suspend fun updatePropertyFromUI(property : Property)
    suspend fun updatePropertyFromFirebase(property: PropertyOnlineEntity, firebaseDocumentId: String)
    suspend fun updateAllPropertiesFromFirebase(properties: List<Pair<PropertyOnlineEntity, String>>)

    //SOFT DELETE
    suspend fun markPropertyAsDeleted(property: Property)
    suspend fun markAllPropertiesAsDeleted()

    //HARD DELETE
    suspend fun deleteProperty(property: PropertyEntity)
    suspend fun clearAllDeleted()

    //FOR TEST HARD DELETE
    fun getPropertyByIdIncludeDeleted(id: String): Flow<PropertyEntity?>
    fun getAllPropertyIncludeDeleted(): Flow<List<PropertyEntity>>

    fun getPropertyWithPoiS(propertyId: String): Flow<PropertyWithPoiS>

}