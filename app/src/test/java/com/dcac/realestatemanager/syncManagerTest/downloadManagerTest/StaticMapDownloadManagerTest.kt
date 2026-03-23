package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapDownloadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeStaticMapEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeStaticMapOnlineEntity
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

class StaticMapDownloadManagerTest {

    private val staticMapRepository = mockk<StaticMapRepository>(relaxed = true)
    private val staticMapOnlineRepository = mockk<StaticMapOnlineRepository>(relaxed = true)

    private lateinit var downloadManager: StaticMapDownloadInterfaceManager

    private val staticMapEntity1 = FakeStaticMapEntity.staticMap1
    private val staticMapEntity2 = FakeStaticMapEntity.staticMap2
    private val staticMapEntity3 = FakeStaticMapEntity.staticMap3
    private val staticMapEntityList = FakeStaticMapEntity.staticMapEntityList
    private val staticMapEntityListNotDeleted = FakeStaticMapEntity.staticMapEntityListNotDeleted
    private val staticMapOnlineEntity1 = FakeStaticMapOnlineEntity.staticMapOnline1
    private val staticMapOnlineEntity2 = FakeStaticMapOnlineEntity.staticMapOnline2
    private val staticMapOnlineEntity3 = FakeStaticMapOnlineEntity.staticMapOnline3
    private val staticMapOnlineEntityListNotDeleted = FakeStaticMapOnlineEntity.staticMapOnlineEntityListNotDeleted
    private val firestoreStaticMapDocument1 = FakeStaticMapOnlineEntity.firestoreStaticMapDocument1
    private val firestoreStaticMapDocument2 = FakeStaticMapOnlineEntity.firestoreStaticMapDocument2
    private val firestoreStaticMapDocument3 = FakeStaticMapOnlineEntity.firestoreStaticMapDocument3

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        downloadManager = StaticMapDownloadManager(staticMapRepository, staticMapOnlineRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedStaticMaps_localStaticMapNull_downloadsAndInsertsStaticMap() = runTest {
        val staticMapId = staticMapOnlineEntity1.universalLocalId

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns listOf(firestoreStaticMapDocument1)
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapId)
        } returns flowOf(null)
        coEvery {
            staticMapOnlineRepository.downloadImageLocally(staticMapOnlineEntity1.storageUrl)
        } returns "file://mock_download.jpg"

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("StaticMap $staticMapId inserted")

        coVerify(exactly = 1) {
            staticMapOnlineRepository.downloadImageLocally(staticMapOnlineEntity1.storageUrl)
        }

        val insertedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = 1) {
            staticMapRepository.insertStaticMapInsertFromFirebase(
                capture(insertedStaticMaps),
                firestoreStaticMapDocument1.firebaseId,
                "file://mock_download.jpg"
            )
        }

        assertThat(insertedStaticMaps.first().universalLocalId).isEqualTo(staticMapId)

        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_allStaticMapsMissingLocally_downloadsAndInsertsAllExceptDeleted() = runTest {
        val firestoreDocs = listOf(
            firestoreStaticMapDocument1,
            firestoreStaticMapDocument2,
            firestoreStaticMapDocument3
        )

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns firestoreDocs

        firestoreDocs.forEach { doc ->
            every {
                staticMapRepository.getStaticMapByIdIncludeDeleted(doc.staticMap.universalLocalId)
            } returns flowOf(null)

            coEvery {
                staticMapOnlineRepository.downloadImageLocally(doc.staticMap.storageUrl)
            } returns "file://mock_download.jpg"
        }

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(2)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly(
            "StaticMap static-map-1 inserted",
            "StaticMap static-map-2 inserted"
        )

        val insertedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = 2) {
            staticMapRepository.insertStaticMapInsertFromFirebase(
                capture(insertedStaticMaps),
                any(),
                any()
            )
        }

        val insertedIds = insertedStaticMaps.map { it.universalLocalId }

        assertThat(insertedIds)
            .containsExactly("static-map-1", "static-map-2")

        coVerify(exactly = 1) {
            staticMapOnlineRepository.downloadImageLocally(staticMapOnlineEntity1.storageUrl)
        }

        coVerify(exactly = 1) {
            staticMapOnlineRepository.downloadImageLocally(staticMapOnlineEntity2.storageUrl)
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_localStaticMapOutdated_downloadsAndUpdatesStaticMap() = runTest {
        val outdatedLocalStaticMap = staticMapEntity1.copy(updatedAt = 1700000000000)
        val updatedOnlineStaticMap = staticMapOnlineEntity1.copy(updatedAt = 1700000002000)
        val staticMapId = updatedOnlineStaticMap.universalLocalId

        val firestoreDoc = firestoreStaticMapDocument1.copy(
            staticMap = updatedOnlineStaticMap
        )

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns listOf(firestoreDoc)
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapId)
        } returns flowOf(outdatedLocalStaticMap)
        coEvery {
            staticMapOnlineRepository.downloadImageLocally(updatedOnlineStaticMap.storageUrl)
        } returns "file://mock_download.jpg"

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("StaticMap $staticMapId updated")

        coVerify(exactly = 1) {
            staticMapOnlineRepository.downloadImageLocally(updatedOnlineStaticMap.storageUrl)
        }

        val updatedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = 1) {
            staticMapRepository.updateStaticMapFromFirebase(
                capture(updatedStaticMaps),
                firestoreDoc.firebaseId
            )
        }

        assertThat(updatedStaticMaps.first().universalLocalId).isEqualTo(staticMapId)

        coVerify(exactly = 0) {
            staticMapRepository.insertStaticMapInsertFromFirebase(any(), any(), any())
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_allStaticMapsOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        val outdatedLocalStaticMaps = staticMapEntityListNotDeleted.mapIndexed { index, staticMap ->
            staticMap.copy(updatedAt = 1700000000000 + index)
        }

        val newerOnlineStaticMaps = staticMapOnlineEntityListNotDeleted.mapIndexed { index, staticMap ->
            staticMap.copy(updatedAt = 1700000000000 + index + 5)
        }

        val baseDocs = listOf(
            firestoreStaticMapDocument1,
            firestoreStaticMapDocument2
        )

        val firestoreDocs = baseDocs.mapIndexed { index, doc ->
            doc.copy(staticMap = newerOnlineStaticMaps[index])
        }

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                staticMapRepository.getStaticMapByIdIncludeDeleted(doc.staticMap.universalLocalId)
            } returns flowOf(outdatedLocalStaticMaps[index])
            coEvery {
                staticMapOnlineRepository.downloadImageLocally(doc.staticMap.storageUrl)
            } returns "file://mock_download.jpg"
        }

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "StaticMap ${it.staticMap.universalLocalId} updated"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        val updatedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = firestoreDocs.size) {
            staticMapRepository.updateStaticMapFromFirebase(
                capture(updatedStaticMaps),
                any()
            )
        }

        assertThat(updatedStaticMaps.map { it.universalLocalId })
            .containsExactlyElementsIn(
                firestoreDocs.map { it.staticMap.universalLocalId }
            )

        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                staticMapOnlineRepository.downloadImageLocally(doc.staticMap.storageUrl)
            }
        }
        coVerify(exactly = 0) {
            staticMapRepository.insertStaticMapInsertFromFirebase(any(), any(), any())
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_staticMapAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val staticMapId = staticMapOnlineEntity1.universalLocalId
        val firestoreDoc = firestoreStaticMapDocument1

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns listOf(firestoreDoc)
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapId)
        } returns flowOf(staticMapEntity1)

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("StaticMap $staticMapId already up-to-date")

        coVerify(exactly = 0) {
            staticMapOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.insertStaticMapInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapId)
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_allStaticMapsAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val firestoreDocs = listOf(
            firestoreStaticMapDocument1,
            firestoreStaticMapDocument2
        )

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                staticMapRepository.getStaticMapByIdIncludeDeleted(doc.staticMap.universalLocalId)
            } returns flowOf(staticMapEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "StaticMap ${it.staticMap.universalLocalId} already up-to-date"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        coVerify(exactly = 0) {
            staticMapOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.insertStaticMapInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                staticMapRepository.getStaticMapByIdIncludeDeleted(doc.staticMap.universalLocalId)
            }
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_mixedCases_returnsCorrectStatuses() = runTest {
        val staticMapInsert = staticMapOnlineEntity1
        val staticMapUpdate = staticMapOnlineEntity2.copy(updatedAt = 1700000006000)
        val staticMapSkip = staticMapOnlineEntity3.copy(isDeleted = false)
        val staticMapError = staticMapOnlineEntity3.copy(
            universalLocalId = "error_id",
            updatedAt = 1700000008000
        )
        val staticMapDelete = staticMapOnlineEntity3

        val outdatedLocalStaticMap = staticMapEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocalStaticMap = staticMapEntity3.copy(isDeleted = false)
        val localStaticMapToDelete = staticMapEntity3.copy(isDeleted = false)

        val firestoreDocs = listOf(
            firestoreStaticMapDocument1.copy(staticMap = staticMapInsert),
            firestoreStaticMapDocument2.copy(staticMap = staticMapUpdate),
            firestoreStaticMapDocument3.copy(staticMap = staticMapSkip),
            firestoreStaticMapDocument3.copy(staticMap = staticMapError),
            firestoreStaticMapDocument3.copy(staticMap = staticMapDelete)
        )

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns firestoreDocs
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapInsert.universalLocalId)
        } returns flowOf(null)
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapUpdate.universalLocalId)
        } returns flowOf(outdatedLocalStaticMap)
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapSkip.universalLocalId)
        } returns flowOf(upToDateLocalStaticMap)
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapError.universalLocalId)
        } throws RuntimeException("DB fail")
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapDelete.universalLocalId)
        } returns flowOf(localStaticMapToDelete)
        coEvery {
            staticMapOnlineRepository.downloadImageLocally(any())
        } returns "file://mock_download.jpg"

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(5)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "StaticMap ${staticMapInsert.universalLocalId} inserted",
            "StaticMap ${staticMapUpdate.universalLocalId} updated",
            "StaticMap ${staticMapSkip.universalLocalId} already up-to-date",
            "StaticMap ${staticMapDelete.universalLocalId} deleted locally (remote deleted)"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label).isEqualTo("StaticMap ${staticMapError.universalLocalId}")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB fail")

        val insertedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = 1) {
            staticMapRepository.insertStaticMapInsertFromFirebase(
                capture(insertedStaticMaps),
                any(),
                "file://mock_download.jpg"
            )
        }

        assertThat(insertedStaticMaps.first().universalLocalId)
            .isEqualTo(staticMapInsert.universalLocalId)

        val updatedStaticMaps = mutableListOf<StaticMapOnlineEntity>()

        coVerify(exactly = 1) {
            staticMapRepository.updateStaticMapFromFirebase(
                capture(updatedStaticMaps),
                any()
            )
        }

        assertThat(updatedStaticMaps.first().universalLocalId)
            .isEqualTo(staticMapUpdate.universalLocalId)

        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(staticMapSkip, any())
        }
        coVerify(exactly = 1) {
            staticMapRepository.deleteStaticMap(localStaticMapToDelete)
        }
        coVerify(exactly = 2) {
            staticMapOnlineRepository.downloadImageLocally(any())
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_individualFailure_returnsPartialSuccessWithFailure() = runTest {
        val staticMapId = staticMapOnlineEntity1.universalLocalId
        val firestoreDoc = firestoreStaticMapDocument1

        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns listOf(firestoreDoc)
        every {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapId)
        }  throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("StaticMap $staticMapId")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 0) {
            staticMapOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.insertStaticMapInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            staticMapRepository.getStaticMapByIdIncludeDeleted(staticMapId)
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_globalFailure_returnsFailureStatus() = runTest {
        coEvery { staticMapOnlineRepository.getAllStaticMaps() } throws RuntimeException("Firebase is down")

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("StaticMap download (global failure)")
        assertThat(failure.error).hasMessageThat().isEqualTo("Firebase is down")

        coVerify(exactly = 1) {
            staticMapOnlineRepository.getAllStaticMaps()
        }
        coVerify(exactly = 0) {
            staticMapRepository.getStaticMapByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            staticMapOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.insertStaticMapInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedStaticMaps_noStaticMapsOnline_returnsEmptyList() = runTest {
        coEvery { staticMapOnlineRepository.getAllStaticMaps() } returns emptyList()

        val result = downloadManager.downloadUnSyncedStaticMaps()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            staticMapOnlineRepository.getAllStaticMaps()
        }
        coVerify(exactly = 0) {
            staticMapRepository.getStaticMapByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.insertStaticMapInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            staticMapRepository.updateStaticMapFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            staticMapOnlineRepository.downloadImageLocally(any())
        }
    }




}
