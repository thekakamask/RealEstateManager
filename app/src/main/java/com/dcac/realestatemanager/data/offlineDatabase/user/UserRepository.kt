package com.dcac.realestatemanager.data.offlineDatabase.user

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    // FOR UI
    fun getUserById(id: String): Flow<User?>
    fun getUserByEmail(email: String): Flow<User?>
    fun getUserByFirebaseUid(firebaseUid: String): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
    fun emailExists(email: String): Flow<Boolean>

    //SYNC
    fun uploadUnSyncedUsersToFirebase(): Flow<List<UserEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun firstUserInsert(user: User): String

    //INSERTIONS FROM FIREBASE
    suspend fun insertUserInsertFromFirebase(user: UserOnlineEntity, firebaseUid: String)
    suspend fun insertAllUsersInsertFromFirebase(users: List<Pair<UserOnlineEntity, String>>)
    //UPDATE
    suspend fun updateUser(user: User)
    suspend fun updateUserFromFirebase(user: UserOnlineEntity, firebaseUid: String)
    suspend fun updateAllUsersFromFirebase(users: List<Pair<UserOnlineEntity, String>>)
    //SOFT DELETE
    suspend fun markUserAsDeleted(user: User)
    suspend fun markAllUsersAsDeleted()

    //HARD DELETE
    suspend fun deleteUser(user : UserEntity)
    suspend fun clearAllUsersDeleted()

    // FOR TEST HARD DELETE CHECK
    fun getUserByIdIncludeDeleted(id: String): Flow<UserEntity?>
    fun getAllUsersIncludeDeleted(): Flow<List<UserEntity>>

}
