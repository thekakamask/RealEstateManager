package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.user.UserUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every

import io.mockk.mockk
import io.mockk.mockkStatic
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

    @Before
    fun setup(){
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(FirebaseAuth::class)

        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        uploadManager = UserUploadManager(userRepository, userOnlineRepository)


    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedUsers_userNotDeleted_uploadsAndUpdatesRoom() = runTest {
        every {
            userRepository.uploadUnSyncedUsersToFirebase()
        } returns flowOf(listOf(userEntity1))

        coEvery {
            userOnlineRepository.uploadUser(any(), any())
        } returns userOnline1

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly("User ${userEntity1.id} uploaded to Firebase")

        coVerify(exactly = 1) {
            userOnlineRepository.uploadUser(
                any(),
                userEntity1.firebaseUid
            )
        }

        val updatedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = 1) {
            userRepository.updateUserFromFirebase(
                capture(updatedUsers),
                userEntity1.firebaseUid
            )
        }

        assertThat(updatedUsers.first().universalLocalId)
            .isEqualTo(userEntity1.id)

        coVerify(exactly = 0) {
            userRepository.deleteUser(any())
        }
    }

    @Test
    fun uploadUnSyncedUsers_userMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        every {
            userRepository.uploadUnSyncedUsersToFirebase()
        } returns flowOf(listOf(userEntity3))

        coEvery {
            userOnlineRepository.markUserAsDeleted(any(), any())
        } returns Unit

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly(
                "User ${userEntity3.id} marked deleted online & removed locally"
            )

        coVerify(exactly = 1) {
            userOnlineRepository.markUserAsDeleted(
                userEntity3.firebaseUid,
                userEntity3.updatedAt
            )
        }

        coVerify(exactly = 1) {
            userRepository.deleteUser(userEntity3)
        }
        coVerify(exactly = 0) {
            userOnlineRepository.uploadUser(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
        }
    }


    @Test
    fun uploadUnSyncedUsers_globalFailure_returnsFailureStatus() = runTest {
        every {
            userRepository.uploadUnSyncedUsersToFirebase()
        } throws RuntimeException("DB crash")

        try {
            uploadManager.syncUnSyncedUsers()
            throw AssertionError("Exception expected but not thrown")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("DB crash")
        }

        coVerify(exactly = 1) {
            userRepository.uploadUnSyncedUsersToFirebase()
        }
        coVerify(exactly = 0) {
            userOnlineRepository.uploadUser(any(), any())
            }
        coVerify(exactly = 0) {
            userOnlineRepository.markUserAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.deleteUser(any())
        }
    }

    @Test
    fun uploadUnSyncedUsers_noUsersToUpload_returnsEmptyList() = runTest {
        every {
            userRepository.uploadUnSyncedUsersToFirebase()
        } returns flowOf(emptyList())

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            userRepository.uploadUnSyncedUsersToFirebase()
        }
        coVerify(exactly = 0) {
            userOnlineRepository.uploadUser(any(), any())
        }
        coVerify(exactly = 0) {
            userOnlineRepository.markUserAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.deleteUser(any())
        }
    }

    @Test
    fun uploadUnSyncedUsers_mixedCases_returnsCorrectStatuses() = runTest {
        val userInsert = userEntity1
        val userDelete = userEntity3
        val userError = userEntity2

        every {
            userRepository.uploadUnSyncedUsersToFirebase()
        } returns flowOf(listOf(userInsert, userDelete, userError))
        coEvery {
            userOnlineRepository.uploadUser(any(), any())
        } returns userOnline1
        coEvery {
            userOnlineRepository.markUserAsDeleted(any(), any())
            } returns Unit
        coEvery {
            userOnlineRepository.uploadUser(
                match { it.universalLocalId == userError.id },
                any()
            )
        } throws RuntimeException("upload failed")

        val result = uploadManager.syncUnSyncedUsers()

        assertThat(result).hasSize(3)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "User ${userInsert.id} uploaded to Firebase",
            "User ${userDelete.id} marked deleted online & removed locally"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("User ${userError.id}")
        assertThat(failure.error).hasMessageThat().isEqualTo("upload failed")

        coVerify(exactly = 2) {
            userOnlineRepository.uploadUser(any(), any())
        }

        val updatedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = 1) {
            userRepository.updateUserFromFirebase(
                capture(updatedUsers),
                any()
            )
        }

        assertThat(updatedUsers.first().universalLocalId)
            .isEqualTo(userInsert.id)

        coVerify(exactly = 1) {
            userOnlineRepository.markUserAsDeleted(
                userDelete.firebaseUid,
                userDelete.updatedAt
            )
        }

        coVerify(exactly = 1) {
            userRepository.deleteUser(userDelete)
        }
    }
}
