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
    private val userEntityListNotDeleted = FakeUserEntity.userEntityListNotDeleted
    private val userOnlineEntity1 = FakeUserOnlineEntity.userOnline1
    private val userOnlineEntity2 = FakeUserOnlineEntity.userOnline2
    private val userOnlineEntity3 = FakeUserOnlineEntity.userOnline3
    private val userOnlineEntityListNotDeleted = FakeUserOnlineEntity.userOnlineEntityListNotDeleted
    private val firestoreUserDocument1 = FakeUserOnlineEntity.firestoreUserDocument1
    private val firestoreUserDocument2 = FakeUserOnlineEntity.firestoreUserDocument2
    private val firestoreUserDocument3 = FakeUserOnlineEntity.firestoreUserDocument3

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
        val userId = userOnlineEntity1.universalLocalId

        coEvery { userOnlineRepository.getAllUsers() } returns listOf(firestoreUserDocument1)
        every {
            userRepository.getUserByIdIncludeDeleted(userId)
        } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("User $userId inserted")

        val insertedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = 1) {
            userRepository.insertUserInsertFromFirebase(
                capture(insertedUsers),
                firestoreUserDocument1.firebaseId
            )
        }

        assertThat(insertedUsers.first().universalLocalId).isEqualTo(userId)

        coVerify(exactly = 0 ) {
            userRepository.updateUserFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedUsers_allUsersMissingLocally_downloadsAndInsertsAll() = runTest {
        val firestoreDocs = listOf(
            firestoreUserDocument1,
            firestoreUserDocument2,
            firestoreUserDocument3
        )

        coEvery { userOnlineRepository.getAllUsers() } returns firestoreDocs

        firestoreDocs.forEach { doc ->
            every {
                userRepository.getUserByIdIncludeDeleted(doc.user.universalLocalId)
            } returns flowOf(null)
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(3)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly(
            "User ${userEntity1.id} inserted",
            "User ${userEntity2.id} inserted",
            "User ${userEntity3.id} inserted"
        )

        val insertedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = 3) {
            userRepository.insertUserInsertFromFirebase(
                capture(insertedUsers),
                any()
            )
        }

        val insertedIds = insertedUsers.map { it.universalLocalId }

        assertThat(insertedIds)
            .containsExactly(userEntity1.id, userEntity2.id, userEntity3.id)
    }

    @Test
    fun downloadUnSyncedUsers_localUserOutdated_downloadsAndUpdatesUser() = runTest {
        val outdatedLocalUser = userEntity1.copy(updatedAt = 1700000000000)
        val updatedOnlineUser = userOnlineEntity1.copy(updatedAt = 1700000002000)
        val userId = updatedOnlineUser.universalLocalId

        val firestoreDoc = firestoreUserDocument1.copy(
            user = updatedOnlineUser
        )

        coEvery { userOnlineRepository.getAllUsers() } returns listOf(firestoreDoc)
        every {
            userRepository.getUserByIdIncludeDeleted(userId)
        } returns flowOf(outdatedLocalUser)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("User $userId updated")

        val updatedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = 1) {
            userRepository.updateUserFromFirebase(
                capture(updatedUsers),
                firestoreDoc.firebaseId
            )
        }

        assertThat(updatedUsers.first().universalLocalId).isEqualTo(userId)

        coVerify(exactly = 0) {
            userRepository.insertUserInsertFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedUsers_allUsersOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        val outdatedLocalUsers = userEntityListNotDeleted.mapIndexed { index, user ->
            user.copy(updatedAt = 1700000000000 + index)
        }
        val newerOnlineUsers = userOnlineEntityListNotDeleted.mapIndexed { index, user ->
            user.copy(updatedAt = 1700000000000 + index + 5)
        }
        val baseDocs = listOf(
            firestoreUserDocument1,
            firestoreUserDocument2
        )
        val firestoreDocs = baseDocs.mapIndexed { index, doc ->
            doc.copy(user = newerOnlineUsers[index])
        }

        coEvery { userOnlineRepository.getAllUsers() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                userRepository.getUserByIdIncludeDeleted(doc.user.universalLocalId)
            } returns flowOf(outdatedLocalUsers[index])
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "User ${it.user.universalLocalId} updated"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        val updatedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = firestoreDocs.size) {
            userRepository.updateUserFromFirebase(
                capture(updatedUsers),
                any()
            )
        }

        assertThat(updatedUsers.map { it.universalLocalId })
            .containsExactlyElementsIn(
                firestoreDocs.map { it.user.universalLocalId }
            )

        coVerify(exactly = 0) {
            userRepository.insertUserInsertFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedUsers_userAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val userId = userOnlineEntity1.universalLocalId
        val firestoreDoc = firestoreUserDocument1

        coEvery { userOnlineRepository.getAllUsers() } returns listOf(firestoreDoc)
        every {
            userRepository.getUserByIdIncludeDeleted(userId)
            } returns flowOf(userEntity1)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("User $userId already up-to-date")

        coVerify(exactly = 0) {
            userRepository.insertUserInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            userRepository.getUserByIdIncludeDeleted(userId)
        }
    }

    @Test
    fun downloadUnSyncedUsers_allUsersAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val firestoreDocs = listOf(
            firestoreUserDocument1,
            firestoreUserDocument2
        )

        coEvery { userOnlineRepository.getAllUsers() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                userRepository.getUserByIdIncludeDeleted(doc.user.universalLocalId)
            } returns flowOf(userEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "User ${it.user.universalLocalId} already up-to-date"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        coVerify(exactly = 0) {
            userRepository.insertUserInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
        }

        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                userRepository.getUserByIdIncludeDeleted(doc.user.universalLocalId)
            }
        }
    }

    @Test
    fun downloadUnSyncedUsers_mixedCases_returnsCorrectStatuses() = runTest {
        val userInsert = userOnlineEntity1
        val userUpdate = userOnlineEntity2.copy(updatedAt = 1700000006000)
        val userSkip = userOnlineEntity3.copy(universalLocalId = "skip_id")
        val userError = userOnlineEntity3.copy(
            universalLocalId = "error_id",
            updatedAt = 1700000008000
        )
        val userDelete = userOnlineEntity3.copy(
            universalLocalId = "delete_id",
            isDeleted = true
        )

        val outdatedLocalUser = userEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocalUser = userEntity3.copy(
            id = "skip_id",
            isDeleted = false
        )
        val localUserToDelete = userEntity3.copy(
            id = "delete_id",
            isDeleted = false
        )

        val firestoreDocs = listOf(
            firestoreUserDocument1.copy(user = userInsert),
            firestoreUserDocument2.copy(user = userUpdate),
            firestoreUserDocument3.copy(user = userSkip),
            firestoreUserDocument3.copy(user = userError),
            firestoreUserDocument3.copy(user = userDelete)
        )

        coEvery { userOnlineRepository.getAllUsers() } returns firestoreDocs
        every {
            userRepository.getUserByIdIncludeDeleted(userInsert.universalLocalId)
        } returns flowOf(null)
        every {
            userRepository.getUserByIdIncludeDeleted(userUpdate.universalLocalId)
        } returns flowOf(outdatedLocalUser)
        every {
            userRepository.getUserByIdIncludeDeleted(userSkip.universalLocalId)
        } returns flowOf(upToDateLocalUser)
        every {
            userRepository.getUserByIdIncludeDeleted(userError.universalLocalId)
        } throws RuntimeException("DB fail")
        every {
            userRepository.getUserByIdIncludeDeleted(userDelete.universalLocalId)
        } returns flowOf(localUserToDelete)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(5)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "User ${userInsert.universalLocalId} inserted",
            "User ${userUpdate.universalLocalId} updated",
            "User ${userSkip.universalLocalId} already up-to-date",
            "User ${userDelete.universalLocalId} deleted locally (remote deleted)"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label).isEqualTo("User ${userError.universalLocalId}")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB fail")

        val insertedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = 1) {
            userRepository.insertUserInsertFromFirebase(
                capture(insertedUsers),
                any()
            )
        }

        assertThat(insertedUsers.first().universalLocalId)
            .isEqualTo(userInsert.universalLocalId)

        val updatedUsers = mutableListOf<UserOnlineEntity>()

        coVerify(exactly = 1) {
            userRepository.updateUserFromFirebase(
                capture(updatedUsers),
                any()
            )
        }

        assertThat(updatedUsers.first().universalLocalId)
            .isEqualTo(userUpdate.universalLocalId)

        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(userSkip, any())
        }
        coVerify(exactly = 1) {
            userRepository.deleteUser(localUserToDelete)
        }
    }

    @Test
    fun downloadUnSyncedUsers_individualFailure_returnsPartialSuccessWithFailure() = runTest {
        val userId = userOnlineEntity1.universalLocalId
        val firestoreDoc = firestoreUserDocument1

        coEvery { userOnlineRepository.getAllUsers() } returns listOf(firestoreDoc)
        every {
            userRepository.getUserByIdIncludeDeleted(userId)
            } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("User $userId")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 0) {
            userRepository.insertUserInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
            }
        coVerify(exactly = 1) {
            userRepository.getUserByIdIncludeDeleted(userId)
        }

    }

    @Test
    fun downloadUnSyncedUsers_globalFailure_returnsFailureStatus() = runTest {
        coEvery { userOnlineRepository.getAllUsers() } throws RuntimeException("Firebase is down")

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Global user download failed")
        assertThat(failure.error).hasMessageThat().isEqualTo("Firebase is down")

        coVerify(exactly = 1) {
            userOnlineRepository.getAllUsers()
        }
        coVerify(exactly = 0) {
            userRepository.getUserByIdIncludeDeleted(any())
            }
        coVerify(exactly = 0) {
            userRepository.insertUserInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedUsers_noUsersOnline_returnsEmptyList() = runTest {
        coEvery { userOnlineRepository.getAllUsers() } returns emptyList()

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            userOnlineRepository.getAllUsers()
        }
        coVerify(exactly = 0) {
            userRepository.getUserByIdIncludeDeleted(any())
            }
        coVerify(exactly = 0) {
            userRepository.insertUserInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            userRepository.updateUserFromFirebase(any(), any())
        }
    }

}
