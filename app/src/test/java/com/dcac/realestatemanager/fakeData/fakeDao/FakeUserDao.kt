package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.user.UserDao
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Fake implementation of UserDao for unit testing purposes.
// It simulates Room behavior using in-memory data structures.
class FakeUserDao : UserDao,
    BaseFakeDao<Long, UserEntity>({ it.id }) {

    // ^ Inherits all in-memory storage logic from BaseFakeDao.
    //   - ENTITY = UserEntity
    //   - ID = Long
    //   - { it.id } tells BaseFakeDao how to extract the ID from a UserEntity

    // Initialize the fake DAO with pre-defined users from FakeUserEntity
    init {
        // 'seed()' is defined in BaseFakeDao
        // It pre-populates the in-memory storage with fake users

        //seed(listOf(FakeUserEntity.user1, FakeUserEntity.user2))
        seed(FakeUserEntity.userEntityList)
    }

    // Inserts or replaces a user in the fake database (from Firebase sync)
    // 'upsert()' comes from BaseFakeDao — insert or update based on ID
    override suspend fun saveUserFromFirebase(user: UserEntity) {
        upsert(user)
    }

    // Updates an existing user
    // Uses the same 'upsert' since update logic is identical in this case
    override suspend fun updateUser(user: UserEntity) {
        upsert(user)
    }

    // Deletes a user from the fake database
    // 'delete()' is inherited from BaseFakeDao
    // It removes the user based on its ID
    override suspend fun deleteUser(user: UserEntity) {
        delete(user)
    }

    // Returns a flow that emits a user by ID (or null if not found)
    // 'entityFlow' is a MutableStateFlow<List<UserEntity>> from BaseFakeDao
    // 'map' transforms the flow to emit only the matching user
    override fun getUserById(id: Long): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.id == id } }

    // Returns a flow that emits a user by email
    override fun getUserByEmail(email: String): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.email == email } }

    // Returns a flow that emits the full list of users
    // No transformation needed — we just expose the full flow as-is
    override fun getAllUsers(): Flow<List<UserEntity>> =
        entityFlow

    // Checks if a user with the given email exists
    override fun emailExists(email: String): Flow<Boolean> =
        entityFlow.map { list -> list.any { it.email == email } }

    // Returns a list of users that are not yet synced with Firebase
    override fun getUnSyncedUsers(): Flow<List<UserEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

    override fun getAllUsersAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("getAllUsersAsCursor is not used in unit tests.")
    }
}