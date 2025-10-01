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

    init {
        seed(FakeUserEntity.userEntityList)
    }

    override fun getUserById(id: Long): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.id == id && !it.isDeleted } }

    override fun getUserByEmail(email: String): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.email == email && !it.isDeleted } }

    override fun getAllUsers(): Flow<List<UserEntity>> =
        entityFlow.map { list -> list.filter { !it.isDeleted } }

    override fun emailExists(email: String): Flow<Boolean> =
        entityFlow.map { list -> list.any { it.email == email && !it.isDeleted } }

    override suspend fun insertUserForcedSyncFalse(
        id: Long,
        email: String,
        agentName: String,
        firebaseUid: String,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            UserEntity(
                id = id,
                email = email,
                agentName = agentName,
                firebaseUid = firebaseUid,
                isDeleted = isDeleted,
                isSynced = false,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun insertUser(user: UserEntity): Long {
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

    override suspend fun insertAllUsers(users: List<UserEntity>) {
        users.forEach { insertUser(it) }
    }

    override suspend fun updateUserForcedSyncFalse(
        id: Long,
        email: String,
        agentName: String,
        firebaseUid: String,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        insertUserForcedSyncFalse(id, email, agentName, firebaseUid, isDeleted, updatedAt)
    }

    override suspend fun updateUser(user: UserEntity) {
        updateUserForcedSyncFalse(
            id = user.id,
            email = user.email,
            agentName = user.agentName,
            firebaseUid = user.firebaseUid,
            isDeleted = user.isDeleted,
            updatedAt = user.updatedAt
        )
    }

    override suspend fun deleteUser(user: UserEntity) {
        delete(user)
    }

    override fun getUserByIdIncludeDeleted(id: Long): Flow<UserEntity?> =
        entityFlow.map { list -> list.find { it.id == id } }

    override suspend fun clearAllUsersDeleted() {
        val toDelete = entityMap.values.filter { it.isDeleted }
        toDelete.forEach { delete(it) }
    }

    override suspend fun markUserAsDeleted(id: Long, updatedAt: Long) {
        entityMap[id]?.let {
            val updated = it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            upsert(updated)
        }
    }

    override suspend fun markAllUsersAsDeleted(updatedAt: Long) {
        val updated = entityMap.values.map {
            it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
        }
        seed(updated)
    }

    override fun getAllUserIncludeDeleted(): Flow<List<UserEntity>> = entityFlow

    override fun uploadUnSyncedUsers(): Flow<List<UserEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

    override suspend fun downloadUserFromFirebaseForcedSyncTrue(
        id: Long,
        email: String,
        agentName: String,
        firebaseUid: String,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            UserEntity(
                id = id,
                email = email,
                agentName = agentName,
                firebaseUid = firebaseUid,
                isDeleted = isDeleted,
                isSynced = true,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun downloadUserFromFirebase(user: UserEntity) {
        downloadUserFromFirebaseForcedSyncTrue(
            id = user.id,
            email = user.email,
            agentName = user.agentName,
            firebaseUid = user.firebaseUid,
            isDeleted = user.isDeleted,
            updatedAt = user.updatedAt
        )
    }

    override fun getAllUsersAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("getAllUsersAsCursor is not used in unit tests.")
    }
}