package com.dcac.realestatemanager.data.offlineDatabase.user

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao{

    //FOR UI
    //QUERIES (FILTERED ON IS_DELETED = 0)
    @Query("SELECT * FROM users WHERE id = :id AND is_deleted = 0")
    fun getUserById(id: String): Flow<UserEntity?>
    @Query("SELECT * FROM users WHERE firebase_uid = :firebaseUid AND is_deleted = 0")
    fun getUserByFirebaseUid(firebaseUid: String): Flow<UserEntity?>
    @Query("SELECT * FROM users WHERE email = :email AND is_deleted = 0")
    fun getUserByEmail(email: String): Flow<UserEntity?>
    @Query("SELECT * FROM users WHERE is_deleted = 0")
    fun getAllUsers(): Flow<List<UserEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND is_deleted = 0)")
    fun emailExists(email: String): Flow<Boolean>

    //SYNC
    //DETECT UN SYNC USER AND SEND TO FIREBASE
    @Query("SELECT * FROM users WHERE is_synced = 0")
    fun uploadUnSyncedUsers(): Flow<List<UserEntity>>

    // INSERTIONS
    // USE FOR INITIAL CREATION OF AN USER (WITH NETWORK SO IS SYNC TRUE)
    suspend fun firstUserInsertForceSyncedTrue(user: UserEntity): String {
        firstUserInsert(user.copy(isSynced = true))
        return user.id
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun firstUserInsert(user: UserEntity)

    // INSERT SINGLE OR MULTIPLE USERS FROM FIREBASE IF NOT ALREADY EXISTS LOCALLY
    suspend fun insertUserNotExistingFromFirebase(user: UserEntity): String {
        insertUserIfNotExists(user.copy(isSynced = true))
        return user.firebaseUid
    }
    suspend fun insertAllUsersNotExistingFromFirebase(users: List<UserEntity>) {
        users.forEach { user ->
            insertUserIfNotExists(user.copy(isSynced = true))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserIfNotExists(user: UserEntity)

    //UPDATES
    // WHEN AN USER UPDATES HIS ACCOUNT ON HIS PHONE (UI → ROOM, will need to sync later)
    suspend fun updateUserFromUIForceSyncFalse(user: UserEntity): String {
        updateUser(user.copy(isSynced = false))
        return user.id
    }
    // WHEN FIREBASE SENDS AN UPDATED SINGLE OR MULTIPLE USERS TO ROOM
    suspend fun updateUserFromFirebaseForceSyncTrue(user: UserEntity): String {
        updateUser(user.copy(isSynced = true))
        return user.firebaseUid
    }
    suspend fun updateAllUsersFromFirebaseForceSyncTrue(users: List<UserEntity>) {
        users.forEach { user ->
            updateUser(user.copy(isSynced = true))
        }
    }
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: UserEntity)

    //SOFT DELETE
    //MARK FROM UI USERS AS DELETED BEFORE REAL DELETE
    @Query("UPDATE users SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markUserAsDeleted(id: String, updatedAt: Long)
    @Query("UPDATE users SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllUsersAsDeleted(updatedAt: Long)

    //HARD DELETE
    //AFTER MARK USER DELETE IN FIREBASE, DELETE USER FROM ROOM
    @Delete
    suspend fun deleteUser(user: UserEntity)
    @Query("DELETE FROM users WHERE is_deleted = 1")
    suspend fun clearAllUsersDeleted()

    // FOR SYNC AND TEST CHECK
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdIncludeDeleted(id: String): Flow<UserEntity?>
    @Query("SELECT * FROM users")
    fun getAllUsersIncludeDeleted(): Flow<List<UserEntity>>

    @RawQuery(observedEntities = [UserEntity::class])
    fun getAllUsersAsCursor(query: SupportSQLiteQuery): Cursor
}
