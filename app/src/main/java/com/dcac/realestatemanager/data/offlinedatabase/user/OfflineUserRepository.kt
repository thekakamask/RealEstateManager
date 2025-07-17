package com.dcac.realestatemanager.data.offlinedatabase.user

import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineUserRepository(
    private val userDao: UserDao
) : UserRepository {

    override fun getUserById(id: Long): Flow<User?> {
        return userDao.getUserById(id).map { entity ->
            entity?.toModel()
        }
    }

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }
}