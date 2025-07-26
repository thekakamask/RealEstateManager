package com.dcac.realestatemanager.data.offlineDatabase.user

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserFromFirebase(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email AND password = :hashedPassword")
    fun authenticate(email: String, hashedPassword: String): Flow<UserEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    fun emailExists(email: String): Flow<Boolean>

    @Query("SELECT * FROM users WHERE is_synced = 0")
    fun getUnSyncedUsers(): Flow<List<UserEntity>>
}
