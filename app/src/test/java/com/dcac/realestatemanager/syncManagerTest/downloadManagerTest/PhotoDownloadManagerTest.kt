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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File


class PhotoDownloadManagerTest {

    // --- Mocks for local and remote photo repositories ---
    private val photoRepository = mockk<PhotoRepository>(relaxed = true)
    private val photoOnlineRepository = mockk<PhotoOnlineRepository>(relaxed = true)

    private lateinit var downloadManager: PhotoDownloadInterfaceManager

    // --- Local photo entities (Room) ---
    private val photoEntity1 = FakePhotoEntity.photo1
    private val photoEntity2 = FakePhotoEntity.photo2
    private val photoEntity3 = FakePhotoEntity.photo3
    private val photoEntityList = FakePhotoEntity.photoEntityList

    // --- Online photo entities (Firebase) ---
    private val photoOnlineEntity1 = FakePhotoOnlineEntity.photoEntity1
    private val photoOnlineEntity2 = FakePhotoOnlineEntity.photoEntity2
    private val photoOnlineEntity3 = FakePhotoOnlineEntity.photoEntity3
    private val photoOnlineEntityList = FakePhotoOnlineEntity.photoOnlineEntityList

    // --- Firebase Storage mocks ---
    private val firebaseStorage = mockk<FirebaseStorage>()
    private val storageRef = mockk<StorageReference>()
    private val tempFile = mockk<File>(relaxed = true)

    @Before
    fun setup() {
        // 🔧 Initialize MockK annotations (optional here since we manually mock everything)
        MockKAnnotations.init(this, relaxUnitFun = true)

        // 🧪 Create instance of the class under test
        downloadManager = PhotoDownloadManager(photoRepository, photoOnlineRepository)

        // 🧩 Mock static method: FirebaseStorage.getInstance()
        mockkStatic(FirebaseStorage::class)

        // 🔌 When getInstance() is called → return our mocked FirebaseStorage
        every { FirebaseStorage.getInstance() } returns firebaseStorage

        // 📦 When getReferenceFromUrl(...) is called → return a mocked reference to the file
        every { firebaseStorage.getReferenceFromUrl(any()) } returns storageRef

        // 🔧 Mock extension function: Task<T>.await() from kotlinx.coroutines.tasks
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        // 🧾 Mock static creation of temp files with File.createTempFile(...)
        mockkStatic(File::class)                // Not always necessary but safe
        every { File.createTempFile(any(), any()) } returns tempFile

        // 🗂️ Simulate the result of converting the file into a URI string
        every { tempFile.toURI().toString() } returns "file://mock_download.jpg"

        // ⬇️ Simulate Firebase downloading the file to the temp location
        coEvery { storageRef.getFile(tempFile).await() } returns mockk()
    }

    @After
    fun tearDown() {
        // 🧼 Unmock everything to clean the testing environment
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedPhotos_localPhotoNull_downloadsAndInsertsPhoto() = runTest {
        // Arrange

        val photoId = photoOnlineEntity1.roomId

        // Firebase returns this one photo
        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(photoOnlineEntity1)

        // Room returns no local photo (null)
        every { photoRepository.getPhotoEntityById(photoId) } returns flowOf(null)

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("Photo $photoId inserted")

        // Verify that the downloaded photo was saved to Room
        coVerify(exactly = 1) {
            photoRepository.downloadPhotoFromFirebase(photoOnlineEntity1, "file://mock_download.jpg")
        }
    }

    @Test
    fun downloadUnSyncedPhotos_allPhotosMissingLocally_downloadsAndInsertsAll() = runTest {
        // Arrange

        coEvery { photoOnlineRepository.getAllPhotos() } returns photoOnlineEntityList

        photoOnlineEntityList.forEach { photoOnline ->
            every { photoRepository.getPhotoEntityById(photoOnline.roomId) } returns flowOf(null)
        }

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(photoOnlineEntityList.size)

        result.forEachIndexed { i, status ->
            val success = status as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail).isEqualTo("Photo ${photoOnlineEntityList[i].roomId} inserted")
        }

        // ✅ Verify all photos were downloaded and saved
        photoOnlineEntityList.forEach {
            coVerify { photoRepository.downloadPhotoFromFirebase(it, "file://mock_download.jpg") }
        }
    }

    @Test
    fun downloadUnSyncedPhotos_localPhotoOutdated_downloadsAndUpdatesPhoto() = runTest {
        // Arrange
        val outdatedLocalPhoto = photoEntity1.copy(updatedAt = 1700000000000)
        val updatedOnlinePhoto = photoOnlineEntity1.copy(updatedAt = 1700000002000)

        val photoId = updatedOnlinePhoto.roomId

        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(updatedOnlinePhoto)

        every { photoRepository.getPhotoEntityById(photoId) } returns flowOf(outdatedLocalPhoto)

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("Photo $photoId updated")

        coVerify(exactly = 1) {
            photoRepository.downloadPhotoFromFirebase(updatedOnlinePhoto, "file://mock_download.jpg")
        }
    }

    @Test
    fun downloadUnSyncedPhotos_allPhotosOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        // Arrange
        val outdatedLocalPhotos = photoEntityList.mapIndexed { index, photo ->
            photo.copy(updatedAt = 1700000000000 + index) // Older than Firebase
        }

        val newerOnlinePhotos = photoOnlineEntityList.mapIndexed { index, photo ->
            photo.copy(updatedAt = 1700000000000 + index + 5) // Newer than local
        }

        coEvery { photoOnlineRepository.getAllPhotos() } returns newerOnlinePhotos

        newerOnlinePhotos.forEachIndexed { index, photoOnline ->
            every { photoRepository.getPhotoEntityById(photoOnline.roomId) } returns flowOf(outdatedLocalPhotos[index])
        }

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(newerOnlinePhotos.size)

        result.forEachIndexed { i, status ->
            val success = status as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail).isEqualTo("Photo ${newerOnlinePhotos[i].roomId} updated")
        }

        // ✅ Verify all outdated photos were updated
        newerOnlinePhotos.forEach {
            coVerify { photoRepository.downloadPhotoFromFirebase(it, "file://mock_download.jpg") }
        }
    }

    @Test
    fun downloadUnSyncedPhotos_photoAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        // Arrange
        val photoId = photoOnlineEntity1.roomId

        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(photoOnlineEntity1)

        every { photoRepository.getPhotoEntityById(photoId) } returns flowOf(photoEntity1)

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("Photo $photoId already up-to-date")

        // ❌ Verify that nothing is saved
        coVerify(exactly = 0) {
            photoRepository.downloadPhotoFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPhotos_allPhotosAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        // Arrange

        coEvery { photoOnlineRepository.getAllPhotos() } returns photoOnlineEntityList

        photoOnlineEntityList.forEachIndexed { index, onlinePhoto ->
            every { photoRepository.getPhotoEntityById(onlinePhoto.roomId) } returns flowOf(photoEntityList[index])
        }

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(photoOnlineEntityList.size)

        result.forEachIndexed { i, status ->
            val success = status as? SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success!!.userEmail).isEqualTo("Photo ${photoOnlineEntityList[i].roomId} already up-to-date")
        }

        // ❌ Verify that no photo was downloaded or saved
        coVerify(exactly = 0) {
            photoRepository.downloadPhotoFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedPhotos_mixedCases_returnsCorrectStatuses() = runTest {
        // Arrange
        val photoOnlineEntity4 = PhotoOnlineEntity(
            description = "Bathroom of Penthouse Champs-Élysées",
            propertyId = 4L,
            updatedAt = 1700000008000,
            storageUrl = "https://firebase.storage.com/photo_4.jpg",
            roomId = 4L
        )
        val photoInsert = photoOnlineEntity1 // -> should insert (no local)
        val photoUpdate = photoOnlineEntity2.copy(updatedAt = 1700000006000) // -> should update (outdated)
        val photoSkip   = photoOnlineEntity3 // -> already up-to-date
        val photoError  = photoOnlineEntity4.copy(updatedAt = 1700000008000) // -> error fetching local

        val outdatedLocalPhoto = photoEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocalPhoto = photoEntity3

        val onlinePhotos = listOf(photoInsert, photoUpdate, photoSkip, photoError)

        coEvery { photoOnlineRepository.getAllPhotos() } returns onlinePhotos

        every { photoRepository.getPhotoEntityById(photoInsert.roomId) } returns flowOf(null) // insert
        every { photoRepository.getPhotoEntityById(photoUpdate.roomId) } returns flowOf(outdatedLocalPhoto) // update
        every { photoRepository.getPhotoEntityById(photoSkip.roomId) } returns flowOf(upToDateLocalPhoto) // skip
        every { photoRepository.getPhotoEntityById(photoError.roomId) } throws RuntimeException("DB fail") // error

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(4)

        // Check insertion
        val statusInsert = result[0] as? SyncStatus.Success
        assertThat(statusInsert).isNotNull()
        assertThat(statusInsert!!.userEmail).isEqualTo("Photo ${photoInsert.roomId} inserted")

        // Check update
        val statusUpdate = result[1] as? SyncStatus.Success
        assertThat(statusUpdate).isNotNull()
        assertThat(statusUpdate!!.userEmail).isEqualTo("Photo ${photoUpdate.roomId} updated")

        // Check skip
        val statusSkip = result[2] as? SyncStatus.Success
        assertThat(statusSkip).isNotNull()
        assertThat(statusSkip!!.userEmail).isEqualTo("Photo ${photoSkip.roomId} already up-to-date")

        // Check error
        val statusError = result[3] as? SyncStatus.Failure
        assertThat(statusError).isNotNull()
        assertThat(statusError!!.label).isEqualTo("Photo ${photoError.roomId}")
        assertThat(statusError.error).hasMessageThat().isEqualTo("DB fail")

        // Verify save was called for insert + update only
        coVerify(exactly = 1) { photoRepository.downloadPhotoFromFirebase(photoInsert, "file://mock_download.jpg") }
        coVerify(exactly = 1) { photoRepository.downloadPhotoFromFirebase(photoUpdate, "file://mock_download.jpg") }
        coVerify(exactly = 0) { photoRepository.downloadPhotoFromFirebase(photoSkip, any()) }
        coVerify(exactly = 0) { photoRepository.downloadPhotoFromFirebase(photoError, any()) }
    }

    @Test
    fun downloadUnSyncedPhotos_individualFailure_returnsPartialSuccessWithFailure() = runTest {
        // Arrange
        val photoId = photoOnlineEntity1.roomId

        coEvery { photoOnlineRepository.getAllPhotos() } returns listOf(photoOnlineEntity1)

        every { photoRepository.getPhotoEntityById(photoId) } throws RuntimeException("DB crash")

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)

        val failure = result[0] as? SyncStatus.Failure
        assertThat(failure).isNotNull()
        assertThat(failure!!.label).isEqualTo("Photo $photoId")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB crash")
    }

    @Test
    fun downloadUnSyncedPhotos_globalFailure_returnsFailureStatus() = runTest {
        // Arrange
        coEvery { photoOnlineRepository.getAllPhotos() } throws RuntimeException("Firebase is down")

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).hasSize(1)

        val failure = result[0] as? SyncStatus.Failure
        assertThat(failure).isNotNull()
        assertThat(failure!!.label).isEqualTo("Photo download (global failure)")
        assertThat(failure.error).hasMessageThat().isEqualTo("Firebase is down")
    }

    @Test
    fun downloadUnSyncedPhotos_noPhotosOnline_returnsEmptyList() = runTest {
        // Arrange
        coEvery { photoOnlineRepository.getAllPhotos() } returns emptyList()

        // Act
        val result = downloadManager.downloadUnSyncedPhotos()

        // Assert
        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            photoRepository.downloadPhotoFromFirebase(any(), any())
        }
    }
}
