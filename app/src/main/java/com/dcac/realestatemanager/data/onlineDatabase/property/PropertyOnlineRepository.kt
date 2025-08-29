package com.dcac.realestatemanager.data.onlineDatabase.property

import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.User

interface PropertyOnlineRepository {

    suspend fun uploadProperty(property: Property, propertyId: String): Property
    suspend fun getProperty(propertyId: String, user: User): Property?
    suspend fun getAllProperties(userList: List<User>): List<Property>
    suspend fun deleteProperty(propertyId: String)
    suspend fun deleteAllPropertiesForUser(userId: Long)


}