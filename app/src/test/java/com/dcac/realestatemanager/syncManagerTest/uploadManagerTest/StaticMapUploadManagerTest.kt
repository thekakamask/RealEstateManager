package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeStaticMapEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeStaticMapOnlineEntity
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

class StaticMapUploadManagerTest {

    private val staticMapRepository = mockk<StaticMapRepository>(relaxed = true)
    private val staticMapOnlineRepository = mockk<StaticMapOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: StaticMapUploadManager

    private val staticMapEntity1 = FakeStaticMapEntity.staticMap1
    private val staticMapEntity2 = FakeStaticMapEntity.staticMap2
    private val staticMapEntity3 = FakeStaticMapEntity.staticMap3
    private val staticMapOnlineEntity1 = FakeStaticMapOnlineEntity.staticMapOnline1

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(FirebaseAuth::class)

        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        uploadManager = StaticMapUploadManager(staticMapRepository, staticMapOnlineRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedStaticMaps_staticMapNotDeleted_uploadsAndUpdatesRoom() = runTest {
        every {
            staticMapRepository.uploadUnSyncedStaticMapToFirebase()
        } returns flowOf(listOf(staticMapEntity1))

        coEvery {
            staticMapOnlineRepository.uploadStaticMap(any(), any())
        } returns staticMapOnlineEntity1

        val result = uploadManager.syncUnSyncedStaticMaps()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly("StaticMap ${staticMapEntity1.id} uploaded to Firebase")

        coVerify(exactly = 1) {
            staticMapOnlineRepository.uploadStaticMap(
                any(),
                staticMapEntity1.firestoreDocumentId!!
            )
        }

        val updatedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = 1) {
            staticMapRepository.updateStaticMapFromFirebase(
                capture(updatedStaticMaps),
                staticMapEntity1.firestoreDocumentId!!
            )
        }

        assertThat(updatedStaticMaps.first().universalLocalId)
            .isEqualTo(staticMapEntity1.id)

        coVerify(exactly = 0) {
            staticMapRepository.deleteStaticMap(any())
        }
    }

    @Test
    fun uploadUnSyncedStaticMaps_staticMapMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        every {
            staticMapRepository.uploadUnSyncedStaticMapToFirebase()
        } returns flowOf(listOf(staticMapEntity3))

        coEvery {
            staticMapOnlineRepository.markStaticMapAsDeleted(any(), any())
        } returns Unit

        val result = uploadManager.syncUnSyncedStaticMaps()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly(
                "StaticMap ${staticMapEntity3.id} marked deleted online & removed locally"
            )

        coVerify(exactly = 1) {
            staticMapOnlineRepository.markStaticMapAsDeleted(
                staticMapEntity3.firestoreDocumentId!!,
                staticMapEntity3.updatedAt
            )
        }
        coVerify(exactly = 1) {
            staticMapRepository.deleteStaticMap(staticMapEntity3)
        }
        coVerify(exactly = 0) {
            staticMapOnlineRepository.uploadStaticMap(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
    }

    @Test
    fun uploadUnSyncedStaticMaps_globalFailure_throwsException() = runTest {
        every {
            staticMapRepository.uploadUnSyncedStaticMapToFirebase()
        } throws RuntimeException("DB crash")

        try {
            uploadManager.syncUnSyncedStaticMaps()
            throw AssertionError("Exception expected but not thrown")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("DB crash")
        }

        coVerify(exactly = 1) {
            staticMapRepository.uploadUnSyncedStaticMapToFirebase()
        }
        coVerify(exactly = 0) {
            staticMapOnlineRepository.uploadStaticMap(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapOnlineRepository.markStaticMapAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.deleteStaticMap(any())
        }
    }

    @Test
    fun uploadUnSyncedStaticMaps_noStaticMapsToUpload_returnsEmptyList() = runTest {
        every {
            staticMapRepository.uploadUnSyncedStaticMapToFirebase()
        } returns flowOf(emptyList())

        val result = uploadManager.syncUnSyncedStaticMaps()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            staticMapRepository.uploadUnSyncedStaticMapToFirebase()
        }
        coVerify(exactly = 0) {
            staticMapOnlineRepository.uploadStaticMap(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapOnlineRepository.markStaticMapAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.deleteStaticMap(any())
        }
    }

    @Test
    fun uploadUnSyncedStaticMaps_mixedCases_returnsCorrectStatuses() = runTest {
        val staticMapInsert = staticMapEntity1
        val staticMapDelete = staticMapEntity3
        val staticMapError = staticMapEntity2

        every {
            staticMapRepository.uploadUnSyncedStaticMapToFirebase()
        } returns flowOf(listOf(staticMapInsert, staticMapDelete, staticMapError))
        coEvery {
            staticMapOnlineRepository.uploadStaticMap(any(), any())
        } returns staticMapOnlineEntity1
        coEvery {
            staticMapOnlineRepository.markStaticMapAsDeleted(any(), any())
        } returns Unit
        coEvery {
            staticMapOnlineRepository.uploadStaticMap(
                match { it.universalLocalId == staticMapError.id },
                any()
            )
        } throws RuntimeException("Upload failed")

        val result = uploadManager.syncUnSyncedStaticMaps()

        assertThat(result).hasSize(3)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "StaticMap ${staticMapInsert.id} uploaded to Firebase",
            "StaticMap ${staticMapDelete.id} marked deleted online & removed locally"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("StaticMap ${staticMapError.id}")
        assertThat(failure.error).hasMessageThat().isEqualTo("Upload failed")

        coVerify(exactly = 2) {
            staticMapOnlineRepository.uploadStaticMap(any(), any())
        }

        val updatedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = 1) {
            staticMapRepository.updateStaticMapFromFirebase(
                capture(updatedStaticMaps),
                any()
            )
        }

        assertThat(updatedStaticMaps.first().universalLocalId)
            .isEqualTo(staticMapInsert.id)

        coVerify(exactly = 1) {
            staticMapOnlineRepository.markStaticMapAsDeleted(
                staticMapDelete.firestoreDocumentId!!,
                staticMapDelete.updatedAt
            )
        }

        coVerify(exactly = 1) {
            staticMapRepository.deleteStaticMap(staticMapDelete)
        }
    }

}