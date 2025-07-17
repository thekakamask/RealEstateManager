package com.dcac.realestatemanager.data.offlinedatabase.user

import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(id: Long): Flow<User?>
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(user: User)
}