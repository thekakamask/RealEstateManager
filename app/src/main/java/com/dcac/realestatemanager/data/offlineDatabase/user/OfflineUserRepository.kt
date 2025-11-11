package com.dcac.realestatemanager.data.offlineDatabase.user

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineUserRepository(
    private val userDao: UserDao
) : UserRepository {

    //FOR UI
    override fun getUserById(id: String): Flow<User?> {
        return userDao.getUserById(id).map { it?.toModel() }
    }
    override fun getUserByEmail(email: String): Flow<User?> {
        return userDao.getUserByEmail(email).map { it?.toModel() }
    }
    override fun getUserByFirebaseUid(firebaseUid: String) : Flow<User?> {
        return userDao.getUserByFirebaseUid(firebaseUid).map { it?.toModel() }
    }
    override fun getAllUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { list -> list.map { it.toModel() } }
    override fun emailExists(email: String): Flow<Boolean> {
        return userDao.emailExists(email)
    }

    //SYNC
    override fun uploadUnSyncedUsersToFirebase(): Flow<List<UserEntity>> =
      userDao.uploadUnSyncedUsers()

    // INSERTIONS
    override suspend fun firstUserInsert(user: User): String {
        return userDao.firstUserInsertForceSyncedTrue(user.toEntity())
    }
    //INSERTIONS FROM FIREBASE
    override suspend fun insertUserInsertFromFirebase(user: UserOnlineEntity, firebaseUid: String) {
        userDao.insertUserNotExistingFromFirebase(user.toEntity(firebaseUid = firebaseUid))
    }
    override suspend fun insertAllUsersInsertFromFirebase(users: List<Pair<UserOnlineEntity, String>>) {
        val entities = users.map { (user, firebaseUid) ->
            user.toEntity(firebaseUid = firebaseUid)
        }
        userDao.insertAllUsersNotExistingFromFirebase(entities)
    }

    // UPDATE
    override suspend fun updateUser(user: User) {
        userDao.updateUserFromUIForceSyncFalse(user.toEntity())
    }
    override suspend fun updateUserFromFirebase(user: UserOnlineEntity, firebaseUid: String) {
        userDao.updateUserFromFirebaseForceSyncTrue(user.toEntity(firebaseUid = firebaseUid))
    }

    override suspend fun updateAllUsersFromFirebase(users: List<Pair<UserOnlineEntity, String>>) {
        val entities = users.map { (user, firebaseUid) ->
            user.toEntity(firebaseUid = firebaseUid)
        }
        userDao.updateAllUsersFromFirebaseForceSyncTrue(entities)
    }

    // SOFT DELETE
    override suspend fun markUserAsDeleted(user: User) {
        userDao.markUserAsDeleted(user.universalLocalId, System.currentTimeMillis())
    }
    override suspend fun markAllUsersAsDeleted() {
        userDao.markAllUsersAsDeleted(System.currentTimeMillis())
    }

    // HARD DELETE
    override suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
    override suspend fun clearAllUsersDeleted() {
        userDao.clearAllUsersDeleted()
    }

    // FOR TEST HARD DELETE CHECK
    override fun getUserByIdIncludeDeleted(id: String): Flow<UserEntity?> =
        userDao.getUserByIdIncludeDeleted(id)
    override fun getAllUsersIncludeDeleted(): Flow<List<UserEntity>> =
        userDao.getAllUsersIncludeDeleted()
}