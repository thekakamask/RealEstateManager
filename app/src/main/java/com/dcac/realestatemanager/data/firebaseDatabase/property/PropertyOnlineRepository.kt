package com.dcac.realestatemanager.data.firebaseDatabase.property

import androidx.room.Update

interface PropertyOnlineRepository {
    suspend fun uploadProperty(property: PropertyOnlineEntity, firebasePropertyId: String): PropertyOnlineEntity
    suspend fun getProperty(firebasePropertyId: String): PropertyOnlineEntity?
    suspend fun getAllProperties(): List<FirestorePropertyDocument>
    suspend fun deleteProperty(firebasePropertyId: String)
    suspend fun deleteAllPropertiesForUser(firebaseUserId: Long)
    suspend fun markPropertyAsDeleted(firebasePropertyId: String, updatedAt: Long)
}