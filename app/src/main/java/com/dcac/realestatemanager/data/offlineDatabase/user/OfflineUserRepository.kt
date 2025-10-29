package com.dcac.realestatemanager.data.offlineDatabase.user

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// OFFLINE IMPLEMENTATION OF THE UserRepository INTERFACE
// THIS CLASS HANDLES LOCAL USER OPERATIONS USING ROOM DATABASE VIA UserDao
class OfflineUserRepository(
    private val userDao: UserDao // DAO INTERFACE TO ACCESS THE USER TABLE IN THE ROOM DATABASE
) : UserRepository {

    //FOR UI

    // RETRIEVE A USER BY THEIR LOCAL ID FROM THE ROOM DATABASE
    override fun getUserById(id: Long): Flow<User?> {
        // MAP THE RESULTING UserEntity TO THE DOMAIN MODEL User
        return userDao.getUserById(id).map { it?.toModel() }
    }

    override fun getUserByFirebaseUid(firebaseUid: String) : Flow<User?> {
        return userDao.getUserByFirebaseUid(firebaseUid).map { it?.toModel() }
    }

    // RETRIEVE A USER BY EMAIL ADDRESS
    override fun getUserByEmail(email: String): Flow<User?> {
        // CONVERT UserEntity TO User ON THE FLY USING map()
        return userDao.getUserByEmail(email).map { it?.toModel() }
    }

    override fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { list -> list.map { it.toModel() } }

    override suspend fun firstInsertUser(user: User): Long {
        return userDao.firstUserInsert(
            user.toEntity().copy(id = 0L)
        )
    }

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun insertAllUsers(users: List<User>) {
        userDao.insertAllUsers(users.map { it.toEntity() })
    }

    // UPDATE AN EXISTING USER LOCALLY
    override suspend fun updateUser(user: User) {
        // CONVERT TO ENTITY FORM FOR ROOM
        userDao.updateUser(user.toEntity())
    }

    override suspend fun markUserAsDeleted(user: User) {
        userDao.markUserAsDeleted(user.id, System.currentTimeMillis())
    }

    override suspend fun markAllUsersAsDeleted() {
        userDao.markAllUsersAsDeleted(System.currentTimeMillis())
    }

    // CHECK IF AN EMAIL IS ALREADY REGISTERED LOCALLY
    override fun emailExists(email: String): Flow<Boolean> {
        // RETURNS A BOOLEAN FLOW
        return userDao.emailExists(email)
    }

    //FOR FIREBASE SYNC

    override fun getUserEntityById(id: Long): Flow<UserEntity?> =
        userDao.getUserById(id)

    // DELETE A USER FROM LOCAL ROOM DATABASE
    override suspend fun deleteUser(user: UserEntity) {
        // CONVERT TO ENTITY THEN DELETE
        userDao.deleteUser(user)
    }

    override suspend fun clearAllUsersDeleted() {
        userDao.clearAllUsersDeleted()
    }

    // RETURN ALL USERS WHO ARE MARKED AS NOT SYNCED WITH FIREBASE YET (isSynced = false)
    override fun uploadUnSyncedUsers(): Flow<List<UserEntity>> {
        // MAP EACH UserEntity TO THE DOMAIN MODEL User
        return userDao.uploadUnSyncedUsers()
    }

    // STORE A USER THAT WAS ORIGINALLY CREATED FROM FIREBASE (INCLUDING SYNC INFO)
    override suspend fun downloadUserFromFirebase(user: UserOnlineEntity, firebaseUid: String) {
        userDao.downloadUserFromFirebase(
            user.toEntity(userId = user.roomId, firebaseUid = firebaseUid)
        )
    }

    // --- FOR TEST / HARD DELETE CHECK ---

    override fun getUserByIdIncludeDeleted(id: Long): Flow<UserEntity?> =
        userDao.getUserByIdIncludeDeleted(id)

    override fun getAllUsersIncludeDeleted(): Flow<List<UserEntity>> =
        userDao.getAllUserIncludeDeleted()
}