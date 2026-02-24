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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class UserDaoTest: DatabaseSetup() {

    private lateinit var userDao: UserDao

    private val user1 = FakeUserEntity.user1
    private val user2 = FakeUserEntity.user2
    private val user3 = FakeUserEntity.user3
    private val allUsersNotDeleted = FakeUserEntity.userEntityListNotDeleted
    private val allUsers = FakeUserEntity.userEntityList

    @Before
    fun setupDao() {
        userDao = db.userDao()
    }

    @Test
    fun getUserById_shouldReturnCorrectUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user2)

        val result = userDao.getUserById(user2.id).first()

        assertEquals(user2, result)
    }

    @Test
    fun getUserById_shouldNotReturnDeleteUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user3)

        val result = userDao.getUserById(user3.id).first()

        assertNull(result)
    }

    @Test
    fun getUserByIdIncludeDeleted_shouldReturnDeleteUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user3)

        val result = userDao.getUserByIdIncludeDeleted(user3.id).first()

        assertEquals(user3.id, result?.id)
    }

    @Test
    fun getUserByFirebaseUid_shouldReturnCorrectUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user2)

        val result = userDao.getUserByFirebaseUid(user2.firebaseUid).first()

        assertEquals(user2, result)
    }

    @Test
    fun getUserByFirebaseUid_shouldNotReturnDeleteUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user3)

        val result = userDao.getUserByFirebaseUid(user3.firebaseUid).first()

        assertNull(result)
    }

    @Test
    fun getUserByEmail_shouldReturnCorrectUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user2)

        val result = userDao.getUserByEmail(user2.email).first()

        assertEquals(user2, result)
    }

    @Test
    fun getUserByEmail_shouldNotReturnDeleteUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user3)

        val result = userDao.getUserByEmail(user3.email).first()

        assertNull(result)
    }

    @Test
    fun getAllUsers_shouldReturnAllUsersNotMarkAsDeleted() = runBlocking {

        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        val result = userDao.getAllUsers().first()

        val expected = allUsersNotDeleted.map { it.copy(isSynced = true) }

        assertEquals(expected, result)
    }

    @Test
    fun getAllUsersIncludeDeleted_shouldReturnAll() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        val result = userDao.getAllUsersIncludeDeleted().first()

        assertEquals(allUsers.size, result.size)
    }

    @Test
    fun emailExists_shouldReturnTrueWhenEmailPresent() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        val result = userDao.emailExists("agent2@example.com").first()

        assertTrue(result)
    }

    @Test
    fun emailExists_shouldReturnFalseWhenEmailNotPresent() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        val result = userDao.emailExists("False@example.com").first()

        assertFalse(result)
    }

    @Test
    fun uploadUnSyncedUsers_shouldReturnOnlyUnSyncedWithIsSyncedFalse() = runBlocking {
        userDao.firstUserInsertForceSyncedTrue(user1)
        userDao.updateUserFromUIForceSyncFalse(user1)

        val result = userDao.uploadUnSyncedUsers().first()

        assertEquals(1, result.size)
        assertTrue(result.all { !it.isSynced })
        assertEquals(user1.id, result.first().id)
    }

    @Test
    fun firstUserInsertForceSyncedTrue_shouldInsertUser() = runBlocking {
        userDao.firstUserInsertForceSyncedTrue(user1)

       val result = userDao.getUserByIdIncludeDeleted(user1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }

    @Test
    fun insertUserNotExistingFromFirebase_shouldInsertUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user1)

        val result = userDao.getUserByIdIncludeDeleted(user1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }

    @Test
    fun insertAllUsersNotExistingFromFirebase_shouldInsert() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        val result = userDao.getAllUsersIncludeDeleted().first()

        assertEquals(allUsers.size, result.size)
        assertTrue(result.all {it.isSynced})
    }

    @Test
    fun updateUserFromUIForceSyncFalse_shouldUpdateAndSetIsSyncFalse() = runBlocking {
        userDao.firstUserInsertForceSyncedTrue(user2)

        val updated = user2.copy(
            agentName = "Updated name",
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateUserFromUIForceSyncFalse(updated)
        val result = userDao.getUserByIdIncludeDeleted(user2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
        assertEquals(updated.agentName, result.agentName)
        assertEquals(updated.updatedAt, result.updatedAt)
    }

    @Test
    fun updateUserFromFirebaseForceSyncTrue_shouldUpdateAndSetIsSyncTrue() = runBlocking {
        userDao.firstUserInsertForceSyncedTrue(user2)

        val updated = user2.copy(
            agentName = "Updated name",
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateUserFromFirebaseForceSyncTrue(updated)
        val result = userDao.getUserByIdIncludeDeleted(user2.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
        assertEquals(updated.agentName, result.agentName)
        assertEquals(updated.updatedAt, result.updatedAt)
    }

    @Test
    fun updateAllUsersFromFirebaseForceSyncTrue_shouldUpdateAllAndSetIsSyncTrue() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        val updatedList = allUsers.map {
            it.copy(
                agentName = it.agentName + "Updated name",
                updatedAt = System.currentTimeMillis()
            )
        }

        userDao.updateAllUsersFromFirebaseForceSyncTrue(updatedList)

        val result = userDao.getAllUsersIncludeDeleted().first()

        assertEquals(updatedList.size, result.size)
        assertTrue(result.all { it.isSynced })

        val resultIds = result.map { it.agentName }.sorted()
        val expectedIds = updatedList.map { it.agentName }.sorted()

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun markUserAsDeleted_shouldMarkUserAsDeleted() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user2)

        userDao.markUserAsDeleted(user2.id, System.currentTimeMillis())

        val uiResult = userDao.getUserById(user2.id).first()
        assertEquals(null, uiResult)

        val dbResult = userDao.getUserByIdIncludeDeleted(user2.id).first()

        assertNotNull(dbResult)
        assertTrue(dbResult!!.isDeleted)
        assertFalse(dbResult.isSynced)
    }

    @Test
    fun markAllUsersAsDeleted_shouldMarkAllUsersAsDeleted() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        val updatedAt = System.currentTimeMillis()

        userDao.markAllUsersAsDeleted(updatedAt)

        val uiResult = userDao.getAllUsers().first()
        assertTrue(uiResult.isEmpty())

        val dbResult = userDao.getAllUsersIncludeDeleted().first()
        assertEquals(allUsers.size, dbResult.size)
        assertTrue(dbResult.all { it.isDeleted })
        assertTrue(dbResult.all { !it.isSynced })
        assertTrue(dbResult.all { it.updatedAt == updatedAt })

    }

    @Test
    fun deleteUser_shouldRemoveUser() = runBlocking {
        userDao.insertUserNotExistingFromFirebase(user3)

        val inserted = userDao.getUserByIdIncludeDeleted(user3.id).first()
        userDao.deleteUser(inserted!!)

        val result = userDao.getUserByIdIncludeDeleted(user3.id).first()
        assertNull(result)
    }

    @Test
    fun clearAllUsers_shouldClearAllUsers() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)

        userDao.markUserAsDeleted(user1.id, System.currentTimeMillis())
        userDao.markUserAsDeleted(user2.id, System.currentTimeMillis())

        userDao.clearAllUsersDeleted()

        val result = userDao.getAllUsersIncludeDeleted().first()

        assertEquals(0, result.size)
    }


    @Test
    fun getAllUserAsCursor_shouldReturnValidCursor() = runBlocking {
        userDao.insertAllUsersNotExistingFromFirebase(allUsers)
        val query = SimpleSQLiteQuery("SELECT * FROM users")
        val cursor = userDao.getAllUsersAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }

}
