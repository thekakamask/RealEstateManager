package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakeUserDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.utils.hashPassword
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
    private lateinit var userRepository: OfflineUserRepository

    @Before
    fun setup() {
        fakeUserDao = FakeUserDao()
        userRepository = OfflineUserRepository(fakeUserDao)
    }

    @Test
    fun getUserById_returnsCorrectUser() = runTest {
        val result = userRepository.getUserById(FakeUserEntity.user1.id).first()
        val expected = FakeUserModel.user1

        assertEquals(expected, result)
    }

    @Test
    fun getUserByEmail_returnsCorrectUser() = runTest {
        val result = userRepository.getUserByEmail(FakeUserEntity.user2.email).first()
        val expected = FakeUserModel.user2

        assertEquals(expected, result)
    }

    @Test
    fun authenticatUser_returnsUserIfCredentialsMatch() = runTest {
        val result = userRepository.authenticateUser(FakeUserEntity.user1.email, "passwordUser1").first()
        val expected = FakeUserModel.user1

        assertNotNull(result)
        assertEquals(expected, result)
    }

    @Test
    fun cacheUserFromFirebase_addsNewUser() = runTest {
        val expectedNewUser = FakeUserModel.user1.copy(
            id = 99L,
            email = "new@user.com",
            password = "secret",
            firebaseUid = "firebase999"
        )
        val expectedHashedPassword = hashPassword("secret")
        userRepository.cacheUserFromFirebase(expectedNewUser)

        val resultEntity = fakeUserDao.entityMap[expectedNewUser.id]

        assertEquals(expectedNewUser.email, resultEntity?.email)
        assertEquals(expectedHashedPassword, resultEntity?.password)

        val resultInserted = userRepository.getUserById(expectedNewUser.id).first()

        assertNotNull(resultInserted)
        assertEquals(expectedNewUser.email, resultInserted?.email)
        assertEquals(expectedHashedPassword, resultInserted?.password)
    }

    @Test
    fun updateUser_modifiesExistingUser() = runTest {
        val expectedUpdated = FakeUserModel.user1.copy(agentName = "Updated Name")
        userRepository.updateUser(expectedUpdated)

        val resultEntity = fakeUserDao.entityMap[expectedUpdated.id]
        assertEquals(expectedUpdated.agentName, resultEntity?.agentName)

     }

    @Test
    fun deleteUser_removesUser() = runTest {
        userRepository.deleteUser(FakeUserModel.user2)

        val resultNull = fakeUserDao.entityMap[FakeUserModel.user2.id]

        assertNull(resultNull)
    }

    @Test
    fun emailExists_returnsTrueIfEmailExists() = runTest {
        val resultExists = userRepository.emailExists(FakeUserEntity.user1.email).first()

        assertTrue(resultExists)
    }

    @Test
    fun emailExists_returnsFalseIfEmailNotFound() = runTest {
        val resultExists = userRepository.emailExists("nonexistent@email.com").first()
        assertFalse(resultExists)
    }

    @Test
    fun getUnSyncedUsers_returnsOnlyUnsyncedUsers() = runTest {
        val result = userRepository.getUnSyncedUsers().first()
        val expected = listOf(FakeUserModel.user2)
        assertEquals(expected, result)
    }

    @Test
    fun getAllUsers_returnsAllUsers() = runTest {
        val result = userRepository.getAllUsers().first()
        val expected = listOf(FakeUserModel.user1, FakeUserModel.user2)
        assertEquals(expected, result)
    }
}