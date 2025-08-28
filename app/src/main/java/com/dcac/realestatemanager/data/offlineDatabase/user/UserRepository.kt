package com.dcac.realestatemanager.data.offlineDatabase.user

import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(id: Long): Flow<User?>
    fun getUserByEmail(email: String): Flow<User?>
    suspend fun cacheUserFromFirebase(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
    fun emailExists(email: String): Flow<Boolean>
    fun getUnSyncedUsers(): Flow<List<User>>
    fun getAllUsers(): Flow<List<User>>
}
