package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.user.FirestoreUserDocument
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

    private val firebaseUserDocument1 = FakeUserOnlineEntity.firestoreUserDocument1
    private val firebaseUserDocumentList = FakeUserOnlineEntity.firestoreUserDocumentList

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
        coEvery {userOnlineRepository.getAllUsers() } returns listOf(firebaseUserDocument1)
        every { userRepository.getUserEntityById(userOnlineEntity1.roomId) } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("User ${userOnlineEntity1.roomId} downloaded")

        coVerify(exactly = 1) {
            userRepository.downloadUserFromFirebase(userOnlineEntity1,firebaseUserDocument1.id )
        }

    }

    @Test
    fun downloadUnSyncedUsers_allUsersMissingLocally_downloadsAndInsertsAll() = runTest {
        coEvery { userOnlineRepository.getAllUsers() } returns firebaseUserDocumentList

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

        firebaseUserDocumentList.forEach {
            coVerify { userRepository.downloadUserFromFirebase(it.user, it.id) }
        }
    }

    @Test
    fun downloadUnSyncedUsers_localUserOutdated_downloadsAndUpdatesUser() = runTest {

        val outdatedLocal = userEntity1.copy(updatedAt = 1700000000000)
        val firestoreDocUpdateOnline = firebaseUserDocument1.copy(user = firebaseUserDocument1.user.copy(updatedAt = 1700000002000))


        coEvery { userOnlineRepository.getAllUsers() } returns listOf(firestoreDocUpdateOnline)
        every { userRepository.getUserEntityById(firestoreDocUpdateOnline.user.roomId) } returns flowOf(outdatedLocal)

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("User ${firestoreDocUpdateOnline.user.roomId} downloaded")

        coVerify(exactly = 1) {
            userRepository.downloadUserFromFirebase(firestoreDocUpdateOnline.user, firestoreDocUpdateOnline.id)
        }
    }

    @Test
    fun downloadUnSyncedUsers_allUsersOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        val outdatedLocalsUsers = userEntityList.mapIndexed { index, user ->
            user.copy(updatedAt = 1700000000000 + index)
        }

        val newerOnlineUsers = userOnlineEntityList.mapIndexed { index, user ->
            user.copy(updatedAt = 1700000000000 + index + 5)
        }

        val newerFirestoreDocs = newerOnlineUsers.mapIndexed { index, user ->
            FirestoreUserDocument(
                id = "firebase_uid_${index + 1}",
                user = user
            )
        }

        coEvery { userOnlineRepository.getAllUsers() } returns newerFirestoreDocs

        newerFirestoreDocs.forEachIndexed { index, doc ->
            every { userRepository.getUserEntityById(doc.user.roomId) } returns flowOf(outdatedLocalsUsers[index])
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(newerFirestoreDocs.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail)
                .isEqualTo("User ${newerFirestoreDocs[index].user.roomId} downloaded")
        }

        newerFirestoreDocs.forEach {
            coVerify { userRepository.downloadUserFromFirebase(it.user, it.id) }
        }
    }

    @Test
    fun downloadUnSyncedUsers_userAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        coEvery { userOnlineRepository.getAllUsers() } returns firebaseUserDocumentList

        firebaseUserDocumentList.forEachIndexed { index, doc ->
            every { userRepository.getUserEntityById(doc.user.roomId) } returns flowOf(userEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(firebaseUserDocumentList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail).isEqualTo("User ${firebaseUserDocumentList[index].user.roomId} already up-to-date")
        }

        coVerify(exactly = 0) {
            userRepository.downloadUserFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedUsers_allUsersAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {

        coEvery { userOnlineRepository.getAllUsers() } returns firebaseUserDocumentList

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
            userRepository.downloadUserFromFirebase(any(), any())
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

        // Cas de test : insert, update, skip, error
        val userInsert = userOnlineEntity1
        val userUpdate = userOnlineEntity2.copy(updatedAt = 1700000006000)
        val userSkip = userOnlineEntity3
        val userError = userOnlineEntity4.copy(updatedAt = 1700000008000)

        val outdatedLocal = userEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocal = userEntity3

        val firestoreDocs = listOf(
            FirestoreUserDocument(
                id = "firebase_uid_1", user = userInsert
            ),
            FirestoreUserDocument(
                id = "firebase_uid_2", user = userUpdate
            ),
            FirestoreUserDocument(
                id = "firebase_uid_3", user = userSkip
            ),
            FirestoreUserDocument(
                id = "firebase_uid_4", user = userError
            )
        )


        coEvery { userOnlineRepository.getAllUsers() } returns firestoreDocs

        every { userRepository.getUserEntityById(userInsert.roomId) } returns flowOf(null)
        every { userRepository.getUserEntityById(userUpdate.roomId) } returns flowOf(outdatedLocal)
        every { userRepository.getUserEntityById(userSkip.roomId) } returns flowOf(upToDateLocal)
        every { userRepository.getUserEntityById(userError.roomId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedUsers()

        assertThat(result).hasSize(4)

        val statusInsert = result[0] as? SyncStatus.Success
        assertThat(statusInsert).isNotNull()
        assertThat(statusInsert!!.userEmail).isEqualTo("User ${userInsert.roomId} downloaded")

        val statusUpdate = result[1] as? SyncStatus.Success
        assertThat(statusUpdate).isNotNull()
        assertThat(statusUpdate!!.userEmail).isEqualTo("User ${userUpdate.roomId} downloaded")

        val statusSkip = result[2] as? SyncStatus.Success
        assertThat(statusSkip).isNotNull()
        assertThat(statusSkip!!.userEmail).isEqualTo("User ${userSkip.roomId} already up-to-date")

        val statusError = result[3] as? SyncStatus.Failure
        assertThat(statusError).isNotNull()
        assertThat(statusError!!.label).isEqualTo("User ${userError.roomId} failed to sync")
        assertThat(statusError.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 1) { userRepository.downloadUserFromFirebase(userInsert, "firebase_uid_1") }
        coVerify(exactly = 1) { userRepository.downloadUserFromFirebase(userUpdate, "firebase_uid_2") }
        coVerify(exactly = 0) { userRepository.downloadUserFromFirebase(userSkip, any()) }
        coVerify(exactly = 0) { userRepository.downloadUserFromFirebase(userError, any()) }
    }

    @Test
    fun downloadUnSyncedUsers_individualFailure_returnsPartialSuccessWithFailure() = runTest {

        coEvery { userOnlineRepository.getAllUsers() } returns listOf(firebaseUserDocument1)
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
            userRepository.downloadUserFromFirebase(any(), any())
        }

    }

}