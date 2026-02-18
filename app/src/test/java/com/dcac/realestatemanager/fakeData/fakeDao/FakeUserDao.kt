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
    BaseFakeDao<String, UserEntity>({ it.id }) {

    init {
        seed(FakeUserEntity.userEntityList)
    }

    override fun getUserById(id: String): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.id == id && !it.isDeleted } }

    override fun getUserByFirebaseUid(firebaseUid: String): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.firebaseUid == firebaseUid && !it.isDeleted } }

    override fun getUserByEmail(email: String): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.email == email && !it.isDeleted } }

    override fun getAllUsers(): Flow<List<UserEntity>> =
        entityFlow.map { list -> list.filter { !it.isDeleted } }

    override fun emailExists(email: String): Flow<Boolean> =
        entityFlow.map { list -> list.any { it.email == email && !it.isDeleted } }

    override fun uploadUnSyncedUsers(): Flow<List<UserEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isSynced }
        }

    override suspend fun firstUserInsert(user: UserEntity) {
        if (!entityMap.containsKey(user.id)) {
            upsert(user)
        }
    }

    override suspend fun insertUserIfNotExists(user: UserEntity) {
        if (!entityMap.containsKey(user.id)) {
            upsert(user)
        }
    }

    override suspend fun updateUser(user: UserEntity) {
        upsert(user)
    }

    override suspend fun markUserAsDeleted(id: String, updatedAt: Long) {
        entityMap[id]?.let {
            upsert(
                it.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override suspend fun markAllUsersAsDeleted(updatedAt: Long) {
        entityMap.values.toList().forEach {
            upsert(
                it.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override suspend fun deleteUser(user: UserEntity) {
        delete(user)
    }

    override suspend fun clearAllUsersDeleted() {
        entityMap.values
            .filter { it.isDeleted }
            .toList()
            .forEach { delete(it) }
    }

    override fun getUserByIdIncludeDeleted(id: String): Flow<UserEntity?> =
        entityFlow.map { list ->
            list.find { it.id == id }
        }

    override fun getAllUsersIncludeDeleted(): Flow<List<UserEntity>> =
        entityFlow

    override fun getAllUsersAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("Cursor not needed in unit tests.")
    }
}