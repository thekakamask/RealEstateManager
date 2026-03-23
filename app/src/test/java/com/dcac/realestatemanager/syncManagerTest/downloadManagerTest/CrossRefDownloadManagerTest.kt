package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
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
    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val poiRepository = mockk<PoiRepository>(relaxed = true)

    private lateinit var downloadManager: PropertyPoiCrossDownloadInterfaceManager

    private val crossRefEntity1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRefEntity2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRefEntity5 = FakePropertyPoiCrossEntity.propertyPoiCross5
    private val crossRefEntityList = FakePropertyPoiCrossEntity.allCrossRefs
    private val crossRefEntityListNotDeleted = FakePropertyPoiCrossEntity.allCrossRefsNotDeleted
    private val crossRefOnlineEntity1 = FakePropertyPoiCrossOnlineEntity.crossOnline1
    private val crossRefOnlineEntity2 = FakePropertyPoiCrossOnlineEntity.crossOnline2
    private val crossRefOnlineEntity5 = FakePropertyPoiCrossOnlineEntity.crossOnline5
    private val crossRefOnlineEntityListNotDeleted = FakePropertyPoiCrossOnlineEntity.propertyPoiCrossOnlineEntityListNotDeleted
    private val firestoreCrossRefDocument1 = FakePropertyPoiCrossOnlineEntity.firestoreCrossDocument1
    private val firestoreCrossRefDocument2 = FakePropertyPoiCrossOnlineEntity.firestoreCrossDocument2
    private val firestoreCrossRefDocument5 = FakePropertyPoiCrossOnlineEntity.firestoreCrossDocument5

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        downloadManager = PropertyPoiCrossDownloadManager(
            crossRefRepository,
            crossRefOnlineRepository,
            propertyRepository,
            poiRepository
        )

    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedCross_localCrossNull_downloadsAndInsertsCross() = runTest {
        val crossRefPropertyId = crossRefOnlineEntity1.universalLocalPropertyId
        val crossRefPoiId = crossRefOnlineEntity1.universalLocalPoiId
        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(firestoreCrossRefDocument1)
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefPropertyId, crossRefPoiId)
        } returns flowOf(null)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        } returns flowOf(null)
        every {
            poiRepository.getPoiByIdIncludeDeleted(any())
        } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("CrossRef ${firestoreCrossRefDocument1.firebaseId} inserted")

        val insertedCrossRefs = mutableListOf<PropertyPoiCrossOnlineEntity>()

        coVerify(exactly = 1) {
            crossRefRepository.insertCrossRefInsertFromFirebase(
                capture(insertedCrossRefs),
                firestoreCrossRefDocument1.firebaseId
            )
        }

        assertThat(insertedCrossRefs.first().universalLocalPropertyId).isEqualTo(crossRefPropertyId)
        assertThat(insertedCrossRefs.first().universalLocalPoiId).isEqualTo(crossRefPoiId)

        coVerify(exactly = 0 ) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedCross_allCrossMissingLocally_downloadsAndInsertsAllExceptDeleted() = runTest {
        val firestoreDocs = listOf(
            firestoreCrossRefDocument1,
            firestoreCrossRefDocument2,
            firestoreCrossRefDocument5
        )

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns firestoreDocs

        firestoreDocs.forEach { doc ->
            every {
                crossRefRepository.getCrossRefsByIdsIncludedDeleted(doc.cross.universalLocalPropertyId, doc.cross.universalLocalPoiId)
            } returns flowOf(null)
            every {
                propertyRepository.getPropertyByIdIncludeDeleted(any())
            } returns flowOf(null)
            every {
                poiRepository.getPoiByIdIncludeDeleted(any())
            } returns flowOf(null)
        }



        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(2)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly(
            "CrossRef ${crossRefEntity1.firestoreDocumentId} inserted",
            "CrossRef ${crossRefEntity2.firestoreDocumentId} inserted"
        )

        val insertedCrossRefs = mutableListOf<PropertyPoiCrossOnlineEntity>()

        coVerify(exactly = 2) {
            crossRefRepository.insertCrossRefInsertFromFirebase(
                capture(insertedCrossRefs),
                any()
            )
        }

        val insertedIds = insertedCrossRefs.map { it.universalLocalPropertyId to it.universalLocalPoiId }

        assertThat(insertedIds)
            .containsExactly(
                crossRefEntity1.universalLocalPropertyId to crossRefEntity1.universalLocalPoiId,
                crossRefEntity2.universalLocalPropertyId to crossRefEntity2.universalLocalPoiId
            )
    }

    @Test
    fun downloadUnSyncedCross_localCrossOutdated_downloadsAndUpdatesCross() = runTest {
        val outdatedLocalCrossRef = crossRefEntity1.copy(updatedAt = 1700000000000)
        val updatedOnlineCrossRef = crossRefOnlineEntity1.copy(updatedAt = 1700000002000)
        val crossRefPropertyId = updatedOnlineCrossRef.universalLocalPropertyId
        val crossRefPoiId = updatedOnlineCrossRef.universalLocalPoiId

        val firestoreDoc = firestoreCrossRefDocument1.copy(
            cross = updatedOnlineCrossRef
        )

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(firestoreDoc)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        } returns flowOf(null)
        every {
            poiRepository.getPoiByIdIncludeDeleted(any())
        } returns flowOf(null)
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefPropertyId, crossRefPoiId)
        } returns flowOf(outdatedLocalCrossRef)

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("CrossRef ${firestoreDoc.firebaseId} updated")

        val updatedCrossRefs = mutableListOf<PropertyPoiCrossOnlineEntity>()

        coVerify(exactly = 1) {
            crossRefRepository.updateCrossRefFromFirebase(
                capture(updatedCrossRefs),
                firestoreDoc.firebaseId
            )
        }

        assertThat(updatedCrossRefs.first().universalLocalPropertyId).isEqualTo(crossRefPropertyId)
        assertThat(updatedCrossRefs.first().universalLocalPoiId).isEqualTo(crossRefPoiId)

        coVerify(exactly = 0) {
            crossRefRepository.insertCrossRefInsertFromFirebase(any(), any())
        }
    }


    @Test
    fun downloadUnSyncedCross_allCrossOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        val outdatedLocalCrossRefs = crossRefEntityListNotDeleted.mapIndexed { index, cross ->
            cross.copy(updatedAt = 1700000000000 + index)
        }
        val newerOnlineCrossRefs = crossRefOnlineEntityListNotDeleted.mapIndexed { index, cross ->
            cross.copy(updatedAt = 1700000000000 + index + 5)
        }
        val baseDocs = listOf(
            firestoreCrossRefDocument1,
            firestoreCrossRefDocument2
        )
        val firestoreDocs = baseDocs.mapIndexed { index, doc ->
            doc.copy(cross = newerOnlineCrossRefs[index])
        }

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns firestoreDocs
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        } returns flowOf(null)
        every {
            poiRepository.getPoiByIdIncludeDeleted(any())
        } returns flowOf(null)

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                crossRefRepository.getCrossRefsByIdsIncludedDeleted(doc.cross.universalLocalPropertyId, doc.cross.universalLocalPoiId)
            } returns flowOf(outdatedLocalCrossRefs[index])
        }

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "CrossRef ${it.firebaseId} updated"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        val updatedCrossRefs = mutableListOf<PropertyPoiCrossOnlineEntity>()

        coVerify(exactly = firestoreDocs.size) {
            crossRefRepository.updateCrossRefFromFirebase(
                capture(updatedCrossRefs),
                any()
            )
        }

        assertThat(updatedCrossRefs.map { it.universalLocalPropertyId to it.universalLocalPoiId })
            .containsExactlyElementsIn(
                firestoreDocs.map { it.cross.universalLocalPropertyId to it.cross.universalLocalPoiId }
            )

        coVerify(exactly = 0) {
            crossRefRepository.insertCrossRefInsertFromFirebase(any(), any())
        }
    }


    @Test
    fun downloadUnSyncedCross_CrossAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val crossRefPropertyId = crossRefOnlineEntity1.universalLocalPropertyId
        val crossRefPoiId = crossRefOnlineEntity1.universalLocalPoiId
        val firestoreDoc = firestoreCrossRefDocument1

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(firestoreDoc)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        } returns flowOf(null)
        every {
            poiRepository.getPoiByIdIncludeDeleted(any())
        } returns flowOf(null)
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefPropertyId, crossRefPoiId)
        } returns flowOf(crossRefEntity1)

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("CrossRef ${firestoreDoc.firebaseId} already up-to-date")

        coVerify(exactly = 0) {
            crossRefRepository.insertCrossRefInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefPropertyId, crossRefPoiId)
        }
    }

    @Test
    fun downloadUnSyncedCross_allCrossAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val firestoreDocs = listOf(
            firestoreCrossRefDocument1,
            firestoreCrossRefDocument2
        )

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns firestoreDocs
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        } returns flowOf(null)
        every {
            poiRepository.getPoiByIdIncludeDeleted(any())
        } returns flowOf(null)

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                crossRefRepository.getCrossRefsByIdsIncludedDeleted(doc.cross.universalLocalPropertyId, doc.cross.universalLocalPoiId)
            } returns flowOf(crossRefEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "CrossRef ${it.firebaseId} already up-to-date"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        coVerify(exactly = 0) {
            crossRefRepository.insertCrossRefInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                crossRefRepository.getCrossRefsByIdsIncludedDeleted(doc.cross.universalLocalPropertyId, doc.cross.universalLocalPoiId)
            }
        }
    }

    @Test
    fun downloadUnSyncedCross_mixedCases_returnsCorrectStatuses() = runTest {
        val crossRefInsert = crossRefOnlineEntity1
        val crossRefUpdate = crossRefOnlineEntity2.copy(updatedAt = 1700000006000)
        val crossRefSkip = crossRefOnlineEntity5.copy(isDeleted = false)
        val crossRefError = crossRefOnlineEntity5.copy(
            universalLocalPropertyId = "error_id",
            updatedAt = 1700000008000
        )
        val crossRefDelete = crossRefOnlineEntity5

        val outdatedLocalCrossRef = crossRefEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocalCrossRef = crossRefEntity5.copy(isDeleted = false)
        val localCrossRefToDelete = crossRefEntity5.copy(isDeleted = false)

        val firestoreDocs = listOf(
            firestoreCrossRefDocument1.copy(cross = crossRefInsert),
            firestoreCrossRefDocument2.copy(cross = crossRefUpdate),
            firestoreCrossRefDocument5.copy(cross = crossRefSkip),
            firestoreCrossRefDocument5.copy(cross = crossRefError),
            firestoreCrossRefDocument5.copy(cross = crossRefDelete)
        )

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns firestoreDocs
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefInsert.universalLocalPropertyId, crossRefInsert.universalLocalPoiId)
        } returns flowOf(null)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        } returns flowOf(
            FakePropertyEntity.property1.copy(isDeleted = false)
        )
        every {
            poiRepository.getPoiByIdIncludeDeleted(any())
        } returns flowOf(
            FakePoiEntity.poi1.copy(isDeleted = false)
        )
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefUpdate.universalLocalPropertyId, crossRefUpdate.universalLocalPoiId)
        } returns flowOf(outdatedLocalCrossRef)
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefSkip.universalLocalPropertyId, crossRefSkip.universalLocalPoiId)
        } returns flowOf(upToDateLocalCrossRef)
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefError.universalLocalPropertyId, crossRefError.universalLocalPoiId)
        } throws RuntimeException("DB fail")
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefDelete.universalLocalPropertyId, crossRefDelete.universalLocalPoiId)
        } returns flowOf(localCrossRefToDelete)

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(5)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "CrossRef ${firestoreCrossRefDocument1.firebaseId} inserted",
            "CrossRef ${firestoreCrossRefDocument2.firebaseId} updated",
            "CrossRef ${firestoreCrossRefDocument5.firebaseId} already up-to-date",
            "CrossRef ${firestoreCrossRefDocument5.firebaseId} deleted locally (remote deleted)"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label)
            .isEqualTo("CrossRef (${crossRefError.universalLocalPropertyId}-${crossRefError.universalLocalPoiId})")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB fail")

        val insertedCrossRefs = mutableListOf<PropertyPoiCrossOnlineEntity>()

        coVerify(exactly = 1) {
            crossRefRepository.insertCrossRefInsertFromFirebase(
                capture(insertedCrossRefs),
                any()
            )
        }

        assertThat(insertedCrossRefs.first().universalLocalPropertyId)
            .isEqualTo(crossRefInsert.universalLocalPropertyId)
        assertThat(insertedCrossRefs.first().universalLocalPoiId)
            .isEqualTo(crossRefInsert.universalLocalPoiId)

        val updatedCrossRefs = mutableListOf<PropertyPoiCrossOnlineEntity>()

        coVerify(exactly = 1) {
            crossRefRepository.updateCrossRefFromFirebase(
                capture(updatedCrossRefs),
                any()
            )
        }

        assertThat(updatedCrossRefs.first().universalLocalPropertyId)
            .isEqualTo(crossRefUpdate.universalLocalPropertyId)
        assertThat(updatedCrossRefs.first().universalLocalPoiId)
            .isEqualTo(crossRefUpdate.universalLocalPoiId)

        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(crossRefSkip, any())
        }
        coVerify(exactly = 1) {
            crossRefRepository.deleteCrossRef(localCrossRefToDelete)
        }
    }

    @Test
    fun downloadUnSyncedCross_individualFailure_returnsPartialSuccessWithFailure() = runTest {
        val crossRefPropertyId = crossRefOnlineEntity1.universalLocalPropertyId
        val crossRefPoiId = crossRefOnlineEntity1.universalLocalPoiId

        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns listOf(firestoreCrossRefDocument1)
        every {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefPropertyId, crossRefPoiId)
        } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label).isEqualTo("CrossRef ($crossRefPropertyId-$crossRefPoiId)")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 0){
            crossRefRepository.insertCrossRefInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(crossRefPropertyId, crossRefPoiId)
        }
    }

    @Test
    fun downloadUnSyncedCross_globalFailure_returnsFailureStatus() = runTest {
        coEvery { crossRefOnlineRepository.getAllCrossRefs() } throws RuntimeException("Firebase is down")

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label).isEqualTo("Global CrossRef download failed")
        assertThat(failure.error).hasMessageThat().isEqualTo("Firebase is down")

        coVerify(exactly = 0){
            crossRefRepository.getAllCrossRefs()
        }
        coVerify(exactly = 0){
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(any(), any())
        }
        coVerify(exactly = 0){
            crossRefRepository.insertCrossRefInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedCross_noCrossOnline_returnsEmptyList() = runTest {
        coEvery { crossRefOnlineRepository.getAllCrossRefs() } returns emptyList()

        val result = downloadManager.downloadUnSyncedPropertyPoiCross()

        assertThat(result).isEmpty()

        coVerify(exactly = 1){
            crossRefOnlineRepository.getAllCrossRefs()
        }
        coVerify(exactly = 0){
            crossRefRepository.getCrossRefsByIdsIncludedDeleted(any(), any())
        }
        coVerify(exactly = 0){
            crossRefRepository.insertCrossRefInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            crossRefRepository.updateCrossRefFromFirebase(any(), any())
        }
    }
}
