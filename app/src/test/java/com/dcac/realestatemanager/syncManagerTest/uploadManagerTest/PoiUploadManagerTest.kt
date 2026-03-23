package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.poi.PoiUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePoiOnlineEntity
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

class PoiUploadManagerTest {

    private val poiRepository = mockk<PoiRepository>(relaxed = true)
    private val poiOnlineRepository = mockk<PoiOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: PoiUploadInterfaceManager

    private val poiEntity1 = FakePoiEntity.poi1
    private val poiEntity2 = FakePoiEntity.poi2
    private val poiEntity3 = FakePoiEntity.poi3
    private val poiOnlineEntity1 = FakePoiOnlineEntity.poiOnline1

    @Before
    fun setup(){
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(FirebaseAuth::class)

        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        uploadManager = PoiUploadManager(poiRepository, poiOnlineRepository)
    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedPoiS_poiNotDeleted_uploadsAndUpdatesRoom() = runTest {
        every {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        } returns flowOf(listOf(poiEntity1))

        coEvery {
            poiOnlineRepository.uploadPoi(any(), any())
        } returns poiOnlineEntity1

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly("Poi ${poiEntity1.id} uploaded to Firebase")

        coVerify(exactly = 1) {
            poiOnlineRepository.uploadPoi(
                any(),
                poiEntity1.firestoreDocumentId!!
            )
        }

        val updatedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = 1) {
            poiRepository.updatePoiFromFirebase(
                capture(updatedPoiS),
                poiEntity1.firestoreDocumentId!!
            )
        }

        assertThat(updatedPoiS.first().universalLocalId)
            .isEqualTo(poiEntity1.id)

        coVerify(exactly = 0) {
            poiRepository.deletePoi(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_poiMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        every {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        } returns flowOf(listOf(poiEntity3))

        coEvery {
            poiOnlineRepository.markPoiAsDeleted(any(), any())
        } returns Unit

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly(
                "Poi ${poiEntity3.id} marked deleted online & removed locally"
            )

        coVerify(exactly = 1) {
            poiOnlineRepository.markPoiAsDeleted(
                poiEntity3.firestoreDocumentId!!,
                poiEntity3.updatedAt
            )
        }

        coVerify(exactly = 1) {
            poiRepository.deletePoi(poiEntity3)
        }
        coVerify(exactly = 0) {
            poiOnlineRepository.uploadPoi(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_globalFailure_returnsFailureStatus() = runTest {
        every {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        } throws RuntimeException("DB crash")

        try {
            uploadManager.syncUnSyncedPoiS()
            throw AssertionError("Exception expected but not thrown")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("DB crash")
        }

        coVerify(exactly = 1) {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        }
        coVerify(exactly = 0) {
            poiOnlineRepository.uploadPoi(any(), any())
        }
        coVerify(exactly = 0) {
            poiOnlineRepository.markPoiAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.deletePoi(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_noPoiSToUpload_returnsEmptyList() = runTest {
        every {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        } returns flowOf(emptyList())

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        }
        coVerify(exactly = 0) {
            poiOnlineRepository.uploadPoi(any(), any())
        }
        coVerify(exactly = 0) {
            poiOnlineRepository.markPoiAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.deletePoi(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {
        val poiInsert = poiEntity1
        val poiDelete = poiEntity3
        val poiError = poiEntity2

        every {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        } returns flowOf(listOf(poiInsert, poiDelete, poiError))
        coEvery {
            poiOnlineRepository.uploadPoi(any(), any())
        } returns poiOnlineEntity1
        coEvery {
            poiOnlineRepository.markPoiAsDeleted(any(), any())
        } returns Unit
        coEvery {
            poiOnlineRepository.uploadPoi(
                match { it.universalLocalId == poiError.id },
                any()
            )
        } throws RuntimeException("upload failed")

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).hasSize(3)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "Poi ${poiInsert.id} uploaded to Firebase",
            "Poi ${poiDelete.id} marked deleted online & removed locally"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Poi ${poiError.id}")
        assertThat(failure.error).hasMessageThat().isEqualTo("upload failed")

        coVerify(exactly = 2) {
            poiOnlineRepository.uploadPoi(any(), any())
        }

        val updatedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = 1) {
            poiRepository.updatePoiFromFirebase(
                capture(updatedPoiS),
                any()
            )
        }

        assertThat(updatedPoiS.first().universalLocalId)
            .isEqualTo(poiInsert.id)

        coVerify(exactly = 1) {
            poiOnlineRepository.markPoiAsDeleted(
                poiDelete.firestoreDocumentId!!,
                poiDelete.updatedAt
            )
        }
        coVerify(exactly = 1) {
            poiRepository.deletePoi(poiDelete)
        }
    }

}
