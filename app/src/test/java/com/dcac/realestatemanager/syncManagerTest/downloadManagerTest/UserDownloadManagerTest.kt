package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.google.common.truth.Truth.assertThat
import com.dcac.realestatemanager.data.sync.user.UserDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.flowOf

class UserDownloadManagerTest {

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val userOnlineRepository = mockk<UserOnlineRepository>(relaxed= true)

    private lateinit var downloadManager: UserDownloadInterfaceManager

    private val userEntity1 = FakeUserEntity.user1
    private val userEntity2 = FakeUserEntity.user2
    private val userEntity3 = FakeUserEntity.user3
    private val userEntityList = FakeUserEntity.userEntityList

    private val userOnlineEntity1 = FakeUserOnlineEntity.userOnline1
    private val userOnlineEntity2 = FakeUserOnlineEntity.userOnline2
    private val userOnlineEntity3 = FakeUserOnlineEntity.userOnline3
    private val userOnlineEntityList = FakeUserOnlineEntity.userOnlineEntityList

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        downloadManager = UserDownloadManager(userRepository, userOnlineRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedUser_localUserNull_downloadsAndInsertsUser() = runTest {
        coEvery {userOnlineRepository.getAllUsers() } returns listOf(userOnlineEntity1)
        every { userRepository.getUserEntityById(userOnlineEntity1.roomId) } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("User ${userOnlineEntity1.roomId} downloaded")

        coVerify(exactly = 1) {
            userRepository.downloadUserFromFirebase(userOnlineEntity1)
        }

    }

    @Test
    fun downloadUnSyncedUsers_allUsersMissingLocally_downloadsAndInsertsAll() = runTest {
        coEvery { userOnlineRepository.getAllUsers() } returns userOnlineEntityList

        userOnlineEntityList.forEach{
            every { userRepository.getUserEntityById(it.roomId) } returns flowOf(null)
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(userOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail).isEqualTo("User ${userOnlineEntityList[index].roomId} downloaded")
        }

        userOnlineEntityList.forEach {
            coVerify { userRepository.downloadUserFromFirebase(it) }
        }
    }

    @Test
    fun downloadUnSyncedUsers_localUserOutdated_downloadsAndUpdatesUser() = runTest {

        val outdatedLocal = userEntity1.copy(updatedAt = 1700000000000)
        val updatedOnline = userOnlineEntity1.copy(updatedAt = 1700000002000)

        coEvery { userOnlineRepository.getAllUsers() } returns listOf(updatedOnline)
        every { userRepository.getUserEntityById(updatedOnline.roomId) } returns flowOf(outdatedLocal)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("User ${updatedOnline.roomId} downloaded")

        coVerify(exactly = 1) {
            userRepository.downloadUserFromFirebase(updatedOnline)
        }
    }

    @Test
    fun downloadUnSyncedUsers_allUsersOutdatedLocally_downloadsAndUpdatesAll() = runTest {

        val outdatedLocalsUsers = userEntityList.mapIndexed {index, user ->
            user.copy(updatedAt = 1700000000000 + index)
        }

        val newerOnlineUsers = userOnlineEntityList.mapIndexed {index, user ->
            user.copy(updatedAt = 1700000000000 + index + 5)
        }

        coEvery { userOnlineRepository.getAllUsers() } returns newerOnlineUsers

        newerOnlineUsers.forEachIndexed { index, userOnline ->
            every { userRepository.getUserEntityById(userOnline.roomId) } returns flowOf(outdatedLocalsUsers[index])
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(newerOnlineUsers.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail).isEqualTo("User ${newerOnlineUsers[index].roomId} downloaded")
        }

        newerOnlineUsers.forEach {
            coVerify { userRepository.downloadUserFromFirebase(it) }
        }
    }

    @Test
    fun downloadUnSyncedUsers_userAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        coEvery { userOnlineRepository.getAllUsers()} returns listOf(userOnlineEntity1)
        every { userRepository.getUserEntityById(userOnlineEntity1.roomId) } returns flowOf(userEntity1)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)
        val success = result[0] as SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success.userEmail).isEqualTo("User ${userOnlineEntity1.roomId} already up-to-date")

        coVerify(exactly = 0) {
            userRepository.downloadUserFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedUsers_allUsersAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {

        coEvery { userOnlineRepository.getAllUsers() } returns userOnlineEntityList

        userOnlineEntityList.forEachIndexed { index, onlineUser ->
            every { userRepository.getUserEntityById(onlineUser.roomId) } returns flowOf(userEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(userOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail).isEqualTo("User ${userOnlineEntityList[index].roomId} already up-to-date")
        }

        coVerify(exactly = 0) {
            userRepository.downloadUserFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedUsers_mixedCases_returnsCorrectStatuses() = runTest {
        val userOnlineEntity4 = UserOnlineEntity(
            email = "agent4@example.com",
            agentName = "Mounette Valco",
            updatedAt = 1700000008000,
            roomId = 4L
        )

        val userInsert = userOnlineEntity1
        val userUpdate = userOnlineEntity2.copy(updatedAt = 1700000006000)
        val userSkip = userOnlineEntity3
        val userError = userOnlineEntity4.copy(updatedAt = 1700000008000)

        val outdatedLocal = userEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocal = userEntity3

        val onlineUsers = listOf(userInsert, userUpdate, userSkip, userError)

        coEvery { userOnlineRepository.getAllUsers() } returns onlineUsers

        every { userRepository.getUserEntityById(userInsert.roomId) } returns flowOf(null)
        every { userRepository.getUserEntityById(userUpdate.roomId) } returns flowOf(outdatedLocal)
        every { userRepository.getUserEntityById(userSkip.roomId) } returns flowOf(upToDateLocal)
        every { userRepository.getUserEntityById(userError.roomId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(4)

        // Check insertion
        val statusInsert = result[0] as? SyncStatus.Success
        assertThat(statusInsert).isNotNull()
        assertThat(statusInsert!!.userEmail).isEqualTo("User ${userInsert.roomId} downloaded")

        // Check update
        val statusUpdate = result[1] as? SyncStatus.Success
        assertThat(statusUpdate).isNotNull()
        assertThat(statusUpdate!!.userEmail).isEqualTo("User ${userUpdate.roomId} downloaded")

        // Check skip
        val statusSkip = result[2] as? SyncStatus.Success
        assertThat(statusSkip).isNotNull()
        assertThat(statusSkip!!.userEmail).isEqualTo("User ${userSkip.roomId} already up-to-date")

        // Check error
        val statusError = result[3] as? SyncStatus.Failure
        assertThat(statusError).isNotNull()
        assertThat(statusError!!.label).isEqualTo("User ${userError.roomId} failed to sync")
        assertThat(statusError.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 1) { userRepository.downloadUserFromFirebase(userInsert) }
        coVerify(exactly = 1) { userRepository.downloadUserFromFirebase(userUpdate) }
        coVerify(exactly = 0) { userRepository.downloadUserFromFirebase(userSkip) }
        coVerify(exactly = 0) { userRepository.downloadUserFromFirebase(userError) }
    }

    @Test
    fun downloadUnSyncedUsers_individualFailure_returnsPartialSuccessWithFailure() = runTest {

        coEvery { userOnlineRepository.getAllUsers() } returns listOf(userOnlineEntity1)
        every { userRepository.getUserEntityById(userOnlineEntity1.roomId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure.label).isEqualTo("User ${userOnlineEntity1.roomId} failed to sync")
        assertThat(failure.error.message).isEqualTo("DB crash")
    }

    @Test
    fun downloadUnSyncedUsers_globalFailure_returnsFailureStatus() = runTest {

        coEvery { userOnlineRepository.getAllUsers() } throws RuntimeException("Firestore down")

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure).isNotNull()
        assertThat(failure.label).isEqualTo("Global user download failed")
        assertThat(failure.error.message).isEqualTo("Firestore down")

    }

    @Test
    fun downloadUnSyncedUsers_noUsersOnline_returnsEmptyList() = runTest {

        coEvery { userOnlineRepository.getAllUsers() } returns emptyList()

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            userRepository.downloadUserFromFirebase(any())
        }

    }

}