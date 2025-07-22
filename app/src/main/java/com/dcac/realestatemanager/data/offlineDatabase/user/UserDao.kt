package com.dcac.realestatemanager.data.offlineDatabase.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email AND password = :hashedPassword LIMIT 1")
    fun authenticate(email: String, hashedPassword: String): Flow<UserEntity?>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    fun emailExists(email: String): Flow<Boolean>

    @Query("SELECT * FROM users WHERE is_synced = 0")
    fun getUnSyncedUsers(): Flow<List<UserEntity>>
}