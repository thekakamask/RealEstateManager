package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePhotoOnlineEntity
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

class PhotoUploadManagerTest {

    // --- Mocks ---
    private val photoRepository = mockk<PhotoRepository>(relaxed = true)
    private val photoOnlineRepository = mockk<PhotoOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: PhotoUploadInterfaceManager

    // --- Fake data ---
    private val photoEntity1 = FakePhotoEntity.photo1
    private val photoEntity2 = FakePhotoEntity.photo2
    private val photoEntity3 = FakePhotoEntity.photo3

    private val photoOnline1 = FakePhotoOnlineEntity.photoEntity1

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        uploadManager = PhotoUploadManager(photoRepository, photoOnlineRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedPhotos_photoNotDeleted_uploadsAndUpdatesRoom() = runTest {
        // Arrange
        coEvery { photoRepository.uploadUnSyncedPhotosToFirebase() } returns flowOf(listOf(photoEntity1))
        coEvery { photoOnlineRepository.uploadPhoto(any(), any()) } returns photoOnline1

        // Act
        val result = uploadManager.syncUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)
        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("Photo ${photoEntity1.id} uploaded")

        coVerify {
            photoOnlineRepository.uploadPhoto(any(), photoEntity1.id.toString())
            photoRepository.downloadPhotoFromFirebase(photoOnline1, photoEntity1.uri)
        }
    }

    @Test
    fun uploadUnSyncedPhotos_photoMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        // Arrange
        val deletedPhoto = photoEntity3
        coEvery { photoRepository.uploadUnSyncedPhotosToFirebase() } returns flowOf(listOf(deletedPhoto))

        // Act
        val result = uploadManager.syncUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)
        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("Photo ${deletedPhoto.id} deleted")

        coVerify {
            photoOnlineRepository.deletePhoto(deletedPhoto.id.toString())
            photoRepository.deletePhoto(deletedPhoto)
        }
    }

    @Test
    fun uploadUnSyncedPhotos_globalFailure_returnsFailureStatus() = runTest {
        // Arrange
        coEvery { photoRepository.uploadUnSyncedPhotosToFirebase() } throws RuntimeException("Room is down")

        // Act
        val result = uploadManager.syncUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)
        val failure = result[0] as? SyncStatus.Failure
        assertThat(failure!!.label).isEqualTo("Global upload sync failed")
        assertThat(failure.error.message).isEqualTo("Room is down")
    }

    @Test
    fun uploadUnSyncedPhotos_noPhotosToUpload_returnsEmptyList() = runTest {
        // Arrange
        coEvery { photoRepository.uploadUnSyncedPhotosToFirebase() } returns flowOf(emptyList())

        // Act
        val result = uploadManager.syncUnSyncedPhotos()

        // Assert
        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            photoOnlineRepository.uploadPhoto(any(), any())
            photoRepository.downloadPhotoFromFirebase(any(), any())
            photoOnlineRepository.deletePhoto(any())
            photoRepository.deletePhoto(any())
        }
    }

    @Test
    fun uploadUnSyncedPhotos_mixedCases_returnsCorrectStatuses() = runTest {
        // Arrange
        val notSyncedNotDeleted = photoEntity1
        val alreadySyncedNotDeleted = photoEntity2
        val notSyncedDeleted = photoEntity3

        // Only return unsynced photos: photo1 (upload), photo3 (delete)
        coEvery {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        } returns flowOf(listOf(notSyncedNotDeleted, notSyncedDeleted))

        // Mock upload
        coEvery {
            photoOnlineRepository.uploadPhoto(any(), notSyncedNotDeleted.id.toString())
        } returns photoOnline1

        // Act
        val result = uploadManager.syncUnSyncedPhotos()

        // Assert: we should have 2 results
        assertThat(result).hasSize(2)

        // Check both statuses, regardless of order
        val uploaded = result.find { it is SyncStatus.Success && it.userEmail == "Photo ${notSyncedNotDeleted.id} uploaded" }
        val deleted = result.find { it is SyncStatus.Success && it.userEmail == "Photo ${notSyncedDeleted.id} deleted" }

        assertThat(uploaded).isNotNull()
        assertThat(deleted).isNotNull()

        // Verify Firebase/Room interactions
        coVerify { photoOnlineRepository.deletePhoto(notSyncedDeleted.id.toString()) }
        coVerify { photoRepository.deletePhoto(notSyncedDeleted) }
        coVerify { photoOnlineRepository.uploadPhoto(any(), notSyncedNotDeleted.id.toString()) }
        coVerify { photoRepository.downloadPhotoFromFirebase(photoOnline1, notSyncedNotDeleted.uri) }

        // Ensure nothing happened to already-synced photo
        coVerify(exactly = 0) {
            photoOnlineRepository.uploadPhoto(any(), alreadySyncedNotDeleted.id.toString())
        }
        coVerify(exactly = 0) {
            photoRepository.downloadPhotoFromFirebase(any(), alreadySyncedNotDeleted.uri)
        }
    }
}