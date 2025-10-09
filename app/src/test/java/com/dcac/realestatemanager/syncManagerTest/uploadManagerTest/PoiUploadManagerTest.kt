package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.poi.PoiUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePoiOnlineEntity
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

class PoiUploadManagerTest {

    private val poiRepository = mockk<PoiRepository>(relaxed = true)
    private val poiOnlineRepository = mockk<PoiOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: PoiUploadInterfaceManager

    private val poiEntity1 = FakePoiEntity.poi1
    private val poiEntity2 = FakePoiEntity.poi2
    private val poiEntity3 = FakePoiEntity.poi3

    private val poiOnline1 = FakePoiOnlineEntity.poiEntity1
    private val poiOnline2 = FakePoiOnlineEntity.poiEntity2

    @Before
    fun setup(){
        MockKAnnotations.init(this, relaxUnitFun = true)
        uploadManager = PoiUploadManager(poiRepository, poiOnlineRepository)
    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedPoiS_poiNotDeleted_uploadsAndUpdatesRoom() = runTest {
        coEvery { poiRepository.uploadUnSyncedPoiSToFirebase() } returns flowOf(listOf(poiEntity1))
        coEvery { poiOnlineRepository.uploadPoi(any(), any()) } returns poiOnline1

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).hasSize(1)
        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("Poi ${poiEntity1.id} uploaded")

        coVerify {
            poiOnlineRepository.uploadPoi(any(), poiEntity1.id.toString())
            poiRepository.downloadPoiFromFirebase(poiOnline1)
        }
    }

    @Test
    fun uploadUnSyncedPoiS_poiMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        val deletedPoi = poiEntity3
        coEvery { poiRepository.uploadUnSyncedPoiSToFirebase() } returns flowOf(listOf(deletedPoi))

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).hasSize(1)
        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("Poi ${deletedPoi.id} deleted")

        coVerify {
            poiOnlineRepository.deletePoi(deletedPoi.id.toString())
            poiRepository.deletePoi(deletedPoi)
        }
    }

    @Test
    fun uploadUnSyncedPoiS_globalFailure_returnsFailureStatus() = runTest {
        coEvery { poiRepository.uploadUnSyncedPoiSToFirebase() } throws RuntimeException("Room is down")

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).hasSize(1)
        val failure = result[0] as? SyncStatus.Failure
        assertThat(failure!!.label).isEqualTo("Global upload sync failed")
        assertThat(failure.error.message).isEqualTo("Room is down")
    }

    @Test
    fun uploadUnSyncedPoiS_noPoiSToUpload_returnsEmptyList() = runTest {
        coEvery { poiRepository.uploadUnSyncedPoiSToFirebase() } returns flowOf(emptyList())

        // Act
        val result = uploadManager.syncUnSyncedPoiS()

        // Assert
        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            poiOnlineRepository.uploadPoi(any(), any())
            poiRepository.downloadPoiFromFirebase(any())
            poiOnlineRepository.deletePoi(any())
            poiRepository.deletePoi(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {
        val notSyncedNotDeleted = poiEntity1
        val alreadySyncedNotDeleted = poiEntity2
        val notSyncedDeleted = poiEntity3

        coEvery {
            poiRepository.uploadUnSyncedPoiSToFirebase()
        } returns flowOf(listOf(notSyncedNotDeleted, notSyncedDeleted))

        coEvery {
            poiOnlineRepository.uploadPoi(any(), notSyncedNotDeleted.id.toString())
        } returns poiOnline1

        val result = uploadManager.syncUnSyncedPoiS()

        assertThat(result).hasSize(2)

        val uploaded = result.find { it is SyncStatus.Success && it.userEmail == "Poi ${notSyncedNotDeleted.id} uploaded" }
        val deleted = result.find { it is SyncStatus.Success && it.userEmail == "Poi ${notSyncedDeleted.id} deleted" }

        assertThat(uploaded).isNotNull()
        assertThat(deleted).isNotNull()

        coVerify { poiOnlineRepository.deletePoi(notSyncedDeleted.id.toString()) }
        coVerify { poiRepository.deletePoi(notSyncedDeleted) }
        coVerify { poiOnlineRepository.uploadPoi(any(), notSyncedNotDeleted.id.toString()) }
        coVerify { poiRepository.downloadPoiFromFirebase(poiOnline1) }

        coVerify(exactly = 0) {
            poiOnlineRepository.uploadPoi(any(), alreadySyncedNotDeleted.id.toString())
        }
        coVerify(exactly = 0) {
            poiRepository.downloadPoiFromFirebase(poiOnline2)
        }
    }

}