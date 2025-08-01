package com.dcac.realestatemanager.data.offlineDatabase.user

import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.hashPassword
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// OFFLINE IMPLEMENTATION OF THE UserRepository INTERFACE
// THIS CLASS HANDLES LOCAL USER OPERATIONS USING ROOM DATABASE VIA UserDao
class OfflineUserRepository(
    private val userDao: UserDao // DAO INTERFACE TO ACCESS THE USER TABLE IN THE ROOM DATABASE
) : UserRepository {

    // RETRIEVE A USER BY THEIR LOCAL ID FROM THE ROOM DATABASE
    override fun getUserById(id: Long): Flow<User?> {
        // MAP THE RESULTING UserEntity TO THE DOMAIN MODEL User
        return userDao.getUserById(id).map { it?.toModel() }
    }

    // RETRIEVE A USER BY EMAIL ADDRESS
    override fun getUserByEmail(email: String): Flow<User?> {
        // CONVERT UserEntity TO User ON THE FLY USING map()
        return userDao.getUserByEmail(email).map { it?.toModel() }
    }

    override fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { list -> list.map { it.toModel() } }

    // AUTHENTICATE A USER BY EMAIL + PASSWORD LOCALLY (OFFLINE LOGIN)
    override fun authenticateUser(email: String, password: String): Flow<User?> {
        val hashed = hashPassword(password) // HASH THE INPUT PASSWORD BEFORE QUERYING
        // PERFORM A ROOM QUERY WITH THE HASHED PASSWORD
        return userDao.authenticate(email, hashed).map { it?.toModel() }
    }

    // STORE A USER THAT WAS ORIGINALLY CREATED FROM FIREBASE (INCLUDING SYNC INFO)
    override suspend fun cacheUserFromFirebase(user: User) {
        // HASH THE PASSWORD BEFORE STORING IT LOCALLY
        val hashedUser = user.copy(password = hashPassword(user.password))
        // CONVERT TO UserEntity THEN SAVE TO ROOM
        userDao.saveUserFromFirebase(hashedUser.toEntity())
    }

    // UPDATE AN EXISTING USER LOCALLY
    override suspend fun updateUser(user: User) {
        // CONVERT TO ENTITY FORM FOR ROOM
        userDao.updateUser(user.toEntity())
    }

    // DELETE A USER FROM LOCAL ROOM DATABASE
    override suspend fun deleteUser(user: User) {
        // CONVERT TO ENTITY THEN DELETE
        userDao.deleteUser(user.toEntity())
    }

    // CHECK IF AN EMAIL IS ALREADY REGISTERED LOCALLY
    override fun emailExists(email: String): Flow<Boolean> {
        // RETURNS A BOOLEAN FLOW
        return userDao.emailExists(email)
    }

    // RETURN ALL USERS WHO ARE MARKED AS NOT SYNCED WITH FIREBASE YET (isSynced = false)
    override fun getUnSyncedUsers(): Flow<List<User>> {
        // MAP EACH UserEntity TO THE DOMAIN MODEL User
        return userDao.getUnSyncedUsers().map { list -> list.map { it.toModel() } }
    }
}