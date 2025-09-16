package com.dcac.realestatemanager.data.firebaseDatabase.user

import com.dcac.realestatemanager.model.User

interface UserOnlineRepository {
    suspend fun uploadUser(user: UserOnlineEntity, userId: String): UserOnlineEntity
    suspend fun getUser(userId: String): UserOnlineEntity?
    suspend fun deleteUser(userId: String)
    suspend fun getAllUsers(): List<UserOnlineEntity>
}