package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyPoiCrossOnlineEntity
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

class CrossRefUploadManagerTest {

    private val crossRefRepository = mockk<PropertyPoiCrossRepository>(relaxed = true)
    private val crossRefOnlineRepository = mockk<PropertyPoiCrossOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: PropertyPoiCrossUploadInterfaceManager

    private val crossRefEntity1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRefEntity2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRefEntity3 = FakePropertyPoiCrossEntity.propertyPoiCross5
    private val crossRefOnlineEntity1 = FakePropertyPoiCrossOnlineEntity.crossOnline1

    @Before
    fun setup(){
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(FirebaseAuth::class)

        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        uploadManager = PropertyPoiCrossUploadManager(crossRefRepository, crossRefOnlineRepository)
    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedPoiS_poiNotDeleted_uploadsAndUpdatesRoom() = runTest {
        every {
            crossRefRepository.uploadUnSyncedCrossRefsToFirebase()
        } returns flowOf(listOf(crossRefEntity1))

        coEvery {
            crossRefOnlineRepository.uploadCrossRef(any())
        } returns crossRefOnlineEntity1

        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly("CrossRef ${crossRefEntity1.universalLocalPropertyId}-${crossRefEntity1.universalLocalPoiId} uploaded to Firebase")

        coVerify(exactly = 1) {
            crossRefOnlineRepository.uploadCrossRef(
                any()
            )
        }

        val updatedCrossRef = mutableListOf<PropertyPoiCrossOnlineEntity>()

        val expectedId = "${crossRefEntity1.universalLocalPropertyId}-${crossRefEntity1.universalLocalPoiId}"

        coVerify(exactly = 1) {
            crossRefRepository.updateCrossRefFromFirebase(
                capture(updatedCrossRef),
                expectedId
            )
        }

        assertThat(updatedCrossRef.first().universalLocalPropertyId)
            .isEqualTo(crossRefEntity1.universalLocalPropertyId)
        assertThat(updatedCrossRef.first().universalLocalPoiId)
            .isEqualTo(crossRefEntity1.universalLocalPoiId)

        coVerify(exactly = 0) {
            crossRefRepository.deleteCrossRef(any())
        }

    }

    @Test
    fun uploadUnSyncedPoiS_poiMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        every {
            crossRefRepository.uploadUnSyncedCrossRefsToFirebase()
        } returns flowOf(listOf(crossRefEntity3))
        coEvery {
            crossRefOnlineRepository.markCrossRefAsDeleted(any(), any(), any())
        } returns Unit

        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val expectedId =
            "${crossRefEntity3.universalLocalPropertyId}-${crossRefEntity3.universalLocalPoiId}"

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly(
                "CrossRef $expectedId marked deleted online & removed locally"
            )

        coVerify(exactly = 1) {
            crossRefOnlineRepository.markCrossRefAsDeleted(
                crossRefEntity3.universalLocalPoiId,
                crossRefEntity3.universalLocalPropertyId,
                crossRefEntity3.updatedAt
            )
        }
        coVerify(exactly = 1) {
            crossRefRepository.deleteCrossRef(crossRefEntity3)
        }
        coVerify(exactly = 0) {
            crossRefOnlineRepository.uploadCrossRef(any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_globalFailure_returnsFailureStatus() = runTest {
        every {
            crossRefRepository.uploadUnSyncedCrossRefsToFirebase()
        } throws RuntimeException("DB crash")

        try {
            uploadManager.syncUnSyncedPropertyPoiCross()
            throw AssertionError("Exception expected but not thrown")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("DB crash")
        }

        coVerify(exactly = 1) {
            crossRefRepository.uploadUnSyncedCrossRefsToFirebase()
        }
        coVerify(exactly = 0) {
            crossRefOnlineRepository.uploadCrossRef(any())
        }
        coVerify(exactly = 0) {
            crossRefOnlineRepository.markCrossRefAsDeleted(any(), any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.deleteCrossRef(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_noPoiSToUpload_returnsEmptyList() = runTest {
        every {
            crossRefRepository.uploadUnSyncedCrossRefsToFirebase()
        } returns flowOf(emptyList())

        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            crossRefRepository.uploadUnSyncedCrossRefsToFirebase()
        }
        coVerify(exactly = 0) {
            crossRefOnlineRepository.uploadCrossRef(any())
        }
        coVerify(exactly = 0) {
            crossRefOnlineRepository.markCrossRefAsDeleted(any(), any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.deleteCrossRef(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {
        val crossInsert = crossRefEntity1
        val crossDelete = crossRefEntity3
        val crossError = crossRefEntity2

        every {
            crossRefRepository.uploadUnSyncedCrossRefsToFirebase()
        } returns flowOf(listOf(crossInsert, crossDelete, crossError))

        coEvery {
            crossRefOnlineRepository.uploadCrossRef(any())
        } returns crossRefOnlineEntity1

        coEvery {
            crossRefOnlineRepository.markCrossRefAsDeleted(any(), any(), any())
        } returns Unit
        coEvery {
            crossRefOnlineRepository.uploadCrossRef(
                match {
                    it.universalLocalPropertyId == crossError.universalLocalPropertyId &&
                            it.universalLocalPoiId == crossError.universalLocalPoiId
                }
            )
        } throws RuntimeException("upload failed")

        val result = uploadManager.syncUnSyncedPropertyPoiCross()
        println("RESULT = $result")

        assertThat(result).hasSize(3)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val insertId =
            "${crossInsert.universalLocalPropertyId}-${crossInsert.universalLocalPoiId}"
        val deleteId =
            "${crossDelete.universalLocalPropertyId}-${crossDelete.universalLocalPoiId}"

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "CrossRef $insertId uploaded to Firebase",
            "CrossRef $deleteId marked deleted online & removed locally"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        val errorId =
            "${crossError.universalLocalPropertyId}-${crossError.universalLocalPoiId}"

        assertThat(failure.label).isEqualTo("CrossRef $errorId failed to sync")
        assertThat(failure.error).hasMessageThat().isEqualTo("upload failed")

        coVerify(exactly = 2) {
            crossRefOnlineRepository.uploadCrossRef(any())
        }

        val updatedCrossRefs = mutableListOf<PropertyPoiCrossOnlineEntity>()

        coVerify(exactly = 1) {
            crossRefRepository.updateCrossRefFromFirebase(
                capture(updatedCrossRefs),
                any()
            )
        }

        assertThat(updatedCrossRefs.first().universalLocalPropertyId)
            .isEqualTo(crossInsert.universalLocalPropertyId)

        coVerify(exactly = 1) {
            crossRefOnlineRepository.markCrossRefAsDeleted(
                crossDelete.universalLocalPoiId,
                crossDelete.universalLocalPropertyId,
                crossDelete.updatedAt
            )
        }
        coVerify(exactly = 1) {
            crossRefRepository.deleteCrossRef(crossDelete)
        }
    }

}
