
package com.dcac.realestatemanager.firebaseRepositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.photo.FirebasePhotoOnlineRepository
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import android.net.Uri
import androidx.core.net.toUri
import com.dcac.realestatemanager.data.firebaseDatabase.photo.FirebasePhotoDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.photo.FirebasePhotoUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.photo.FirebasePhotoDeleteException
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePhotoOnlineEntity
import kotlinx.coroutines.tasks.await
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


/**
 * Unit tests for [FirebasePhotoOnlineRepository].
 *
 * using MockK to simulate FirebaseFirestore and FirebaseStorage behavior,
 * so tests run locally without a real Firebase backend.
 */

@RunWith(JUnit4::class)
class FirebasePhotoRepositoryTest {

    // --- Firebase mock objects ---
    private val firestore = mockk<FirebaseFirestore>() // Firestore root
    private val storage = mockk<FirebaseStorage>()     // Firebase Storage root
    private val collection = mockk<CollectionReference>() // A Firestore collection
    private val document = mockk<DocumentReference>()     // A Firestore document
    private val storageRefRoot = mockk<StorageReference>() // Root of storage references
    private val storageRef = mockk<StorageReference>()     // A storage reference to a single file
    private val query = mockk<Query>()                 // Firestore query

    private lateinit var repo: PhotoOnlineRepository

    private val photoEntity1 = FakePhotoEntity.photo1
    private val photoEntity2 = FakePhotoEntity.photo2
    private val photoEntity3 = FakePhotoEntity.photo3
    private val photoOnlineEntity1 = FakePhotoOnlineEntity.photoEntity1
    private val photoOnlineEntity2 = FakePhotoOnlineEntity.photoEntity2
    private val photoOnlineEntity3 = FakePhotoOnlineEntity.photoEntity3
    private val photoOnlineEntityList = FakePhotoOnlineEntity.photoOnlineEntityList

    // Setup before each test.
     // Initializes MockK
    // Stubs Android Uri and Firebase coroutines extensions
    // Prevents "Method Log.d not mocked" crash

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Setup common repository
        repo = FirebasePhotoOnlineRepository(firestore, storage)

        // Mock Uri.parse() and toUri() extensions so they don't rely on Android runtime
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        mockkStatic("com.google.firebase.storage.ktx.StorageKt") // For UploadTask.await()
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")          // For Task<T>.await()
        mockkStatic("androidx.core.net.UriKt")                   // For String.toUri()

        // ✅ Mock Log to prevent "Method d not mocked" errors
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
    }

    // Clean up mocks after each test *//*

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadPhoto_success_uploadsToStorageWritesFirestore_ReturnSyncPhoto() = runTest {
        // --- GIVEN (setup test data and mocks) ---

        // Take a fake photo object from our test data
        val photoId = photoEntity1.id.toString() // Firebase uses string IDs

        // Mock the behavior of "toUri()" (Android extension).
        // Normally, photo.uri.toUri() would return a real Android Uri,
        // but here we provide a fake Uri object to stay in JVM tests.
        val fileUri = mockk<Uri>()
        every { any<String>().toUri() } returns fileUri

        // Mock a fake download URL returned by Firebase Storage
        val downloadUri = mockk<Uri>()
        every { downloadUri.toString() } returns "https://firebase.storage.com/$photoId.jpg"

        // Mock Firebase Storage upload process:
        // - UploadTask represents the async upload
        // - TaskSnapshot is the result of a completed upload
        val uploadTask = mockk<UploadTask>()
        val uploadSnapshot = mockk<UploadTask.TaskSnapshot>()

        // Firebase downloadUrl property is also a Task<Uri>
        val downloadTask = mockk<com.google.android.gms.tasks.Task<Uri>>()

        // Define Firebase Storage flow:
        // 1. Get root storage reference
        every { storage.reference } returns storageRefRoot
        // 2. Navigate to a child path "photos/{photoId}.jpg"
        every { storageRefRoot.child("photos/${photoId}.jpg") } returns storageRef
        // 3. Upload the file with putFile() → returns UploadTask
        every { storageRef.putFile(fileUri) } returns uploadTask
        // 4. Await the upload task successfully
        coEvery { uploadTask.await() } returns uploadSnapshot

        // 5. Get the download URL of the uploaded file
        every { storageRef.downloadUrl } returns downloadTask
        // 6. Await the downloadUrl task → returns our fake Uri
        coEvery { downloadTask.await() } returns downloadUri

        // Define Firestore behavior:
        // 1. Point to "photos" collection
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        // 2. Get a document by photoId
        every { collection.document(photoId) } returns document

        // Convert to online entity using your mapper
        val photoOnline = photoOnlineEntity1.copy(storageUrl = photoEntity1.uri)

        // Expected Firestore entity with remote URL after upload
        val expectedEntity = photoOnline.copy(storageUrl = downloadUri.toString())

        // Mock Firestore "set()" to succeed with this expected entity
        coEvery { document.set(eq(expectedEntity)).await() } returns null


        // --- WHEN (execute the function under test) ---
        val result = repo.uploadPhoto(photoOnline, photoId)


        // --- THEN (verify results and interactions) ---

        // ---assert synced and remote URL is returned
        assertThat(result).isEqualTo(expectedEntity)

        // Verify that:
        // 1. Firebase Storage "putFile" was called exactly once with our fake Uri
        verify(exactly = 1) { storageRef.putFile(fileUri) }

        // 2. Firestore "set" was called once with the expected entity
        verify(exactly = 1) { document.set(eq(expectedEntity)) }



    }

    @Test
    fun uploadPhoto_storageFailure_wrapsInFirebasePhotoUploadException() = runTest {
        // --- GIVEN (setup test data and mocks) ---

        // Use a fake photo from our test data
        val photoId = photoEntity2.id.toString()

        // Simulates a PhotoOnlineEntity ready to be uploaded (local path)
        val photoOnline = photoOnlineEntity2.copy(storageUrl = photoEntity2.uri)

        // Convert the photo's local URI (string) into a Uri object.
        // Normally Android would do this, but here we just call Uri.parse()
        val fileUri: Uri = Uri.parse(photoOnline.storageUrl)

        // Mock the Firebase UploadTask (represents an ongoing upload)
        val uploadTask = mockk<UploadTask>()

        // Define Firebase Storage flow up to the upload:
        // 1. Get root reference of Firebase Storage
        every { storage.reference } returns storageRefRoot
        // 2. Navigate to a child path for this specific photo
        every { storageRefRoot.child("photos/${photoId}.jpg") } returns storageRef
        // 3. Call putFile() with our fileUri, returns the mocked UploadTask
        every { storageRef.putFile(fileUri) } returns uploadTask

        // ❌ Simulate a failure when awaiting the upload task.
        // Instead of succeeding, the upload throws a RuntimeException.
        coEvery { uploadTask.await() } throws RuntimeException("Storage upload failed")


        // --- WHEN (execute the function under test) ---
        // runCatching allows us to capture the exception instead of crashing the test
        val thrown = runCatching { repo.uploadPhoto(photoOnline, photoId) }.exceptionOrNull()


        // --- THEN (verify the behavior is correct) ---

        // The repository should wrap the RuntimeException inside
        // our custom FirebasePhotoUploadException
        assertThat(thrown).isInstanceOf(FirebasePhotoUploadException::class.java)

        // The root cause of the exception should be the original "Upload failed"
        assertThat(thrown!!.cause?.message).isEqualTo("Storage upload failed")
    }

    @Test
    fun uploadPhoto_emptyUri_wrapsInFirebasePhotoUploadException() = runTest {
        // --- GIVEN ---
        val photoId = photoEntity3.id.toString()

        // Simulate a PhotoOnlineEntity with an empty storageUrl
        val photoOnline = photoOnlineEntity3.copy(storageUrl = "")

        // --- WHEN ---
        val thrown = runCatching { repo.uploadPhoto(photoOnline, photoId) }.exceptionOrNull()

        // --- THEN ---
        assertThat(thrown).isInstanceOf(FirebasePhotoUploadException::class.java)
        assertThat(thrown).hasCauseThat().isInstanceOf(IllegalArgumentException::class.java)
        assertThat(thrown).hasCauseThat().hasMessageThat().contains("Photo URI is empty")
    }

    @Test
    fun uploadPhoto_firestoreFailure_wrapsInFirebasePhotoUploadException() = runTest {
        // --- GIVEN ---
        val photoId = photoEntity1.id.toString()

        // Simulate a PhotoOnlineEntity with a real local storageUrl (local path)
        val photoOnline = photoOnlineEntity1.copy(storageUrl = photoEntity1.uri)

        val fileUri = mockk<Uri>()
        every { any<String>().toUri() } returns fileUri

        val downloadUri = mockk<Uri>()
        every { downloadUri.toString() } returns "https://firebase.storage.com/$photoId.jpg"

        val uploadTask = mockk<UploadTask>()
        val uploadSnapshot = mockk<UploadTask.TaskSnapshot>()
        val downloadTask = mockk<com.google.android.gms.tasks.Task<Uri>>()

        // ✅ Mock Firebase Storage (upload OK)
        every { storage.reference } returns storageRefRoot
        every { storageRefRoot.child("photos/${photoId}.jpg") } returns storageRef
        every { storageRef.putFile(fileUri) } returns uploadTask
        coEvery { uploadTask.await() } returns uploadSnapshot
        every { storageRef.downloadUrl } returns downloadTask
        coEvery { downloadTask.await() } returns downloadUri

        // ❌ Simulate an error in Firestore .set()
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.document(photoId) } returns document
        coEvery { document.set(any()).await() } throws RuntimeException("Firestore failed")

        // --- WHEN ---
        val thrown = runCatching { repo.uploadPhoto(photoOnline, photoId) }.exceptionOrNull()

        // --- THEN ---
        assertThat(thrown).isInstanceOf(FirebasePhotoUploadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun getPhoto_success_returnsPhoto() = runTest {
        // --- GIVEN ---

        val photoOnlineEntityId = photoOnlineEntity1.roomId.toString() // Firestore document ID (string)

        // Mock snapshot Firestore document
        val snapshot = mockk<DocumentSnapshot>()

        // Stub Firestore call chain
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.document(photoOnlineEntityId) } returns document
        coEvery { document.get().await() } returns snapshot

        // Simulate Firestore returning our expected photo entity
        every { snapshot.toObject(PhotoOnlineEntity::class.java) } returns photoOnlineEntity1

        // --- WHEN ---

        val result = repo.getPhoto(photoOnlineEntityId)

        // --- THEN ---

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(photoOnlineEntity1)
    }

    @Test
    fun getPhoto_noEntityFound_returnsNull() = runTest {
        // --- GIVEN ---

        val photoId = "99" // ID that does not correspond to any Firestore document

        // Mock snapshot vide
        val snapshot = mockk<DocumentSnapshot>()

        // Stub Firestore chain
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.document(photoId) } returns document
        coEvery { document.get().await() } returns snapshot

        // Simulates missing data in Firestore
        every { snapshot.toObject(PhotoOnlineEntity::class.java) } returns null

        // --- WHEN ---

        val result = repo.getPhoto(photoId)

        // --- THEN ---

        assertThat(result).isNull()
    }

    @Test
    fun getPhoto_firestoreFailure_wrapsInFirebasePhotoDownloadException() = runTest {
        // --- GIVEN ---

        val photoId = photoOnlineEntity2.roomId.toString()

        // Simulate a failing Firestore call chain
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.document(photoId) } returns document
        coEvery { document.get().await() } throws RuntimeException("Firestore failed")

        // --- WHEN ---

        val thrown = runCatching { repo.getPhoto(photoId) }.exceptionOrNull()

        // --- THEN ---

        assertThat(thrown).isInstanceOf(FirebasePhotoDownloadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun getPhotosByPropertyId_success_returnsList() = runTest {
        // --- GIVEN ---

        val propertyId = photoOnlineEntity1.propertyId

        // Filter simulated Firebase entities for this propertyId
        val expectedEntities = photoOnlineEntityList
            .filter { it.propertyId == propertyId }

        // Mock of QuerySnapshot and DocumentSnapshot returned by Firestore
        val snapshot = mockk<QuerySnapshot>()
        val docs = expectedEntities.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(PhotoOnlineEntity::class.java) } returns entity
                every { id } returns entity.roomId.toString()
            }
        }

        // Firestore chain simulation
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        // --- WHEN ---

        val result = repo.getPhotosByPropertyId(propertyId)

        // --- THEN ---

        // Verifies that the number of entities matches
        assertThat(result).hasSize(expectedEntities.size)

        // Verifies that each returned entity corresponds to the expected one
        result.forEachIndexed { index, actual ->
            val expected = expectedEntities[index]

            assertThat(actual.description).isEqualTo(expected.description)
            assertThat(actual.propertyId).isEqualTo(propertyId)
            assertThat(actual.storageUrl).isEqualTo(expected.storageUrl)
            assertThat(actual.updatedAt).isEqualTo(expected.updatedAt)
            assertThat(actual.roomId).isEqualTo(expected.roomId)
        }
    }

    @Test
    fun getPhotosByPropertyId_noResults_returnsEmptyList() = runTest {
        // --- GIVEN ---
        val propertyId = 42L // No entity matches this ID
        val snapshot = mockk<QuerySnapshot>()

        // Simulates the Firestore string
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList() // No photos found

        // --- WHEN ---
        val result = repo.getPhotosByPropertyId(propertyId)

        // --- THEN ---
        assertThat(result).isEmpty() // Expected result: empty list
    }

    @Test
    fun getPhotosByPropertyId_firestoreFailure_throwsException() = runTest {
        // --- GIVEN ---
        val propertyId = photoOnlineEntity2.propertyId

        // Simulates the Firestore call chain
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        // --- WHEN ---
        val thrown = runCatching { repo.getPhotosByPropertyId(propertyId) }.exceptionOrNull()

        // --- THEN ---
        assertThat(thrown).isInstanceOf(FirebasePhotoDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("Firestore failed")
    }

    @Test
    fun getAllPhotos_success_returnsList() = runTest {
        // --- GIVEN ---

        // Fake Firebase entities from your test dataset
        // Create a mocked Firestore QuerySnapshot and mocked DocumentSnapshots
        val snapshot = mockk<QuerySnapshot>()
        val docs = photoOnlineEntityList.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(PhotoOnlineEntity::class.java) } returns entity
                every { id } returns entity.roomId.toString()
            }
        }

        // Mock Firestore call chain
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        // --- WHEN ---

        val result = repo.getAllPhotos()

        // --- THEN ---

        // Check that all entities were returned and mapped correctly
        assertThat(result).hasSize(photoOnlineEntityList.size)

        result.forEachIndexed { index, actual ->
            val expected = photoOnlineEntityList[index]

            assertThat(actual.description).isEqualTo(expected.description)
            assertThat(actual.propertyId).isEqualTo(expected.propertyId)
            assertThat(actual.storageUrl).isEqualTo(expected.storageUrl)
            assertThat(actual.updatedAt).isEqualTo(expected.updatedAt)
            assertThat(actual.roomId).isEqualTo(expected.roomId)
        }
    }

    @Test
    fun getAllPhotos_noResults_returnsEmptyList() = runTest {
        // --- GIVEN ---
        val snapshot = mockk<QuerySnapshot>()

        // Simulate Firestore call
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList() // No document find

        // --- WHEN ---
        val result = repo.getAllPhotos()

        // --- THEN ---
        assertThat(result).isEmpty() // Empty list
    }

    @Test
    fun getAllPhotos_firestoreFailure_wrapsInFirebasePhotoDownloadException() = runTest {
        // --- GIVEN ---

        // Simulate Firestore call
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("Firestore failed")

        // --- WHEN ---
        val thrown = runCatching { repo.getAllPhotos() }.exceptionOrNull()

        // --- THEN ---
        assertThat(thrown).isInstanceOf(FirebasePhotoDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("Firestore failed")
    }

    //SUITE ICI

    @Test
    fun deletePhoto_success_callsFirestoreDelete() = runTest {
        // --- GIVEN ---
        val photoId = photoEntity3.id.toString()

        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.document(photoId) } returns document
        coEvery { document.delete().await() } returns null // Simulate successful deletion

        // --- WHEN ---
        repo.deletePhoto(photoId)

        // --- THEN ---
        coVerify(exactly = 1) { document.delete().await() }
    }

    @Test
    fun deletePhoto_firestoreFailure_throwsException() = runTest {
        // --- GIVEN ---
        val photoId = photoEntity1.id.toString()

        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.document(photoId) } returns document
        coEvery { document.delete().await() } throws RuntimeException("Firestore failed")

        // --- WHEN ---
        val thrown = runCatching { repo.deletePhoto(photoId) }.exceptionOrNull()

        // --- THEN ---
        assertThat(thrown).isInstanceOf(FirebasePhotoDeleteException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }


    @Test
    fun deletePhotosByPropertyId_success_deletesAllDocs() = runTest {
        // --- GIVEN (mock Firestore query and documents) ---

        val propertyId = photoOnlineEntity3.propertyId
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val ref1 = mockk<DocumentReference>()
        val ref2 = mockk<DocumentReference>()

        // Firestore query by propertyId
        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot

        // Simulate snapshot with 2 documents
        every { snapshot.documents } returns listOf(doc1, doc2)

        // Each document has a reference (its location in Firestore)
        every { doc1.reference } returns ref1
        every { doc2.reference } returns ref2

        // Simulate successful deletion of both documents
        coEvery { ref1.delete().await() } returns null
        coEvery { ref2.delete().await() } returns null


        // --- WHEN (call the repository method) ---

        repo.deletePhotosByPropertyId(propertyId)


        // --- THEN (verify both documents were deleted) ---
        coVerify { ref1.delete().await() }
        coVerify { ref2.delete().await() }
    }

    @Test
    fun deletePhotosByPropertyId_firestoreFailureAtQuery_throwsException() = runTest {
        // --- GIVEN ---
        val propertyId = photoOnlineEntity2.propertyId

        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        // --- WHEN ---
        val thrown = runCatching { repo.deletePhotosByPropertyId(propertyId) }.exceptionOrNull()

        // --- THEN ---
        assertThat(thrown).isInstanceOf(FirebasePhotoDeleteException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun deletePhotosByPropertyId_firestoreFailureAtDelete_throwsException() = runTest {
        // --- GIVEN ---
        val propertyId = photoOnlineEntity1.propertyId
        val snapshot = mockk<QuerySnapshot>()
        val doc = mockk<DocumentSnapshot>()
        val ref = mockk<DocumentReference>()

        every { firestore.collection(FirestoreCollections.PHOTOS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns listOf(doc)
        every { doc.reference } returns ref

        // Simule une erreur au delete
        coEvery { ref.delete().await() } throws RuntimeException("Firestore failed")

        // --- WHEN ---
        val thrown = runCatching { repo.deletePhotosByPropertyId(propertyId) }.exceptionOrNull()

        // --- THEN ---
        assertThat(thrown).isInstanceOf(FirebasePhotoDeleteException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }


}