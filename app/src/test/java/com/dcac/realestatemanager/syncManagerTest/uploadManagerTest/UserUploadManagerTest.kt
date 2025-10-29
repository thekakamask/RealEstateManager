package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.user.UserUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify

import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserUploadManagerTest {

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val userOnlineRepository = mockk<UserOnlineRepository>(relaxed=true)

    private lateinit var uploadManager: UserUploadInterfaceManager

    private val userEntity1 = FakeUserEntity.user1
    private val userEntity2 = FakeUserEntity.user2
    private val userEntity3 = FakeUserEntity.user3

    private val userOnline1 = FakeUserOnlineEntity.userOnline1
    private val userOnline2 = FakeUserOnlineEntity.userOnline2

    private val firebaseUserDocument1 = FakeUserOnlineEntity.firestoreUserDocument1
    private val firebaseUserDocument2 = FakeUserOnlineEntity.firestoreUserDocument2

    @Before
    fun setup(){
        MockKAnnotations.init(this, relaxUnitFun = true)
        uploadManager = UserUploadManager(userRepository, userOnlineRepository)
    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedUsers_userNotDeleted_uploadsAndUpdatesRoom() = runTest {
        coEvery { userRepository.uploadUnSyncedUsers() } returns flowOf(listOf(userEntity1))
        coEvery { userOnlineRepository.uploadUser(any(), any()) } returns userOnline1

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).hasSize(1)
        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("User ${userEntity1.id} uploaded")

        coVerify {
            userOnlineRepository.uploadUser(any(), userEntity1.firebaseUid)
            userRepository.downloadUserFromFirebase(userOnline1, firebaseUserDocument1.id)
        }
    }

    @Test
    fun uploadUnSyncedUsers_userMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        val deletedUser = userEntity3
        coEvery { userRepository.uploadUnSyncedUsers() } returns flowOf(listOf(deletedUser))

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).hasSize(1)
        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("User ${deletedUser.id} deleted")

        coVerify {
            userOnlineRepository.deleteUser(deletedUser.firebaseUid)
            userRepository.deleteUser(deletedUser)
        }
    }

    @Test
    fun uploadUnSyncedUsers_globalFailure_returnsFailureStatus() = runTest {
        coEvery { userRepository.uploadUnSyncedUsers() } throws RuntimeException("Room is down")

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).hasSize(1)
        val failure = result[0] as? SyncStatus.Failure
        assertThat(failure!!.label).isEqualTo("Global upload sync failed")
        assertThat(failure.error.message).isEqualTo("Room is down")
    }

    @Test
    fun uploadUnSyncedUsers_noUsersToUpload_returnsEmptyList() = runTest {
        coEvery { userRepository.uploadUnSyncedUsers() } returns flowOf(emptyList())

        // Act
        val result = uploadManager.syncUnSyncedUsers()

        // Assert
        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            userOnlineRepository.uploadUser(any(), any())
            userRepository.downloadUserFromFirebase(any(),any())
            userOnlineRepository.deleteUser(any())
            userRepository.deleteUser(any())
        }
    }

    @Test
    fun uploadUnSyncedUsers_mixedCases_returnsCorrectStatuses() = runTest {
        val notSyncedNotDeleted = userEntity1
        val alreadySyncedNotDeleted = userEntity2
        val notSyncedDeleted = userEntity3


        coEvery {
            userRepository.uploadUnSyncedUsers()
        } returns flowOf(listOf(notSyncedNotDeleted, notSyncedDeleted))

        coEvery {
            userOnlineRepository.uploadUser(any(), notSyncedNotDeleted.firebaseUid)
        } returns userOnline1

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).hasSize(2)

        val uploaded = result.find { it is SyncStatus.Success && it.userEmail == "User ${notSyncedNotDeleted.id} uploaded" }
        val deleted = result.find { it is SyncStatus.Success && it.userEmail == "User ${notSyncedDeleted.id} deleted" }

        assertThat(uploaded).isNotNull()
        assertThat(deleted).isNotNull()


        coVerify {
            userOnlineRepository.uploadUser(match {
                it.email == notSyncedNotDeleted.email && it.agentName == notSyncedNotDeleted.agentName
            }, notSyncedNotDeleted.firebaseUid)

            userRepository.downloadUserFromFirebase(userOnline1, notSyncedNotDeleted.firebaseUid)

            //userRepository.downloadUserFromFirebase(
            //                match {
            //                    it.email == userOnline1.email &&
            //                            it.agentName == userOnline1.agentName &&
            //                            it.roomId == userOnline1.roomId
            //                },
            //                notSyncedNotDeleted.firebaseUid
            //            )
        }

        coVerify {
            userOnlineRepository.deleteUser(notSyncedDeleted.firebaseUid)
            userRepository.deleteUser(notSyncedDeleted)
        }

        coVerify(exactly = 0) {
            userOnlineRepository.uploadUser(any(), alreadySyncedNotDeleted.firebaseUid)
            userRepository.downloadUserFromFirebase(any(), alreadySyncedNotDeleted.firebaseUid)
        }
    }
}