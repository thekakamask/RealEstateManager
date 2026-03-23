package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePhotoOnlineEntity
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

class PhotoUploadManagerTest {

    private val photoRepository = mockk<PhotoRepository>(relaxed = true)
    private val photoOnlineRepository = mockk<PhotoOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: PhotoUploadInterfaceManager

    private val photoEntity1 = FakePhotoEntity.photo1
    private val photoEntity2 = FakePhotoEntity.photo2
    private val photoEntity3 = FakePhotoEntity.photo3
    private val photoOnlineEntity1 = FakePhotoOnlineEntity.photoOnline1

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(FirebaseAuth::class)

        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        uploadManager = PhotoUploadManager(photoRepository, photoOnlineRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedPhotos_photoNotDeleted_uploadsAndUpdatesRoom() = runTest {
        every {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        } returns flowOf(listOf(photoEntity1))

        coEvery {
            photoOnlineRepository.uploadPhoto(any(), any())
        } returns photoOnlineEntity1

        val result = uploadManager.syncUnSyncedPhotos()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly("Photo ${photoEntity1.id} uploaded to Firebase")

        coVerify(exactly = 1) {
            photoOnlineRepository.uploadPhoto(
                any(),
                photoEntity1.firestoreDocumentId!!
            )
        }

        val updatedPhotos = mutableListOf<PhotoOnlineEntity>()

        coVerify(exactly = 1) {
            photoRepository.updatePhotoFromFirebase(
                capture(updatedPhotos),
                photoEntity1.firestoreDocumentId!!
            )
        }

        assertThat(updatedPhotos.first().universalLocalId)
            .isEqualTo(photoEntity1.id)

        coVerify(exactly = 0) {
            photoRepository.deletePhoto(any())
        }
    }

    @Test
    fun uploadUnSyncedPhotos_photoMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        every {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        } returns flowOf(listOf(photoEntity3))

        coEvery {
            photoOnlineRepository.markPhotoAsDeleted(any(), any())
        } returns Unit

        val result = uploadManager.syncUnSyncedPhotos()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly(
                "Photo ${photoEntity3.id} marked deleted online & removed locally"
            )

        coVerify(exactly = 1) {
            photoOnlineRepository.markPhotoAsDeleted(
                photoEntity3.firestoreDocumentId!!,
                photoEntity3.updatedAt
            )
        }

        coVerify(exactly = 1) {
            photoRepository.deletePhoto(photoEntity3)
        }
        coVerify(exactly = 0) {
            photoOnlineRepository.uploadPhoto(any(), any())
        }
        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }
    }

    @Test
    fun uploadUnSyncedPhotos_globalFailure_throwsException() = runTest {
        every {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        } throws RuntimeException("DB crash")

        try {
            uploadManager.syncUnSyncedPhotos()
            throw AssertionError("Exception expected but not thrown")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("DB crash")
        }

        coVerify(exactly = 1) {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        }
        coVerify(exactly = 0) {
            photoOnlineRepository.uploadPhoto(any(), any())
        }
        coVerify(exactly = 0) {
            photoOnlineRepository.markPhotoAsDeleted(any(), any())
        }

        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }

        coVerify(exactly = 0) {
            photoRepository.deletePhoto(any())
        }
    }

    @Test
    fun uploadUnSyncedPhotos_noPhotosToUpload_returnsEmptyList() = runTest {
        every {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        } returns flowOf(emptyList())

        val result = uploadManager.syncUnSyncedPhotos()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        }

        coVerify(exactly = 0) {
            photoOnlineRepository.uploadPhoto(any(), any())
        }

        coVerify(exactly = 0) {
            photoOnlineRepository.markPhotoAsDeleted(any(), any())
        }

        coVerify(exactly = 0) {
            photoRepository.updatePhotoFromFirebase(any(), any())
        }

        coVerify(exactly = 0) {
            photoRepository.deletePhoto(any())
        }
    }

    @Test
    fun uploadUnSyncedPhotos_mixedCases_returnsCorrectStatuses() = runTest {
        val photoInsert = photoEntity1
        val photoDelete = photoEntity3
        val photoError = photoEntity2

        every {
            photoRepository.uploadUnSyncedPhotosToFirebase()
        } returns flowOf(listOf(photoInsert, photoDelete, photoError))
        coEvery {
            photoOnlineRepository.uploadPhoto(any(), any())
        } returns photoOnlineEntity1
        coEvery {
            photoOnlineRepository.markPhotoAsDeleted(any(), any())
        } returns Unit
        coEvery {
            photoOnlineRepository.uploadPhoto(
                match { it.universalLocalId == photoError.id },
                any()
            )
        } throws RuntimeException("Upload failed")

        val result = uploadManager.syncUnSyncedPhotos()

        assertThat(result).hasSize(3)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "Photo ${photoInsert.id} uploaded to Firebase",
            "Photo ${photoDelete.id} marked deleted online & removed locally"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Photo ${photoError.id}")
        assertThat(failure.error).hasMessageThat().isEqualTo("Upload failed")

        coVerify(exactly = 2) {
            photoOnlineRepository.uploadPhoto(any(), any())
        }

        val updatedPhotos = mutableListOf<PhotoOnlineEntity>()

        coVerify(exactly = 1) {
            photoRepository.updatePhotoFromFirebase(
                capture(updatedPhotos),
                any()
            )
        }

        assertThat(updatedPhotos.first().universalLocalId)
            .isEqualTo(photoInsert.id)

        coVerify(exactly = 1) {
            photoOnlineRepository.markPhotoAsDeleted(
                photoDelete.firestoreDocumentId!!,
                photoDelete.updatedAt
            )
        }

        coVerify(exactly = 1) {
            photoRepository.deletePhoto(photoDelete)
        }
    }
}
