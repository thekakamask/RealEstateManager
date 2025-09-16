package com.dcac.realestatemanager.data.firebaseDatabase.property

interface PropertyOnlineRepository {
    suspend fun uploadProperty(property: PropertyOnlineEntity, propertyId: String): PropertyOnlineEntity
    suspend fun getProperty(propertyId: String): PropertyOnlineEntity?
    suspend fun getAllProperties(): List<PropertyOnlineEntity>
    suspend fun deleteProperty(propertyId: String)
    suspend fun deleteAllPropertiesForUser(userId: Long)
}