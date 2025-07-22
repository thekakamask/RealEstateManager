package com.dcac.realestatemanager.data.offlineDatabase.user

import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.hashPassword
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineUserRepository(
    private val userDao: UserDao
) : UserRepository {

    override fun getUserById(id: Long): Flow<User?> {
        return userDao.getUserById(id).map { it?.toModel() }
    }

    override fun getUserByEmail(email: String): Flow<User?> {
        return userDao.getUserByEmail(email).map { it?.toModel() }
    }

    override fun authenticateUser(email: String, password: String): Flow<User?> {
        val hashed = hashPassword(password)
        return userDao.authenticate(email, hashed).map { it?.toModel() }
    }

    override suspend fun insertUser(user: User) {
        val hashedUser = user.copy(password = hashPassword(user.password))
        userDao.insertUser(hashedUser.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }

    override fun emailExists(email: String): Flow<Boolean> {
        return userDao.emailExists(email)
    }

    override fun getUnSyncedUsers(): Flow<List<User>> {
        return userDao.getUnSyncedUsers().map { list -> list.map { it.toModel() } }
    }
}