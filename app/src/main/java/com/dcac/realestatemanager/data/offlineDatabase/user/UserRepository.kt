package com.dcac.realestatemanager.data.offlineDatabase.user

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    // FOR UI
    fun getUserById(id: Long): Flow<User?>
    fun getUserByEmail(email: String): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
    suspend fun firstInsertUser(user: User): Long
    suspend fun insertUser(user: User)
    suspend fun insertAllUsers(users: List<User>)
    suspend fun updateUser(user: User)
    suspend fun markUserAsDeleted(user: User)
    suspend fun markAllUsersAsDeleted()
    fun emailExists(email: String): Flow<Boolean>
    fun getUserByFirebaseUid(firebaseUid: String): Flow<User?>

    // FOR FIREBASE SYNC

    fun getUserEntityById(id: Long): Flow<UserEntity?>
    suspend fun deleteUser(user: UserEntity)
    suspend fun clearAllUsersDeleted()
    fun uploadUnSyncedUsers(): Flow<List<UserEntity>>
    suspend fun downloadUserFromFirebase(user: UserOnlineEntity, firebaseUid: String)

    fun getUserByIdIncludeDeleted(id: Long): Flow<UserEntity?>
    fun getAllUsersIncludeDeleted(): Flow<List<UserEntity>>

}
