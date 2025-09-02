package com.dcac.realestatemanager.data.onlineDatabase.user

import com.dcac.realestatemanager.model.User

interface UserOnlineRepository {
    suspend fun uploadUser(user: User, userId: String): User
    suspend fun getUser(userId: String): User?
    suspend fun deleteUser(userId: String)
    suspend fun getAllUsers(): List<User>
}