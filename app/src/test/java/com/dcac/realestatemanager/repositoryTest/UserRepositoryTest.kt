
package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakeUserDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.model.User
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var userRepository: UserRepository

    private val userEntity1 = FakeUserEntity.user1
    private val userEntity2 = FakeUserEntity.user2
    private val userEntity3 = FakeUserEntity.user3
    private val allUsersEntity = FakeUserEntity.userEntityList
    private val allUsersEntityNotDeleted = FakeUserEntity.userEntityListNotDeleted

    private val userModel1 = FakeUserModel.user1
    private val userModel2 = FakeUserModel.user2
    private val userModel3 = FakeUserModel.user3
    private val allUsersModel = FakeUserModel.userModelList
    private val allUsersModelNotDeleted = FakeUserModel.userModelListNotDeleted

    @Before
    fun setup() {
        fakeUserDao = FakeUserDao()
        userRepository = OfflineUserRepository(fakeUserDao)
    }

    @Test
    fun getUserById_returnsCorrectUser() = runTest {
        val result = userRepository.getUserById(userEntity1.id).first()
        assertNotNull(result)
        assertEquals(userModel1, result)
        assertEquals(userModel1.id, result?.id)
        assertEquals(userModel1.email, result?.email)
        assertEquals(userModel1.agentName, result?.agentName)
        assertEquals(userModel1.firebaseUid, result?.firebaseUid)
        assertEquals(userModel1.updatedAt, result?.updatedAt)
    }

    @Test
    fun getUserByEmail_returnsCorrectUser() = runTest {
        val result = userRepository.getUserByEmail(userEntity2.email).first()
        assertNotNull(result)
        assertEquals(userModel2, result)
        assertEquals(userModel2.id, result?.id)
        assertEquals(userModel2.email, result?.email)
        assertEquals(userModel2.agentName, result?.agentName)
        assertEquals(userModel2.firebaseUid, result?.firebaseUid)
        assertEquals(userModel2.updatedAt, result?.updatedAt)
    }

    @Test
    fun getAllUsers_returnsAllUsers() = runTest {
        val result = userRepository.getAllUsers().first()
        assertEquals(allUsersModelNotDeleted.size, result.size)
        assertTrue(result.containsAll(allUsersModelNotDeleted))
    }


    @Test
    fun insertUser_insertsUserCorrectly() = runTest {
        val newUser = User(
            id = 99L,
            email = "test@insert.com",
            agentName = "New Agent",
            firebaseUid = "new_firebase_uid",
            updatedAt = System.currentTimeMillis()
        )

        userRepository.insertUser(newUser)

        val insertedEntity = fakeUserDao.entityMap[newUser.id]

        assertNotNull(insertedEntity)
        assertEquals(newUser.id, insertedEntity?.id)
        assertEquals(newUser.email, insertedEntity?.email)
        assertEquals(newUser.agentName, insertedEntity?.agentName)
        assertEquals(newUser.firebaseUid, insertedEntity?.firebaseUid)
        assertEquals(newUser.isSynced, insertedEntity?.isSynced)
        assertEquals(newUser.isDeleted, insertedEntity?.isDeleted)
        assertEquals(newUser.updatedAt, insertedEntity?.updatedAt)

        val resultInserted = userRepository.getUserById(newUser.id).first()

        assertNotNull(resultInserted)
        assertEquals(newUser.id, resultInserted?.id)
        assertEquals(newUser.email, resultInserted?.email)
        assertEquals(newUser.agentName, resultInserted?.agentName)
        assertEquals(newUser.firebaseUid, resultInserted?.firebaseUid)
        assertEquals(newUser.isSynced, resultInserted?.isSynced)
        assertEquals(newUser.isDeleted, resultInserted?.isDeleted)

    }

    @Test
    fun insertAllUsers_insertsAllUsersCorrectly() = runTest {
        val newUsers = listOf(
            User(
                id = 10L,
                email = "test1@insert.com",
                agentName = "New Agent 1",
                firebaseUid = "new_firebase_uid_1",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            ),
            User(
                id = 11L,
                email = "test2@insert.com",
                agentName = "New Agent 2",
                firebaseUid = "new_firebase_uid_2",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            ),
            User(
                id = 12L,
                email = "test3@insert.com",
                agentName = "New Agent 3",
                firebaseUid = "new_firebase_uid_3",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
        )
        userRepository.insertAllUsers(newUsers)

        newUsers.forEach { expected ->
            val entity = fakeUserDao.entityMap[expected.id]
            assertNotNull(entity)
            assertEquals(expected.id, entity?.id)
            assertEquals(expected.email, entity?.email)
            assertEquals(expected.agentName, entity?.agentName)
        }

        val allUsers = userRepository.getAllUsers().first()
        newUsers.forEach { expected ->
            val actual = allUsers.find { it.id == expected.id }
            assertNotNull(actual)
            assertEquals(expected.id, actual!!.id)
            assertEquals(expected.email, actual.email)
            assertEquals(expected.agentName, actual.agentName)
        }
    }

    @Test
    fun updateUser_shouldModifyExistingUser() = runTest {
        val updated = userModel2.copy(
            email = "updated@example.com",
            agentName = "Updated Agent",
            updatedAt = System.currentTimeMillis()
        )
        userRepository.updateUser(updated)
        val result = userRepository.getUserById(userModel2.id).first()
        assertNotNull(result)
        assertEquals(updated.email, result?.email)
        assertEquals(updated.agentName, result?.agentName)
        assertFalse(result?.isSynced ?: true)
    }

    @Test
    fun updateUser_onNonExistingUser_shouldInsertIt() = runTest {
        val nonExistingUser = User(
            id = 99999L,
            email = "ghost@noemail.com",
            agentName = "Ghost Agent",
            firebaseUid = "ghost_firebase_uid",
            updatedAt = System.currentTimeMillis()
        )

        userRepository.updateUser(nonExistingUser)

        val entity = fakeUserDao.entityMap[nonExistingUser.id]
        assertNotNull(entity)
        assertEquals(nonExistingUser.email, entity?.email)

        val result = userRepository.getUserById(nonExistingUser.id).first()
        assertNotNull(result)
        assertEquals(nonExistingUser.email, result?.email)
    }

    @Test
    fun markUserAsDeleted_marksUserAsDeleted() = runTest {
        userRepository.markUserAsDeleted(userModel2)

        val rawEntity = fakeUserDao.entityMap[userModel2.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        val result = userRepository.getAllUsers().first()
        TestCase.assertFalse(result.any { it.id == userModel2.id })
    }

    @Test
    fun markUserAsDeleted_calledTwice_staysDeleted() = runTest {
        userRepository.markUserAsDeleted(userModel1)
        userRepository.markUserAsDeleted(userModel1)

        val rawEntity = fakeUserDao.entityMap[userModel1.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        val result = userRepository.getAllUsers().first()
        assertFalse(result.any { it.id == userModel1.id })
    }

    @Test
    fun markAllUsersAsDeleted_marksAllUsersAsDeleted() = runTest {
        userRepository.markAllUsersAsDeleted()

        fakeUserDao.entityMap.values.forEach {
            assertTrue(it.isDeleted)
        }

        val result = userRepository.getAllUsers().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun emailExists_returnsTrueForExistingEmail() = runTest {
        val exists = userRepository.emailExists(userModel1.email).first()
        assertTrue(exists)
    }

    @Test
    fun emailExists_returnsFalseForNonExistingEmail() = runTest {
        val exists = userRepository.emailExists("ghost@noemail.com").first()
        assertFalse(exists)
    }

    @Test
    fun getUserEntityById_returnsCorrectUserEntity() = runTest {
        val expected = userEntity1

        val result = userRepository.getUserEntityById(expected.id).first()

        assertNotNull(result)
        assertEquals(expected.id, result?.id)
        assertEquals(expected.email, result?.email)
        assertEquals(expected.agentName, result?.agentName)
        assertEquals(expected.firebaseUid, result?.firebaseUid)
        assertEquals(expected.isSynced, result?.isSynced)
        assertEquals(expected.isDeleted, result?.isDeleted)
        assertEquals(expected.updatedAt, result?.updatedAt)
    }

    @Test
    fun deleteUser_deletesUserCorrectly() = runTest {
        val beforeDelete = userRepository
            .getUserByIdIncludeDeleted(userEntity3.id)
            .first()
        assertNotNull(beforeDelete)

        userRepository.deleteUser(userEntity3)

        val afterDelete = userRepository
            .getUserByIdIncludeDeleted(userEntity3.id)
            .first()
        assertNull(afterDelete)
    }

    @Test
    fun clearAllUsersDeleted_clearsAllDeletedUsers() = runTest {
        val deletedUser = userEntity3
        assertTrue(deletedUser.isDeleted)

        val beforeClear = userRepository.getUserByIdIncludeDeleted(deletedUser.id).first()
        assertNotNull(beforeClear)

        userRepository.clearAllUsersDeleted()

        val afterClear = userRepository.getUserByIdIncludeDeleted(deletedUser.id).first()
        assertNull(afterClear)

        val stillPresent = userRepository.getUserEntityById(userEntity1.id).first()
        assertNotNull(stillPresent)
    }

    @Test
    fun uploadUnSyncedUsers_returnsUnSyncedUsers() = runTest {
        val expected = allUsersEntity.filter { !it.isSynced }
        val synced = allUsersEntity.filter { it.isSynced }

        val result = userRepository.uploadUnSyncedUsers().first()

        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun downloadUserFromFirebase_storesUserCorrectly() = runTest {
        val firebaseUser = UserOnlineEntity(
            email = "cloud@firebase.com",
            agentName = "Cloud Agent",
            firebaseUid = "firebase_new_999",
            updatedAt = System.currentTimeMillis(),
            roomId = 999L
        )

        userRepository.downloadUserFromFirebase(firebaseUser)

        val result = userRepository.getUserEntityById(firebaseUser.roomId).first()

        assertNotNull(result)
        assertEquals(firebaseUser.roomId, result?.id)
        assertEquals(firebaseUser.agentName, result?.agentName)
        assertEquals(firebaseUser.email, result?.email)
        assertTrue(result?.isSynced == true)
    }

    @Test
    fun downloadUserFromFirebase_updatesExisting() = runTest {
        val original = userEntity1
        val firebaseUser = UserOnlineEntity(
            email = "updated@firebase.com",
            agentName = "Update Agent",
            firebaseUid = "firebase_new_999",
            updatedAt = System.currentTimeMillis(),
            roomId = original.id
        )

        userRepository.downloadUserFromFirebase(firebaseUser)

        val entity = fakeUserDao.entityMap[original.id]
        assertNotNull(entity)
        assertEquals(firebaseUser.email, entity?.email)
        assertEquals(firebaseUser.agentName, entity?.agentName)
        assertTrue(entity?.isSynced == true)

        val result = userRepository.getUserEntityById(original.id).first()
        assertNotNull(result)
        assertEquals(firebaseUser.email, result?.email)
        assertEquals(firebaseUser.agentName, result?.agentName)
        assertTrue(result?.isSynced == true)
    }

    @Test
    fun getUserByIdIncludeDeleted_returnsUserDeleted() = runTest {
        val deletedUser = userEntity3
        assertTrue(deletedUser.isDeleted)

        val result = userRepository.getUserByIdIncludeDeleted(deletedUser.id).first()

        assertNotNull(result)
        assertEquals(deletedUser.id, result?.id)
        assertTrue(result?.isDeleted == true)
    }

    @Test
    fun getAllUsersIncludeDeleted_returnsAllIncludeDeleted() = runTest {
        val result = userRepository.getAllUsersIncludeDeleted().first()
        assertEquals(allUsersEntity.size, result.size)
        assertTrue(result.any { it.isDeleted })
    }

}
