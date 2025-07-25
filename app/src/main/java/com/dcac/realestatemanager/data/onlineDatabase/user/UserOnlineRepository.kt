package com.dcac.realestatemanager.data.onlineDatabase.user

interface UserOnlineRepository {
    suspend fun uploadUser(user: UserOnlineEntity, userId: String)
    suspend fun getUser(userId: String): UserOnlineEntity?
}