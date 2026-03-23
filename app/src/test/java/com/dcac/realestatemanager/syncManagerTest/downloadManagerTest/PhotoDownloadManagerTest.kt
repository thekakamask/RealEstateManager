package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePhotoOnlineEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PhotoDownloadManagerTest {

    private val photoRepository = mockk<PhotoRepository>(relaxed = true)
    private val photoOnlineRepository = mockk<PhotoOnlineRepository>(relaxed = true)

    private lateinit var downloadManager: PhotoDownloadInterfaceManager

    private val photoEntity1 = FakePhotoEntity.photo1
    private val photoEntity2 = FakePhotoEntity.photo2
    private val photoEntity3 = FakePhotoEntity.photo3
    private val photoEntityList = FakePhotoEntity.photoEntityList
    private val photoEntityListNotDeleted = FakePhotoEntity.photoEntityListNotDeleted
    private val photoOnlineEntity1 = FakePhotoOnlineEntity.photoOnline1
    private val photoOnlineEntity2 = FakePhotoOnlineEntity.photoOnline2
    private val photoOnlineEntity3 = FakePhotoOnlineEntity.photoOnline3
    private val photoOnlineEntityListNotDeleted = FakePhotoOnlineEntity.photoOnlineEntityListNotDeleted
    private val firestorePhotoDocument1 = FakePhotoOnlineEntity.firestorePhotoDocument1
    private val firestorePhotoDocument2 = FakePhotoOnlineEntity.firestorePhotoDocument2
    private val firestorePhotoDocument3 = FakePhotoOnlineEntity.firestorePhotoDocument3

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        downloadManager = PhotoDownloadManager(photoRepository, photoOnlineRepository)

    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedPhotos_localPhotoNull_downloadsAndInsertsPhoto() = runTest {
        val photoId = photoOnlineEntity1.universalLocalId

        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(firestorePhotoDocument1)
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoId)
        } returns flowOf(null)
        coEvery {
            photoOnlineRepository.downloadImageLocally(photoOnlineEntity1.storageUrl)
        } returns "file://mock_download.jpg"

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Photo $photoId inserted")

        coVerify(exactly = 1) {
            photoOnlineRepository.downloadImageLocally(photoOnlineEntity1.storageUrl)
        }

        val insertedPhotos = mutableListOf<PhotoOnlineEntity>()

        coVerify(exactly = 1) {
            photoRepository.insertPhotoInsertFromFirebase(
                capture(insertedPhotos),
                firestorePhotoDocument1.firebaseId,
                "file://mock_download.jpg"
            )
        }

        assertThat(insertedPhotos.first().universalLocalId).isEqualTo(photoId)

        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPhotos_allPhotosMissingLocally_downloadsAndInsertsAllExceptDeleted() = runTest {
        val firestoreDocs = listOf(
            firestorePhotoDocument1,
            firestorePhotoDocument2,
            firestorePhotoDocument3
        )

        coEvery { photoOnlineRepository.getAllPhotos() } returns firestoreDocs
        // For each remote photo
        firestoreDocs.forEach { doc ->
            // Simulate that no local photo exists (all photos are missing locally)
            every {
                photoRepository.getPhotoByIdIncludeDeleted(doc.photo.universalLocalId)
            } returns flowOf(null)
            // Mock the download to always return a fake local URI
            coEvery {
                photoOnlineRepository.downloadImageLocally(doc.photo.storageUrl)
            } returns "file://mock_download.jpg"
        }

        // Execute the method under test
        val result = downloadManager.downloadUnSyncedPhotos()
        // Verify that only 2 results are returned (the deleted photo is skipped)
        assertThat(result).hasSize(2)
        // Extract success messages from results
        val messages = result.map { (it as SyncStatus.Success).message }
        // Verify that only non-deleted photos were inserted
        assertThat(messages).containsExactly(
            "Photo photo-1 inserted",
            "Photo photo-2 inserted"
        )
        // Capture inserted photos to verify which ones were actually inserted
        val insertedPhotos = mutableListOf<PhotoOnlineEntity>()
        // Verify that exactly 2 insert operations were performed
        coVerify(exactly = 2) {
            photoRepository.insertPhotoInsertFromFirebase(
                capture(insertedPhotos), // capture inserted entities
                any(),                   // firestoreId (not validated here)
                any()                    // localUri (not validated here)
            )
        }

        val insertedIds = insertedPhotos.map { it.universalLocalId }
        // Verify that only expected photo IDs were inserted
        assertThat(insertedIds)
            .containsExactly("photo-1", "photo-2")
        // Verify that download was triggered only for non-deleted photos
        // Photo 1 should be downloaded once
        coVerify(exactly = 1) {
            photoOnlineRepository.downloadImageLocally(photoOnlineEntity1.storageUrl)
        }
        // Photo 2 should be downloaded once
        coVerify(exactly = 1) {
            photoOnlineRepository.downloadImageLocally(photoOnlineEntity2.storageUrl)
        }
    }

    @Test
    fun downloadUnSyncedPhotos_localPhotoOutdated_downloadsAndUpdatesPhoto() = runTest {
        val outdatedLocalPhoto = photoEntity1.copy(updatedAt = 1700000000000)
        val updatedOnlinePhoto = photoOnlineEntity1.copy(updatedAt = 1700000002000)
        val photoId = updatedOnlinePhoto.universalLocalId

        val firestoreDoc = firestorePhotoDocument1.copy(
            photo = updatedOnlinePhoto
        )

        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(firestoreDoc)
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoId)
        } returns flowOf(outdatedLocalPhoto)
        coEvery {
            photoOnlineRepository.downloadImageLocally(updatedOnlinePhoto.storageUrl)
        } returns "file://mock_download.jpg"

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Photo $photoId updated")

        coVerify(exactly = 1) {
            photoOnlineRepository.downloadImageLocally(updatedOnlinePhoto.storageUrl)
        }

        val updatedPhotos = mutableListOf<PhotoOnlineEntity>()

        coVerify(exactly = 1) {
            photoRepository.updatePhotoFromFirebase(
                capture(updatedPhotos),
                firestoreDoc.firebaseId
            )
        }

        assertThat(updatedPhotos.first().universalLocalId).isEqualTo(photoId)

        coVerify(exactly = 0) {
            photoRepository.insertPhotoInsertFromFirebase(any(), any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPhotos_allPhotosOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        val outdatedLocalPhotos = photoEntityListNotDeleted.mapIndexed { index, photo ->
            photo.copy(updatedAt = 1700000000000 + index)
        }
        val newerOnlinePhotos = photoOnlineEntityListNotDeleted.mapIndexed { index, photo ->
            photo.copy(updatedAt = 1700000000000 + index + 5)
        }
        val baseDocs = listOf(
            firestorePhotoDocument1,
            firestorePhotoDocument2
        )
        val firestoreDocs = baseDocs.mapIndexed { index, doc ->
            doc.copy(photo = newerOnlinePhotos[index])
        }

        coEvery { photoOnlineRepository.getAllPhotos() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                photoRepository.getPhotoByIdIncludeDeleted(doc.photo.universalLocalId)
            } returns flowOf(outdatedLocalPhotos[index])
            coEvery {
                photoOnlineRepository.downloadImageLocally(doc.photo.storageUrl)
            } returns "file://mock_download.jpg"
        }

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "Photo ${it.photo.universalLocalId} updated"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        val updatedPhotos = mutableListOf<PhotoOnlineEntity>()

        coVerify(exactly = firestoreDocs.size) {
            photoRepository.updatePhotoFromFirebase(
                capture(updatedPhotos),
                any()
            )
        }

        assertThat(updatedPhotos.map { it.universalLocalId })
            .containsExactlyElementsIn(
                firestoreDocs.map { it.photo.universalLocalId }
            )

        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                photoOnlineRepository.downloadImageLocally(doc.photo.storageUrl)
            }
        }
        coVerify(exactly = 0) {
            photoRepository.insertPhotoInsertFromFirebase(any(), any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPhotos_photoAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val photoId = photoOnlineEntity1.universalLocalId
        val firestoreDoc = firestorePhotoDocument1

        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(firestoreDoc)
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoId)
        } returns flowOf(photoEntity1)

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Photo $photoId already up-to-date")

        coVerify(exactly = 0) {
            photoOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            photoRepository.insertPhotoInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            photoRepository.getPhotoByIdIncludeDeleted(photoId)
        }
    }

    @Test
    fun downloadUnSyncedPhotos_allPhotosAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val firestoreDocs = listOf(
            firestorePhotoDocument1,
            firestorePhotoDocument2
        )

        coEvery { photoOnlineRepository.getAllPhotos() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                photoRepository.getPhotoByIdIncludeDeleted(doc.photo.universalLocalId)
            } returns flowOf(photoEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "Photo ${it.photo.universalLocalId} already up-to-date"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        coVerify(exactly = 0) {
            photoOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            photoRepository.insertPhotoInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }
        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                photoRepository.getPhotoByIdIncludeDeleted(doc.photo.universalLocalId)
            }
        }
    }

    @Test
    fun downloadUnSyncedPhotos_mixedCases_returnsCorrectStatuses() = runTest {
        val photoInsert = photoOnlineEntity1
        val photoUpdate = photoOnlineEntity2.copy(updatedAt = 1700000006000)
        val photoSkip = photoOnlineEntity3.copy(isDeleted = false)
        val photoError = photoOnlineEntity3.copy(
            universalLocalId = "error_id",
            updatedAt = 1700000008000
        )
        val photoDelete = photoOnlineEntity3

        val outdatedLocalPhoto = photoEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocalPhoto = photoEntity3.copy(isDeleted = false)
        val localPhotoToDelete = photoEntity3.copy(isDeleted = false)

        val firestoreDocs = listOf(
            firestorePhotoDocument1.copy(photo = photoInsert),
            firestorePhotoDocument2.copy(photo = photoUpdate),
            firestorePhotoDocument3.copy(photo = photoSkip),
            firestorePhotoDocument3.copy(photo = photoError),
            firestorePhotoDocument3.copy(photo = photoDelete)
        )

        coEvery { photoOnlineRepository.getAllPhotos() } returns firestoreDocs
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoInsert.universalLocalId)
        } returns flowOf(null)
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoUpdate.universalLocalId)
        } returns flowOf(outdatedLocalPhoto)
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoSkip.universalLocalId)
        } returns flowOf(upToDateLocalPhoto)
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoError.universalLocalId)
        } throws RuntimeException("DB fail")
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoDelete.universalLocalId)
        } returns flowOf(localPhotoToDelete)
        coEvery {
            photoOnlineRepository.downloadImageLocally(any())
        } returns "file://mock_download.jpg"

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(5)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "Photo ${photoInsert.universalLocalId} inserted",
            "Photo ${photoUpdate.universalLocalId} updated",
            "Photo ${photoSkip.universalLocalId} already up-to-date",
            "Photo ${photoDelete.universalLocalId} deleted locally (remote deleted)"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label).isEqualTo("Photo ${photoError.universalLocalId}")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB fail")

        val insertedPhotos = mutableListOf<PhotoOnlineEntity>()

        coVerify(exactly = 1) {
            photoRepository.insertPhotoInsertFromFirebase(
                capture(insertedPhotos),
                any(),
                "file://mock_download.jpg"
            )
        }

        assertThat(insertedPhotos.first().universalLocalId)
            .isEqualTo(photoInsert.universalLocalId)

        val updatedPhotos = mutableListOf<PhotoOnlineEntity>()

        coVerify(exactly = 1) {
            photoRepository.updatePhotoFromFirebase(
                capture(updatedPhotos),
                any()
            )
        }

        assertThat(updatedPhotos.first().universalLocalId)
            .isEqualTo(photoUpdate.universalLocalId)

        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(photoSkip, any())
        }
        coVerify(exactly = 1) {
            photoRepository.deletePhoto(localPhotoToDelete)
        }
        coVerify(exactly = 2) {
            photoOnlineRepository.downloadImageLocally(any())
        }
    }


    @Test
    fun downloadUnSyncedPhotos_individualFailure_returnsPartialSuccessWithFailure() = runTest {
        val photoId = photoOnlineEntity1.universalLocalId
        val firestoreDoc = firestorePhotoDocument1

        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(firestoreDoc)
        every {
            photoRepository.getPhotoByIdIncludeDeleted(photoId)
        } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Photo $photoId")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 0) {
            photoOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            photoRepository.insertPhotoInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            photoRepository.getPhotoByIdIncludeDeleted(photoId)
        }
    }

    @Test
    fun downloadUnSyncedPhotos_globalFailure_returnsFailureStatus() = runTest {
        coEvery { photoOnlineRepository.getAllPhotos() } throws RuntimeException("Firebase is down")

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Photo download (global failure)")
        assertThat(failure.error).hasMessageThat().isEqualTo("Firebase is down")

        coVerify(exactly = 1) {
            photoOnlineRepository.getAllPhotos()
        }
        coVerify(exactly = 0) {
            photoRepository.getPhotoByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            photoOnlineRepository.downloadImageLocally(any())
        }
        coVerify(exactly = 0) {
            photoRepository.insertPhotoInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPhotos_noPhotosOnline_returnsEmptyList() = runTest {
        coEvery { photoOnlineRepository.getAllPhotos() } returns emptyList()

        val result = downloadManager.downloadUnSyncedPhotos()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            photoOnlineRepository.getAllPhotos()
        }
        coVerify(exactly = 0) {
            photoRepository.getPhotoByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            photoRepository.insertPhotoInsertFromFirebase(any(), any(), any())
        }
        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            photoOnlineRepository.downloadImageLocally(any())
        }
    }
}
