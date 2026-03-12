
package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakeUserDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
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
    private val allUserEntityNotDeleted = FakeUserEntity.userEntityListNotDeleted
    private val allUsersEntity = FakeUserEntity.userEntityList
    private val userOnlineEntity1 = FakeUserOnlineEntity.userOnline1
    private val userOnlineEntity2 = FakeUserOnlineEntity.userOnline2
    private val userOnlineEntity3 = FakeUserOnlineEntity.userOnline3
    private val userModel1 = FakeUserModel.user1
    private val userModel2 = FakeUserModel.user2
    private val userModel3 = FakeUserModel.user3
    private val allUsersModelNotDeleted = FakeUserModel.userModelListNotDeleted

    @Before
    fun setup() {
        fakeUserDao = FakeUserDao()
        userRepository = OfflineUserRepository(fakeUserDao)
    }

    @Test
    fun getUserById_returnsCorrectUser() = runTest {
        val result = userRepository.getUserById(userModel1.universalLocalId).first()

        assertEquals(userModel1, result)
    }

    @Test
    fun getUserByEmail_returnsCorrectUser() = runTest {
        val result = userRepository.getUserByEmail(userModel2.email).first()

        assertEquals(userModel2, result)
    }

    @Test
    fun getUserByFirebaseUid_returnsCorrectUser() = runTest {
        val result = userRepository.getUserByFirebaseUid(userModel1.firebaseUid).first()

        assertEquals(userModel1, result)
    }

    @Test
    fun getAllUsers_shouldReturnsAllUsers() = runTest {
        val result = userRepository.getAllUsers().first()

        assertEquals(allUsersModelNotDeleted.size, result.size)
    }

    @Test
    fun uploadUnSyncedUsers_shouldReturnOnlyUsersWithIsSyncedFalse() = runTest {
        val result = userRepository.uploadUnSyncedUsersToFirebase().first()

        val expected = allUsersEntity
            .filter { !it.isSynced }

        assertEquals(expected, result)
    }

    @Test
    fun firstInsertUser_shouldInsertWithIsSyncedTrue() = runTest {
        val newUserModel = User(
            universalLocalId = "user-4",
            email = "adresse@hotmail.fr",
            agentName = "New agent",
            firebaseUid = "firebase_uid_4",
            updatedAt = 1900000000000L
        )

        userRepository.firstUserInsert(newUserModel)

        val resultEntity = fakeUserDao.entityMap[newUserModel.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(newUserModel.universalLocalId, id)
            assertEquals(newUserModel.email, email)
            assertEquals(newUserModel.agentName, agentName)
            assertEquals(newUserModel.firebaseUid, firebaseUid)
            assertTrue(isSynced)
            assertFalse(isDeleted)
            assertEquals(newUserModel.updatedAt, updatedAt)
        }

        val resultInserted = userRepository
            .getUserById(newUserModel.universalLocalId)
            .first()

        assertNotNull(resultInserted)
        resultInserted!!.apply{
            assertEquals(newUserModel.firebaseUid, resultInserted.firebaseUid)
            assertEquals(newUserModel.universalLocalId, resultInserted.universalLocalId)
            assertEquals(newUserModel.agentName, resultInserted.agentName)
            assertEquals(newUserModel.email, resultInserted.email)
            assertTrue(resultInserted.isSynced)
            assertEquals(newUserModel.updatedAt, resultInserted.updatedAt)
        }
    }

    @Test
    fun insertUserInsertFromFirebase_shouldInsertWithIsSyncTrue() = runTest {
        val firestoreId = "firestore-user-4"
        val onlineUser = UserOnlineEntity(
            universalLocalId = "user-4",
            email = "adresse@hotmail.fr",
            agentName = "New agent",
            updatedAt = 1900000000000L
        )

        userRepository.insertUserInsertFromFirebase(
            onlineUser, firestoreId
        )

        val resultEntity = fakeUserDao.entityMap[onlineUser.universalLocalId]

        assertNotNull(resultEntity)
        resultEntity!!.apply {
            assertEquals(onlineUser.universalLocalId, id)
            assertEquals(onlineUser.email, email)
            assertEquals(onlineUser.agentName, agentName)
            assertEquals(firestoreId, firebaseUid)
        }

        val resultInserted = userRepository
            .getUserById(onlineUser.universalLocalId)
            .first()

        assertNotNull(resultInserted)
        resultInserted!!.apply {
            assertEquals(onlineUser.universalLocalId, universalLocalId)
            assertEquals(onlineUser.email, email)
            assertEquals(onlineUser.agentName, agentName)
            assertEquals(firestoreId, firebaseUid)
            assertTrue(isSynced)
            assertEquals(onlineUser.updatedAt, updatedAt)
        }
    }

    @Test
    fun insertUsersInsertFromFirebase_shouldInsertAllWithIsSyncedTrue() = runTest {
        val insertedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-user-4",
            "firestore-user-5",
            "firestore-user-6"
        )

        val onlineUsers = listOf(
            UserOnlineEntity(
                universalLocalId = "user-4",
                email = "adresse@hotmail.fr",
                agentName = "New agent",
                updatedAt = insertedTimestamp + 1
            ),
            UserOnlineEntity(
                universalLocalId = "user-5",
                email = "adresse2@hotmail.fr",
                agentName = "New agent 2",
                updatedAt = insertedTimestamp + 2
            ),
            UserOnlineEntity(
                universalLocalId = "user-6",
                email = "adresse3@hotmail.fr",
                agentName = "New agent 3",
                updatedAt = insertedTimestamp + 3
            )
        )

        val pairs = onlineUsers.mapIndexed { index, user ->
            user to firestoreIds[index]
        }

        userRepository.insertAllUsersInsertFromFirebase(pairs)

        onlineUsers.forEachIndexed { index, expected ->

            val resultEntity = fakeUserDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)
            resultEntity!!.apply {
                assertEquals(expected.universalLocalId, id)
                assertEquals(expected.email, email)
                assertEquals(expected.agentName, agentName)
                assertEquals(firestoreIds[index], firebaseUid)
                assertTrue(isSynced)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }

        val allUsers = userRepository.getAllUsers().first()

        onlineUsers.forEachIndexed { index, expected ->
            val resultInserted = allUsers.find {
                it.universalLocalId == expected.universalLocalId
            }

            assertNotNull(resultInserted)

            resultInserted!!.apply {
                assertEquals(firestoreIds[index], firebaseUid)
                assertEquals(expected.universalLocalId, universalLocalId)
                assertEquals(expected.email, email)
                assertEquals(expected.agentName, agentName)
                assertTrue(isSynced)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }
    }

    @Test
    fun updateUserFromUI_shouldUpdateUserAndForceSyncFalse() = runTest {
        val updatedTimestamp = 1800000000000L
        val updateUser = userModel1.copy (
            agentName = "Updated agent",
            updatedAt = updatedTimestamp
        )
        userRepository.updateUser(updateUser)

        val resultEntity = fakeUserDao.entityMap[updateUser.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals("Updated agent", resultEntity.agentName)
            assertFalse(resultEntity.isSynced)
            assertEquals(updatedTimestamp, updatedAt)
        }

        val resultUpdated = userRepository
            .getUserById(updateUser.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals("Updated agent", resultUpdated.agentName)
            assertFalse(resultUpdated.isSynced)
            assertEquals(updatedTimestamp, resultUpdated.updatedAt)
        }

    }

    @Test
    fun updateUserFromFirebase_shouldUpdateUserAndForceSyncTrue() = runTest {
        val firestoreId = "firestore-user-1"
        val updatedTimestamp = 1900000000000L
        val updatedOnlineUser = userOnlineEntity1.copy(
            agentName = "Updated from Firebase",
            updatedAt = updatedTimestamp
        )

        userRepository.updateUserFromFirebase(
            user = updatedOnlineUser,
            firebaseUid = firestoreId
        )


    }

    @Test
    fun updateAllUsersFromFirebase_shouldUpdateAllUsers() = runTest {
        val updatedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-user-1",
            "firestore-user-2",
            "firestore-user-3"
        )
        val updatedUsersFromFirebase = listOf(
            userOnlineEntity1.copy(
                agentName = "Updated from Firebase 1",
                updatedAt = updatedTimestamp + 1
            ),
            userOnlineEntity2.copy(
                agentName = "Updated from Firebase 2",
                updatedAt = updatedTimestamp + 2
            ),
            userOnlineEntity3.copy(
                agentName = "Updated from Firebase 3",
                updatedAt = updatedTimestamp + 3
            )
        )
        val pairs = updatedUsersFromFirebase.mapIndexed { index, user ->
            user to firestoreIds[index]
        }

        userRepository.updateAllUsersFromFirebase(pairs)

        updatedUsersFromFirebase.forEachIndexed { index, expected ->

            val resultEntity = fakeUserDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.agentName, resultEntity.agentName)
                assertEquals(firestoreIds[index], resultEntity.firebaseUid)
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
                assertTrue(resultEntity.isSynced)
            }
        }

        val allUsers = userRepository.getAllUsers().first()

        updatedUsersFromFirebase.forEachIndexed { index, expected ->
            val resultUpdated = allUsers.find{
                it.universalLocalId == expected.universalLocalId
            }

            assertNotNull(resultUpdated)
            resultUpdated!!.apply {
                assertEquals(firestoreIds[index], firebaseUid)
                assertEquals(expected.agentName, agentName)
                assertTrue(isSynced)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }
    }

    @Test
    fun markUserAsDelete_shouldHideUserFromQueries() = runTest {
        userRepository.markUserAsDeleted(userModel2)

        val rawEntity = fakeUserDao.entityMap[userModel2.universalLocalId]
        assertNotNull(rawEntity)
        rawEntity!!.apply {
            assertTrue(isDeleted)
            assertFalse(isSynced)
        }

        val result = userRepository.getAllUsers().first()
        assertFalse(result.contains(userModel2))
    }

    @Test
    fun markAllUsersAsDelete_shouldHideUsersFromQueries() = runTest {
        userRepository.markAllUsersAsDeleted()

        val rawEntities = fakeUserDao.entityMap.values

        assertNotNull(rawEntities)

        rawEntities.apply{
            assertTrue(rawEntities.isNotEmpty())
            assertTrue(rawEntities.all {it.isDeleted})
            assertTrue(rawEntities.all {!it.isSynced})
        }

        val result = userRepository
            .getAllUsers()
            .first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteUser_shouldDeleteUser() = runTest {
        val existsBefore = fakeUserDao.entityMap.containsKey(userEntity3.id)
        assertTrue(existsBefore)

        userRepository.deleteUser(userEntity3)

        val resultEntity = fakeUserDao.entityMap.containsKey(userEntity3.id)
        assertFalse(resultEntity)

        val resultDeleted = userRepository
            .getUserByIdIncludeDeleted(userEntity3.id)
            .first()

        assertNull(resultDeleted)
    }

    @Test
    fun clearAllUsersDeleted_shouldDeleteOnlyDeletedUsers() = runTest {
        userRepository.markUserAsDeleted(userModel1)

        assertTrue(fakeUserDao.entityMap[userModel1.universalLocalId]!!.isDeleted)
        assertTrue(fakeUserDao.entityMap[userModel3.universalLocalId]!!.isDeleted)
        assertFalse(fakeUserDao.entityMap[userModel2.universalLocalId]!!.isDeleted)

        userRepository.clearAllUsersDeleted()

        assertFalse(fakeUserDao.entityMap.containsKey(userModel1.universalLocalId))
        assertFalse(fakeUserDao.entityMap.containsKey(userModel3.universalLocalId))
        assertTrue(fakeUserDao.entityMap.containsKey(userModel2.universalLocalId))

        val allProperties = userRepository.getAllUsersIncludeDeleted().first()

        assertFalse(allProperties.any { it.id == userModel1.universalLocalId })
        assertFalse(allProperties.any { it.id == userModel3.universalLocalId })
        assertTrue(allProperties.any { it.id == userModel2.universalLocalId })
    }

    @Test
    fun getUserByIdIncludeDeleted_returnsDeletedUser() = runTest {
        val result = userRepository
            .getUserByIdIncludeDeleted(userEntity3.id)
            .first()

        assertNotNull(result)

        result!!.apply {
            assertEquals(userEntity3.id, result.id)
            assertTrue(result.isDeleted)
        }
    }

    @Test
    fun getAllUsersByIdIncludeDeleted_returnsAllIncludingDeleted() = runTest {
        val result = userRepository.getAllUsersIncludeDeleted().first()

        assertEquals(allUsersEntity.size, result.size)
        assertTrue(result.any { it.isDeleted })
    }
}
