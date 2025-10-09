package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyPoiCrossOnlineEntity
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

class CrossRefDownloadManagerTest {

    private val crossRefRepository = mockk<PropertyPoiCrossRepository>(relaxed = true)
    private val crossRefOnlineRepository = mockk<PropertyPoiCrossOnlineRepository>(relaxed = true)

    private lateinit var downloadManager: PropertyPoiCrossDownloadInterfaceManager

    private val crossRefEntity1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRefEntity2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRefEntity3 = FakePropertyPoiCrossEntity.propertyPoiCross3
    private val crossRefEntityList = FakePropertyPoiCrossEntity.allCrossRefs

    private val crossRefOnlineEntity1 = FakePropertyPoiCrossOnlineEntity.cross1
    private val crossRefOnlineEntity2 = FakePropertyPoiCrossOnlineEntity.cross2
    private val crossRefOnlineEntity3 = FakePropertyPoiCrossOnlineEntity.cross3
    private val crossRefOnlineEntityList = FakePropertyPoiCrossOnlineEntity.propertyPoiCrossOnlineEntityList

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        downloadManager = PropertyPoiCrossDownloadManager(crossRefRepository, crossRefOnlineRepository)

    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedCross_localCrossNull_downloadsAndInsertsCross() = runTest {

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(crossRefOnlineEntity1)

        every { crossRefRepository.getCrossEntityByIds(crossRefOnlineEntity1.propertyId, crossRefOnlineEntity1.poiId) } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("CrossRef (${crossRefOnlineEntity1.propertyId}, ${crossRefOnlineEntity1.poiId}) downloaded")

        coVerify(exactly = 1) {
            crossRefRepository.downloadCrossRefFromFirebase(crossRefOnlineEntity1)
        }
    }

    @Test
    fun downloadUnSyncedCross_allCrossMissingLocally_downloadsAndInsertsAll() = runTest {

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns crossRefOnlineEntityList

        crossRefOnlineEntityList.forEach {
            every { crossRefRepository.getCrossEntityByIds(it.propertyId, it.poiId) } returns flowOf(null)
        }

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(crossRefOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            val expectedPropertyId = crossRefOnlineEntityList[index].propertyId
            val expectedPoiId = crossRefOnlineEntityList[index].poiId
            assertThat(success.userEmail).isEqualTo("CrossRef ($expectedPropertyId, $expectedPoiId) downloaded")
        }

        crossRefOnlineEntityList.forEach {
            coVerify { crossRefRepository.downloadCrossRefFromFirebase(it) }
        }
    }

    @Test
    fun downloadUnSyncedCross_localCrossOutdated_downloadsAndUpdatesCross() = runTest {

        val outdatedLocal = crossRefEntity1.copy(updatedAt = 1700000000000)
        val updatedOnline = crossRefOnlineEntity1.copy(updatedAt = 1700000002000)

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(updatedOnline)
        every { crossRefRepository.getCrossEntityByIds(updatedOnline.propertyId, updatedOnline.poiId) } returns flowOf(outdatedLocal)

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val success = result[0] as SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success.userEmail).isEqualTo("CrossRef (${crossRefOnlineEntity1.propertyId}, ${crossRefOnlineEntity1.poiId}) downloaded")

        coVerify(exactly = 1) {
            crossRefRepository.downloadCrossRefFromFirebase(updatedOnline)
        }
    }


    @Test
    fun downloadUnSyncedCross_allCrossOutdatedLocally_downloadsAndUpdatesAll() = runTest {

        val outdatedLocalsProperties = crossRefEntityList.mapIndexed { index, poi ->
            poi.copy(updatedAt = 1700000000000 + index)
        }

        val newerOnlineProperties = crossRefOnlineEntityList.mapIndexed { index, poi ->
            poi.copy(updatedAt = 1700000000000 + index + 5)
        }

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns newerOnlineProperties

        newerOnlineProperties.forEachIndexed { index, propertyOnline ->
            every { crossRefRepository.getCrossEntityByIds(propertyOnline.propertyId, propertyOnline.poiId) } returns flowOf(outdatedLocalsProperties[index])
        }

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(newerOnlineProperties.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            val expectedPropertyId = newerOnlineProperties[index].propertyId
            val expectedPoiId = newerOnlineProperties[index].poiId
            assertThat(success.userEmail).isEqualTo("CrossRef ($expectedPropertyId, $expectedPoiId) downloaded")
        }

        newerOnlineProperties.forEach {
            coVerify { crossRefRepository.downloadCrossRefFromFirebase(it) }
        }
    }


    @Test
    fun downloadUnSyncedCross_CrossAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(crossRefOnlineEntity1)

        every { crossRefRepository.getCrossEntityByIds(crossRefOnlineEntity1.propertyId, crossRefOnlineEntity1.poiId) } returns flowOf(crossRefEntity1)

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)
        val success = result[0] as SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success.userEmail).isEqualTo("CrossRef (${crossRefOnlineEntity1.propertyId}, ${crossRefOnlineEntity1.poiId}) already up-to-date")

        coVerify(exactly = 0) {
            crossRefRepository.downloadCrossRefFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedCross_allCrossAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns crossRefOnlineEntityList

        crossRefOnlineEntityList.forEachIndexed { index, onlineCrossRef ->
            every { crossRefRepository.getCrossEntityByIds(onlineCrossRef.propertyId, onlineCrossRef.poiId) } returns flowOf(crossRefEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(crossRefOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            val expectedPropertyId = crossRefOnlineEntityList[index].propertyId
            val expectedPoiId = crossRefOnlineEntityList[index].poiId
            assertThat(success.userEmail).isEqualTo("CrossRef ($expectedPropertyId, $expectedPoiId) already up-to-date")
        }

        coVerify(exactly = 0) {
            crossRefRepository.downloadCrossRefFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {

        val crossRefOnlineEntity4 = PropertyPoiCrossOnlineEntity(
            propertyId = 3L,
            poiId = 3L,
            updatedAt = 1700000008000 ,
            roomId = 4L
        )

        val crossRefInsert = crossRefOnlineEntity1
        val crossRefUpdate = crossRefOnlineEntity2.copy(updatedAt = 1700000006000)
        val crossRefSkip = crossRefOnlineEntity3
        val crossRefError = crossRefOnlineEntity4.copy(updatedAt = 1700000008000)

        val outdatedLocal = crossRefEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocal = crossRefEntity3

        val onlineCrossRefs = listOf(crossRefInsert, crossRefUpdate, crossRefSkip, crossRefError)

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns onlineCrossRefs

        every { crossRefRepository.getCrossEntityByIds(crossRefInsert.propertyId, crossRefInsert.poiId) } returns flowOf(null)
        every { crossRefRepository.getCrossEntityByIds(crossRefUpdate.propertyId, crossRefUpdate.poiId) } returns flowOf(outdatedLocal)
        every { crossRefRepository.getCrossEntityByIds(crossRefSkip.propertyId, crossRefSkip.poiId) } returns flowOf(upToDateLocal)
        every { crossRefRepository.getCrossEntityByIds(crossRefError.propertyId, crossRefError.poiId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(4)

        // Check insertion
        val statusInsert = result[0] as? SyncStatus.Success
        assertThat(statusInsert).isNotNull()
        assertThat(statusInsert!!.userEmail).isEqualTo("CrossRef (${crossRefOnlineEntity1.propertyId}, ${crossRefOnlineEntity1.poiId}) downloaded")

        // Check update
        val statusUpdate = result[1] as? SyncStatus.Success
        assertThat(statusUpdate).isNotNull()
        assertThat(statusUpdate!!.userEmail).isEqualTo("CrossRef (${crossRefOnlineEntity2.propertyId}, ${crossRefOnlineEntity2.poiId}) downloaded")

        // Check skip
        val statusSkip = result[2] as? SyncStatus.Success
        assertThat(statusSkip).isNotNull()
        assertThat(statusSkip!!.userEmail).isEqualTo("CrossRef (${crossRefOnlineEntity3.propertyId}, ${crossRefOnlineEntity3.poiId}) already up-to-date")

        // Check error
        val statusError = result[3] as? SyncStatus.Failure
        assertThat(statusError).isNotNull()
        assertThat(statusError!!.label).isEqualTo("CrossRef (${crossRefOnlineEntity4.propertyId}, ${crossRefOnlineEntity4.poiId}) failed to sync")
        assertThat(statusError.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 1) { crossRefRepository.downloadCrossRefFromFirebase(crossRefInsert) }
        coVerify(exactly = 1) { crossRefRepository.downloadCrossRefFromFirebase(crossRefUpdate) }
        coVerify(exactly = 0) { crossRefRepository.downloadCrossRefFromFirebase(crossRefSkip) }
        coVerify(exactly = 0) { crossRefRepository.downloadCrossRefFromFirebase(crossRefError) }

    }

    @Test
    fun downloadUnSyncedPoiS_individualFailure_returnsPartialSuccessWithFailure() = runTest {

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(crossRefOnlineEntity1)

        every { crossRefRepository.getCrossEntityByIds(crossRefOnlineEntity1.propertyId, crossRefOnlineEntity1.poiId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure.label).isEqualTo("CrossRef (${crossRefOnlineEntity1.propertyId}, ${crossRefOnlineEntity1.poiId}) failed to sync")
        assertThat(failure.error.message).isEqualTo("DB crash")
    }

    @Test
    fun downloadUnSyncedPoiS_globalFailure_returnsFailureStatus() = runTest {

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } throws RuntimeException("Firestore down")

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure).isNotNull()
        assertThat(failure.label).isEqualTo("Global cross-reference download failed")
        assertThat(failure.error.message).isEqualTo("Firestore down")
    }

    @Test
    fun downloadUnSyncedPoiS_noPoiSOnline_returnsEmptyList() = runTest {

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns emptyList()

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            crossRefRepository.downloadCrossRefFromFirebase(any())
        }
    }
}