package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePoiOnlineEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PoiDownloadManagerTest {

    private val poiRepository = mockk<PoiRepository>(relaxed = true)
    private val poiOnlineRepository = mockk<PoiOnlineRepository>(relaxed = true)

    private lateinit var downloadManager: PoiDownloadInterfaceManager

    private val poiEntity1 = FakePoiEntity.poi1
    private val poiEntity2 = FakePoiEntity.poi2
    private val poiEntity3 = FakePoiEntity.poi3
    private val poiEntityList = FakePoiEntity.poiEntityList

    private val poiOnlineEntity1 = FakePoiOnlineEntity.poiEntity1
    private val poiOnlineEntity2 = FakePoiOnlineEntity.poiEntity2
    private val poiOnlineEntity3 = FakePoiOnlineEntity.poiEntity3
    private val poiOnlineEntityList = FakePoiOnlineEntity.poiOnlineEntityList

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        downloadManager = PoiDownloadManager(poiRepository, poiOnlineRepository)

    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedPoi_localPoiNull_downloadsAndInsertsPoi() = runTest {

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(poiOnlineEntity1)

        every { poiRepository.getPoiEntityById(poiOnlineEntity1.roomId) } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("Poi ${poiOnlineEntity1.roomId} downloaded")

        coVerify(exactly = 1) {
            poiRepository.downloadPoiFromFirebase(poiOnlineEntity1)
        }
    }

    @Test
    fun downloadUnSyncedPoiS_allPoiSMissingLocally_downloadsAndInsertsAll() = runTest {

        coEvery { poiOnlineRepository.getAllPoiS() } returns poiOnlineEntityList

        poiOnlineEntityList.forEach {
            every { poiRepository.getPoiEntityById(it.roomId) } returns flowOf(null)
        }

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(poiOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success.userEmail).isEqualTo("Poi ${poiOnlineEntityList[index].roomId} downloaded")
        }

        poiOnlineEntityList.forEach {
            coVerify { poiRepository.downloadPoiFromFirebase(it) }
        }
    }

    @Test
    fun downloadUnSyncedPoiS_localPoiOutdated_downloadsAndUpdatesPoi() = runTest {

        val outdatedLocal = poiEntity1.copy(updatedAt = 1700000000000)
        val updatedOnline = poiOnlineEntity1.copy(updatedAt = 1700000002000)

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(updatedOnline)
        every { poiRepository.getPoiEntityById(updatedOnline.roomId) } returns flowOf(outdatedLocal)

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val success = result[0] as SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success.userEmail).isEqualTo("Poi ${updatedOnline.roomId} downloaded")

        coVerify(exactly = 1) {
            poiRepository.downloadPoiFromFirebase(updatedOnline)
        }
    }


    @Test
    fun downloadUnSyncedPoiS_allPoiSOutdatedLocally_downloadsAndUpdatesAll() = runTest {

        val outdatedLocalsPoiS = poiEntityList.mapIndexed { index, poi ->
            poi.copy(updatedAt = 1700000000000 + index)
        }

        val newerOnlinePoiS = poiOnlineEntityList.mapIndexed { index, poi ->
            poi.copy(updatedAt = 1700000000000 + index + 5)
        }

        coEvery { poiOnlineRepository.getAllPoiS() } returns newerOnlinePoiS

    newerOnlinePoiS.forEachIndexed { index, poiOnline ->
            every { poiRepository.getPoiEntityById(poiOnline.roomId) } returns flowOf(outdatedLocalsPoiS[index])
        }

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(newerOnlinePoiS.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success.userEmail).isEqualTo("Poi ${newerOnlinePoiS[index].roomId} downloaded")
        }

    newerOnlinePoiS.forEach {
            coVerify { poiRepository.downloadPoiFromFirebase(it) }
        }
    }


    @Test
    fun downloadUnSyncedPoiS_poiAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(poiOnlineEntity1)

        every { poiRepository.getPoiEntityById(poiOnlineEntity1.roomId) } returns flowOf(poiEntity1)

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)
        val success = result[0] as SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success.userEmail).isEqualTo("Poi ${poiOnlineEntity1.roomId} already up-to-date")

        coVerify(exactly = 0) {
            poiRepository.downloadPoiFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedPoiS_allPoiSAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {

        coEvery { poiOnlineRepository.getAllPoiS() } returns poiOnlineEntityList

        poiOnlineEntityList.forEachIndexed { index, onlinePoi ->
            every { poiRepository.getPoiEntityById(onlinePoi.roomId) } returns flowOf(poiEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(poiOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success.userEmail).isEqualTo("Poi ${poiOnlineEntityList[index].roomId} already up-to-date")
        }

        coVerify(exactly = 0) {
            poiRepository.downloadPoiFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {

        val poiOnlineEntity4 = PoiOnlineEntity(
            name = "Monop chez Mounette",
            type = "supermarché",
            updatedAt = 1700000008000 ,
            roomId = 4L
        )

        val poiInsert = poiOnlineEntity1
        val poiUpdate = poiOnlineEntity2.copy(updatedAt = 1700000006000)
        val poiSkip = poiOnlineEntity3
        val poiError = poiOnlineEntity4.copy(updatedAt = 1700000008000)

        val outdatedLocal = poiEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocal = poiEntity3

        val onlinePoiS = listOf(poiInsert, poiUpdate, poiSkip, poiError)

        coEvery { poiOnlineRepository.getAllPoiS() } returns onlinePoiS

        every { poiRepository.getPoiEntityById(poiInsert.roomId) } returns flowOf(null)
        every { poiRepository.getPoiEntityById(poiUpdate.roomId) } returns flowOf(outdatedLocal)
        every { poiRepository.getPoiEntityById(poiSkip.roomId) } returns flowOf(upToDateLocal)
        every { poiRepository.getPoiEntityById(poiError.roomId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(4)

        // Check insertion
        val statusInsert = result[0] as? SyncStatus.Success
        assertThat(statusInsert).isNotNull()
        assertThat(statusInsert!!.userEmail).isEqualTo("Poi ${poiInsert.roomId} downloaded")

        // Check update
        val statusUpdate = result[1] as? SyncStatus.Success
        assertThat(statusUpdate).isNotNull()
        assertThat(statusUpdate!!.userEmail).isEqualTo("Poi ${poiUpdate.roomId} downloaded")

        // Check skip
        val statusSkip = result[2] as? SyncStatus.Success
        assertThat(statusSkip).isNotNull()
        assertThat(statusSkip!!.userEmail).isEqualTo("Poi ${poiSkip.roomId} already up-to-date")

        // Check error
        val statusError = result[3] as? SyncStatus.Failure
        assertThat(statusError).isNotNull()
        assertThat(statusError!!.label).isEqualTo("Poi ${poiError.roomId} failed to sync")
        assertThat(statusError.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 1) { poiRepository.downloadPoiFromFirebase(poiInsert) }
        coVerify(exactly = 1) { poiRepository.downloadPoiFromFirebase(poiUpdate) }
        coVerify(exactly = 0) { poiRepository.downloadPoiFromFirebase(poiSkip) }
        coVerify(exactly = 0) { poiRepository.downloadPoiFromFirebase(poiError) }

    }

    @Test
    fun downloadUnSyncedPoiS_individualFailure_returnsPartialSuccessWithFailure() = runTest {

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(poiOnlineEntity1)

        every { poiRepository.getPoiEntityById(poiOnlineEntity1.roomId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure.label).isEqualTo("Poi ${poiOnlineEntity1.roomId} failed to sync")
        assertThat(failure.error.message).isEqualTo("DB crash")
    }

    @Test
    fun downloadUnSyncedPoiS_globalFailure_returnsFailureStatus() = runTest {

        coEvery { poiOnlineRepository.getAllPoiS() } throws RuntimeException("Firestore down")

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure).isNotNull()
        assertThat(failure.label).isEqualTo("Global POI download failed")
        assertThat(failure.error.message).isEqualTo("Firestore down")
    }

    @Test
    fun downloadUnSyncedPoiS_noPoiSOnline_returnsEmptyList() = runTest {

        coEvery { poiOnlineRepository.getAllPoiS() } returns emptyList()

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            poiRepository.downloadPoiFromFirebase(any())
        }
    }
}