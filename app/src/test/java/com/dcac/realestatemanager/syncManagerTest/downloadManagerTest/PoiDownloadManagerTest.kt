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
    private val poiEntityListNotDeleted = FakePoiEntity.poiEntityListNotDeleted
    private val poiOnlineEntity1 = FakePoiOnlineEntity.poiOnline1
    private val poiOnlineEntity2 = FakePoiOnlineEntity.poiOnline2
    private val poiOnlineEntity3 = FakePoiOnlineEntity.poiOnline3
    private val poiOnlineEntityListNotDeleted = FakePoiOnlineEntity.poiOnlineEntityListNotDeleted
    private val firestorePoiDocument1 = FakePoiOnlineEntity.firestorePoiDocument1
    private val firestorePoiDocument2 = FakePoiOnlineEntity.firestorePoiDocument2
    private val firestorePoiDocument3 = FakePoiOnlineEntity.firestorePoiDocument3

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
        val poiId = poiOnlineEntity1.universalLocalId

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(firestorePoiDocument1)
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiId)
        } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Poi $poiId inserted")

        val insertedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = 1) {
            poiRepository.insertPoiInsertFromFirebase(
                capture(insertedPoiS),
                firestorePoiDocument1.firebaseId
            )
        }

        assertThat(insertedPoiS.first().universalLocalId).isEqualTo(poiId)

        coVerify(exactly = 0 ) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPoiS_allPoiSMissingLocally_downloadsAndInsertsAllExceptDeleted() = runTest {
        val firestoreDocs = listOf(
            firestorePoiDocument1,
            firestorePoiDocument2,
            firestorePoiDocument3
        )

        coEvery { poiOnlineRepository.getAllPoiS() } returns firestoreDocs

        firestoreDocs.forEach { doc ->
            every {
                poiRepository.getPoiByIdIncludeDeleted(doc.poi.universalLocalId)
            } returns flowOf(null)
        }

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(2)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly(
            "Poi ${poiEntity1.id} inserted",
            "Poi ${poiEntity2.id} inserted"
        )

        val insertedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = 2) {
            poiRepository.insertPoiInsertFromFirebase(
                capture(insertedPoiS),
                any()
            )
        }

        val insertedIds = insertedPoiS.map { it.universalLocalId }

        assertThat(insertedIds)
            .containsExactly(poiEntity1.id, poiEntity2.id)

    }

    @Test
    fun downloadUnSyncedPoiS_localPoiOutdated_downloadsAndUpdatesPoi() = runTest {
        val outdatedLocalPoi = poiEntity1.copy(updatedAt = 1700000000000)
        val updatedOnlinePoi = poiOnlineEntity1.copy(updatedAt = 1700000002000)
        val poiId = updatedOnlinePoi.universalLocalId

        val firestoreDoc = firestorePoiDocument1.copy(
            poi = updatedOnlinePoi
        )

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(firestoreDoc)
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiId)
        } returns flowOf(outdatedLocalPoi)

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Poi $poiId updated")

        val updatedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = 1) {
            poiRepository.updatePoiFromFirebase(
                capture(updatedPoiS),
                firestoreDoc.firebaseId
            )
        }

        assertThat(updatedPoiS.first().universalLocalId).isEqualTo(poiId)

        coVerify(exactly = 0) {
            poiRepository.insertPoiInsertFromFirebase(any(), any())
        }
    }


    @Test
    fun downloadUnSyncedPoiS_allPoiSOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        val outdatedLocalPoiS = poiEntityListNotDeleted.mapIndexed { index, poi ->
            poi.copy(updatedAt = 1700000000000 + index)
        }
        val newerOnlinePoiS = poiOnlineEntityListNotDeleted.mapIndexed { index, poi ->
            poi.copy(updatedAt = 1700000000000 + index + 5)
        }
        val baseDocs = listOf(
            firestorePoiDocument1,
            firestorePoiDocument2
        )
        val firestoreDocs = baseDocs.mapIndexed { index, doc ->
            doc.copy(poi = newerOnlinePoiS[index])
        }

        coEvery { poiOnlineRepository.getAllPoiS() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                poiRepository.getPoiByIdIncludeDeleted(doc.poi.universalLocalId)
            } returns flowOf(outdatedLocalPoiS[index])
        }

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "Poi ${it.poi.universalLocalId} updated"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        val updatedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = firestoreDocs.size) {
            poiRepository.updatePoiFromFirebase(
                capture(updatedPoiS),
                any()
            )
        }

        assertThat(updatedPoiS.map { it.universalLocalId })
            .containsExactlyElementsIn(
                firestoreDocs.map { it.poi.universalLocalId }
            )

        coVerify(exactly = 0) {
            poiRepository.insertPoiInsertFromFirebase(any(), any())
        }
    }


    @Test
    fun downloadUnSyncedPoiS_poiAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val poiId = poiOnlineEntity1.universalLocalId
        val firestoreDoc = firestorePoiDocument1

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(firestoreDoc)
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiId)
        } returns flowOf(poiEntity1)

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Poi $poiId already up-to-date")

        coVerify(exactly = 0) {
            poiRepository.insertPoiInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            poiRepository.getPoiByIdIncludeDeleted(poiId)
        }
    }

    @Test
    fun downloadUnSyncedPoiS_allPoiSAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val firestoreDocs = listOf(
            firestorePoiDocument1,
            firestorePoiDocument2
        )

        coEvery { poiOnlineRepository.getAllPoiS() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                poiRepository.getPoiByIdIncludeDeleted(doc.poi.universalLocalId)
            } returns flowOf(poiEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "Poi ${it.poi.universalLocalId} already up-to-date"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        coVerify(exactly = 0) {
            poiRepository.insertPoiInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                poiRepository.getPoiByIdIncludeDeleted(doc.poi.universalLocalId)
            }
        }
    }

    @Test
    fun downloadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {
        val poiInsert = poiOnlineEntity1
        val poiUpdate = poiOnlineEntity2.copy(updatedAt = 1700000006000)
        val poiSkip = poiOnlineEntity3.copy(isDeleted = false)
        val poiError = poiOnlineEntity3.copy(
            universalLocalId = "error_id",
            updatedAt = 1700000008000
        )
        val poiDelete = poiOnlineEntity3

        val outdatedLocalPoi = poiEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocalPoi = poiEntity3.copy(isDeleted = false)
        val localPoiToDelete = poiEntity3.copy(isDeleted = false)

        val firestoreDocs = listOf(
            firestorePoiDocument1.copy(poi = poiInsert),
            firestorePoiDocument2.copy(poi = poiUpdate),
            firestorePoiDocument3.copy(poi = poiSkip),
            firestorePoiDocument3.copy(poi = poiError),
            firestorePoiDocument3.copy(poi = poiDelete)
        )

        coEvery { poiOnlineRepository.getAllPoiS() } returns firestoreDocs
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiInsert.universalLocalId)
        } returns flowOf(null)
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiUpdate.universalLocalId)
        } returns flowOf(outdatedLocalPoi)
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiSkip.universalLocalId)
        } returns flowOf(upToDateLocalPoi)
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiError.universalLocalId)
        } throws RuntimeException("DB fail")
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiDelete.universalLocalId)
        } returns flowOf(localPoiToDelete)

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(5)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "Poi ${poiInsert.universalLocalId} inserted",
            "Poi ${poiUpdate.universalLocalId} updated",
            "Poi ${poiSkip.universalLocalId} already up-to-date",
            "Poi ${poiDelete.universalLocalId} deleted locally (remote deleted)"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label).isEqualTo("Poi ${poiError.universalLocalId}")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB fail")

        val insertedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = 1) {
            poiRepository.insertPoiInsertFromFirebase(
                capture(insertedPoiS),
                any()
            )
        }

        assertThat(insertedPoiS.first().universalLocalId)
            .isEqualTo(poiInsert.universalLocalId)

        val updatedPoiS = mutableListOf<PoiOnlineEntity>()

        coVerify(exactly = 1) {
            poiRepository.updatePoiFromFirebase(
                capture(updatedPoiS),
                any()
            )
        }

        assertThat(updatedPoiS.first().universalLocalId)
            .isEqualTo(poiUpdate.universalLocalId)

        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(poiSkip, any())
        }
        coVerify(exactly = 1) {
            poiRepository.deletePoi(localPoiToDelete)
        }

    }

    @Test
    fun downloadUnSyncedPoiS_individualFailure_returnsPartialSuccessWithFailure() = runTest {
        val poiId = poiOnlineEntity1.universalLocalId
        val firestoreDoc = firestorePoiDocument1

        coEvery { poiOnlineRepository.getAllPoiS() } returns listOf(firestoreDoc)
        every {
            poiRepository.getPoiByIdIncludeDeleted(poiId)
        } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Poi $poiId")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 0) {
            poiRepository.insertPoiInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            poiRepository.getPoiByIdIncludeDeleted(poiId)
        }

    }

    @Test
    fun downloadUnSyncedPoiS_globalFailure_returnsFailureStatus() = runTest {
        coEvery { poiOnlineRepository.getAllPoiS() } throws RuntimeException("Firebase is down")

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Global POI download failed")
        assertThat(failure.error).hasMessageThat().isEqualTo("Firebase is down")

        coVerify(exactly = 1) {
            poiOnlineRepository.getAllPoiS()
        }
        coVerify(exactly = 0) {
            poiRepository.getPoiByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            poiRepository.insertPoiInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPoiS_noPoiSOnline_returnsEmptyList() = runTest {
        coEvery { poiOnlineRepository.getAllPoiS() } returns emptyList()

        val result = downloadManager.downloadUnSyncedPoiS()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            poiOnlineRepository.getAllPoiS()
        }
        coVerify(exactly = 0) {
            poiRepository.getPoiByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            poiRepository.insertPoiInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            poiRepository.updatePoiFromFirebase(any(), any())
        }
    }
}
