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

    @Query("SELECT * FROM users WHERE firebase_uid = :firebaseUid AND is_deleted = 0")
    fun getUserByFirebaseUid(firebaseUid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email AND is_deleted = 0")
    fun getUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE is_deleted = 0")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND is_deleted = 0)")
    fun emailExists(email: String): Flow<Boolean>

    @Query("""
        INSERT OR REPLACE INTO users (
            id, email, agent_name, firebase_uid, is_deleted, is_synced, updated_at
        ) VALUES (
            :id, :email, :agentName, :firebaseUid, :isDeleted, 0, :updatedAt
        )
    """)
    suspend fun insertUserForcedSyncFalse(
        id: Long,
        email: String,
        agentName: String,
        firebaseUid: String,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // --- Wrapper insert ---
    suspend fun insertUser(user: UserEntity): Long {
        insertUserForcedSyncFalse(
            id = user.id,
            email = user.email,
            agentName = user.agentName,
            firebaseUid = user.firebaseUid,
            isDeleted = user.isDeleted,
            updatedAt = user.updatedAt
        )
        return user.id
    }

    // --- Wrapper insert all ---
    suspend fun insertAllUsers(users: List<UserEntity>) {
        users.forEach { insertUser(it) }
    }


    @Query("""
        UPDATE users SET 
            email = :email,
            agent_name = :agentName,
            firebase_uid = :firebaseUid,
            is_deleted = :isDeleted,
            is_synced = 0,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateUserForcedSyncFalse(
        id: Long,
        email: String,
        agentName: String,
        firebaseUid: String,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // --- Wrapper update ---
    suspend fun updateUser(user: UserEntity) {
        updateUserForcedSyncFalse(
            id = user.id,
            email = user.email,
            agentName = user.agentName,
            firebaseUid = user.firebaseUid,
            isDeleted = user.isDeleted,
            updatedAt = user.updatedAt
        )
    }

    // --- Hard delete ---
    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserByIdIncludeDeleted(id: Long): Flow<UserEntity?>

    @Query("DELETE FROM users WHERE is_deleted = 1")
    suspend fun clearAllUsersDeleted()

    // --- Soft delete ---
    @Query("UPDATE users SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markUserAsDeleted(id: Long, updatedAt: Long)

    @Query("UPDATE users SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllUsersAsDeleted(updatedAt: Long)

    //for test check hard delete
    @Query("SELECT * FROM users")
    fun getAllUserIncludeDeleted(): Flow<List<UserEntity>>

    // --- Sync ---
    @Query("SELECT * FROM users WHERE is_synced = 0")
    fun uploadUnSyncedUsers(): Flow<List<UserEntity>>

    @Query("""
        INSERT OR REPLACE INTO users (
            id, email, agent_name, firebase_uid, is_deleted, is_synced, updated_at
        ) VALUES (
            :id, :email, :agentName, :firebaseUid, :isDeleted, 1, :updatedAt
        )
    """)
    suspend fun downloadUserFromFirebaseForcedSyncTrue(
        id: Long,
        email: String,
        agentName: String,
        firebaseUid: String,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // --- Wrapper Firebase insert ---
    suspend fun downloadUserFromFirebase(user: UserEntity) {
        downloadUserFromFirebaseForcedSyncTrue(
            id = user.id,
            email = user.email,
            agentName = user.agentName,
            firebaseUid = user.firebaseUid,
            isDeleted = user.isDeleted,
            updatedAt = user.updatedAt
        )
    }

    // --- ContentProvider support ---
    @RawQuery(observedEntities = [UserEntity::class])
    fun getAllUsersAsCursor(query: SupportSQLiteQuery): Cursor
}
