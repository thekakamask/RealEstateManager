package com.dcac.realestatemanager.data.offlineDatabase.user

import android.database.Cursor
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao{

    // --- Queries (filtered on is_deleted = 0) ---
    @Query("SELECT * FROM users WHERE id = :id AND is_deleted = 0")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email AND is_deleted = 0")
    fun getUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE is_deleted = 0")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND is_deleted = 0)")
    fun emailExists(email: String): Flow<Boolean>

    // --- Insert & Update ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Hard delete ---
    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    // --- Soft delete ---
    @Query("UPDATE users SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markUserAsDeleted(id: Long, updatedAt: Long)

    @Query("UPDATE users SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllUsersAsDeleted(updatedAt: Long)

    // --- Sync ---
    @Query("SELECT * FROM users WHERE is_synced = 0")
    fun uploadUnSyncedUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun downloadUserFromFirebase(user: UserEntity)

    // --- ContentProvider support ---
    @RawQuery(observedEntities = [UserEntity::class])
    fun getAllUsersAsCursor(query: SupportSQLiteQuery): Cursor
}
