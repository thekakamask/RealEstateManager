package com.dcac.realestatemanager.daoTest

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.daoTest.fakeData.DatabaseSetup
import com.dcac.realestatemanager.data.offlineDatabase.user.UserDao
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeUserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class UserDaoTest: DatabaseSetup() {

    private lateinit var userDao: UserDao

    private val users = FakeUserEntity.userEntityList

    @Before
    fun setupDao() {
        userDao = db.userDao()
    }

    @Test
    fun insert_and_getUserById_shouldReturnUser() = runBlocking {
        userDao.saveUserFromFirebase(users[0])
        val retrievedUser = userDao.getUserById(users[0].id).first()
        assertEquals(users[0], retrievedUser)
    }

    @Test
    fun getUserByEmail_shouldReturnCorrectUser() = runBlocking {
        userDao.saveUserFromFirebase(users[0])
        val retrievedUser = userDao.getUserByEmail(users[0].email).first()
        assertEquals(users[0], retrievedUser)
    }

    @Test
    fun getAllUsers_shouldReturnAllInserted() = runBlocking {
        users.forEach { userDao.saveUserFromFirebase(it) }
        val all = userDao.getAllUsers().first()
        assertEquals(users, all)
    }

    @Test
    fun updateUser_shouldUpdateExisting() = runBlocking {
        userDao.saveUserFromFirebase(users[0])
        val updatedUser = users[0].copy(agentName = "Alice Updated")
        userDao.updateUser(updatedUser)
        val retrievedUser = userDao.getUserById(users[0].id).first()
        assertEquals("Alice Updated", retrievedUser?.agentName)
    }

    @Test
    fun deleteUser_shouldRemoveUser() = runBlocking {
        userDao.saveUserFromFirebase(users[0])
        userDao.deleteUser(users[0])
        val deletedUser = userDao.getAllUsers().first()
        assertTrue(deletedUser.isEmpty())
    }

    @Test
    fun emailExists_shouldReturnTrueWhenEmailPresent() = runBlocking {
        userDao.saveUserFromFirebase(users[0])
        val exists = userDao.emailExists(users[0].email).first()
        assertTrue(exists)
    }

    @Test
    fun getUnSyncedUsers_shouldReturnOnlyUnsynced() = runBlocking {
        users.forEach { userDao.saveUserFromFirebase(it) }
        val unSyncedUsers = userDao.getUnSyncedUsers().first()
        assertEquals(listOf(users[1]), unSyncedUsers)
    }

    //This test ensures that:
    //the Cursor is not null,
    //it contains data (when the database is not empty),
    //it is closed correctly (good practice).
    @Test
    fun getAllUsersAsCursor_shouldReturnValidCursor() = runBlocking {
        users.forEach { userDao.saveUserFromFirebase(it) }
        val query = SimpleSQLiteQuery("SELECT * FROM users")
        val cursor = userDao.getAllUsersAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }

}