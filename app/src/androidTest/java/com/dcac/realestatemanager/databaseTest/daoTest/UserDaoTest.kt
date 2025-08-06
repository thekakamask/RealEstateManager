package com.dcac.realestatemanager.databaseTest.daoTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.data.offlineDatabase.user.UserDao
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import com.dcac.realestatemanager.databaseTest.DatabaseSetup
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class UserDaoTest: DatabaseSetup() {

    private lateinit var userDao: UserDao

    private val user1: UserEntity = FakeUserEntity.user1
    private val user2: UserEntity = FakeUserEntity.user2

    @Before
    fun setupDao() {
        userDao = db.userDao()
    }

    @Test
    fun insert_and_getUserById_shouldReturnUser() = runBlocking {
        userDao.saveUserFromFirebase(user1)
        val retrievedUser = userDao.getUserById(user1.id).first()
        assertEquals(user1, retrievedUser)
    }

    @Test
    fun getUserByEmail_shouldReturnCorrectUser()= runBlocking {
        userDao.saveUserFromFirebase(user1)
        val retrievedUser = userDao.getUserByEmail(user1.email).first()
        assertEquals(user1, retrievedUser)
    }

    @Test
    fun getAllUsers_shouldReturnAllInserted() = runBlocking {
        userDao.saveUserFromFirebase(user1)
        userDao.saveUserFromFirebase(user2)
        val all = userDao.getAllUsers().first()
        assertEquals(listOf(user1, user2), all)
    }

    @Test
    fun updateUser_shouldUpdateExisting() = runBlocking {
        userDao.saveUserFromFirebase(user1)
        val updatedUser = user1.copy(agentName = "Alice Updated")
        userDao.updateUser(updatedUser)
        val retrievedUser = userDao.getUserById(user1.id).first()
        assertEquals("Alice Updated", retrievedUser?.agentName)
    }

    @Test
    fun deleteUser_shouldRemoveUser() = runBlocking {
        userDao.saveUserFromFirebase(user1)
        userDao.deleteUser(user1)
        val deletedUser = userDao.getAllUsers().first()
        assertTrue(deletedUser.isEmpty())
    }

    @Test
    fun emailExists_shouldReturnTrueWhenEmailPresent() = runBlocking {
        userDao.saveUserFromFirebase(user1)
        val exists = userDao.emailExists(user1.email).first()
        assertTrue(exists)
    }

    @Test
    fun authenticate_shouldReturnUserOnMatchingCredentials() = runBlocking {
        userDao.saveUserFromFirebase(user1)
        val authenticatedUser = userDao.authenticate(user1.email, user1.password).first()
        assertEquals(user1, authenticatedUser)
    }

    @Test
    fun getUnSyncedUsers_shouldReturnOnlyUnsynced() = runBlocking {
        userDao.saveUserFromFirebase(user1)
        userDao.saveUserFromFirebase(user2)
        val unSyncedUsers = userDao.getUnSyncedUsers().first()
        assertEquals(listOf(user2), unSyncedUsers)

    }



}