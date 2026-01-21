package com.dcac.realestatemanager.data.firebaseDatabase.user

interface UserOnlineRepository {
    suspend fun uploadUser(user: UserOnlineEntity, firebaseUserId: String): UserOnlineEntity
    suspend fun getUser(firebaseUserId: String): FirestoreUserDocument?
    //suspend fun deleteUser(firebaseUserId: String)
    suspend fun getAllUsers(): List<FirestoreUserDocument>
    suspend fun markUserAsDeleted(firebaseUserId: String, updatedAt: Long)
}