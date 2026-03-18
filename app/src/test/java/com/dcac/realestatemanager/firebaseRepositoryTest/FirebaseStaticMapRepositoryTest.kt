package com.dcac.realestatemanager.firebaseRepositoryTest

import android.net.Uri
import androidx.core.net.toUri
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.STATIC_MAPS
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.FirebaseStaticMapDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.FirebaseStaticMapOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.FirebaseStaticMapUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineRepository
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeStaticMapEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeStaticMapOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File


@RunWith(JUnit4::class)
class FirebaseStaticMapRepositoryTest {

    private val firestore = mockk<FirebaseFirestore>()
    private val storage = mockk<FirebaseStorage>()
    private val collection = mockk<CollectionReference>()
    private val document = mockk<DocumentReference>()
    private val storageRefRoot = mockk<StorageReference>()
    private val storageRef = mockk<StorageReference>()
    private val query = mockk<Query>()

    private lateinit var repo: StaticMapOnlineRepository

    private val staticMapEntity1 = FakeStaticMapEntity.staticMap1
    private val staticMapEntity2 = FakeStaticMapEntity.staticMap2
    private val staticMapEntity3 = FakeStaticMapEntity.staticMap3
    private val staticMapOnlineEntity1 = FakeStaticMapOnlineEntity.staticMapOnline1
    private val staticMapOnlineEntity2 = FakeStaticMapOnlineEntity.staticMapOnline2
    private val staticMapOnlineEntity3 = FakeStaticMapOnlineEntity.staticMapOnline3
    private val staticMapOnlineEntityList = FakeStaticMapOnlineEntity.staticMapOnlineEntityList

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        repo = FirebaseStaticMapOnlineRepository(firestore, storage)

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
        mockkStatic("com.google.firebase.storage.ktx.StorageKt")
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        mockkStatic("androidx.core.net.UriKt")
        mockkStatic(FirebaseStorage::class)

        every { FirebaseStorage.getInstance() } returns storage

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadStaticMap_success_uploadsToStorageWritesFirestore_returnSyncStaticMap() = runTest {

        val staticMapId = staticMapEntity1.id

        val fileUri = mockk<Uri>()
        every { fileUri.scheme } returns "file"
        every { any<String>().toUri() } returns fileUri

        val downloadUri = mockk<Uri>()
        every { downloadUri.toString() } returns "https://firebase.storage.com/$staticMapId.jpg"

        val uploadTask = mockk<UploadTask>()
        val uploadSnapshot = mockk<UploadTask.TaskSnapshot>()

        val downloadTask = mockk<com.google.android.gms.tasks.Task<Uri>>()

        every { storage.reference } returns storageRefRoot
        every { storageRefRoot.child("staticMaps/${staticMapId}.jpg") } returns storageRef
        every { storageRef.putFile(fileUri) } returns uploadTask
        coEvery { uploadTask.await() } returns uploadSnapshot
        every { storageRef.downloadUrl } returns downloadTask
        coEvery { downloadTask.await() } returns downloadUri
        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.document(staticMapId)} returns document

        val staticMapOnline = staticMapOnlineEntity1.copy(storageUrl = staticMapEntity1.uri)
        val expectedEntity = staticMapOnline.copy(storageUrl = downloadUri.toString())

        coEvery { document.set(eq(expectedEntity)).await() } returns null

        val result = repo.uploadStaticMap(staticMapOnline, staticMapId)

        assertThat(result).isEqualTo(expectedEntity)
        verify(exactly = 1) { storageRef.putFile(fileUri) }
        verify(exactly = 1) { document.set(eq(expectedEntity)) }

    }

    @Test
    fun uploadStaticMap_storageFailure_wrapsInFirebaseStaticMapUploadException() = runTest {
        val staticMapId = staticMapEntity2.id

        val staticMapOnline = staticMapOnlineEntity2.copy(storageUrl = staticMapEntity2.uri)

        val fileUri: Uri = Uri.parse(staticMapOnline.storageUrl)

        val uploadTask = mockk<UploadTask>()

        every { storage.reference } returns storageRefRoot
        every { storageRefRoot.child("staticMaps/${staticMapId}.jpg") } returns storageRef
        every { storageRef.putFile(fileUri) } returns uploadTask

        coEvery { uploadTask.await() } throws RuntimeException("Storage upload failed")

        val thrown = runCatching { repo.uploadStaticMap(staticMapOnline, staticMapId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseStaticMapUploadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Storage upload failed")
    }

    @Test
    fun uploadStaticMap_emptyUri_wrapsInFirebaseStaticMapUploadException() = runTest {
        val staticMapId = staticMapEntity3.id

        val staticMapOnline = staticMapOnlineEntity3.copy(storageUrl = "")

        val thrown = runCatching { repo.uploadStaticMap(staticMapOnline, staticMapId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseStaticMapUploadException::class.java)
        assertThat(thrown).hasCauseThat().isInstanceOf(IllegalArgumentException::class.java)
        assertThat(thrown).hasCauseThat().hasMessageThat().contains("StaticMap URI is empty")
    }

    @Test
    fun uploadStaticMap_firestoreFailure_wrapsInFirebaseStaticMapUploadException() = runTest {
        val staticMapId = staticMapEntity1.id

        val staticMapOnline = staticMapOnlineEntity1.copy(storageUrl = staticMapEntity1.uri)

        val fileUri = mockk<Uri>()
        every { fileUri.scheme } returns "file"
        every { any<String>().toUri() } returns fileUri

        val downloadUri = mockk<Uri>()
        every { downloadUri.toString() } returns "https://firebase.storage.com/$staticMapId.jpg"

        val uploadTask = mockk<UploadTask>()
        val uploadSnapshot = mockk<UploadTask.TaskSnapshot>()
        val downloadTask = mockk<com.google.android.gms.tasks.Task<Uri>>()

        every { storage.reference } returns storageRefRoot
        every { storageRefRoot.child("staticMaps/${staticMapId}.jpg") } returns storageRef
        every { storageRef.putFile(fileUri) } returns uploadTask
        coEvery { uploadTask.await() } returns uploadSnapshot
        every { storageRef.downloadUrl } returns downloadTask
        coEvery { downloadTask.await() } returns downloadUri

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.document(staticMapId) } returns document
        coEvery { document.set(any()).await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.uploadStaticMap(staticMapOnline, staticMapId)}.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseStaticMapUploadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun getStaticMap_success_returnsStaticMap() = runTest {
        val staticMapEntityId = staticMapOnlineEntity1.universalLocalId

        val snapshot = mockk<DocumentSnapshot>()

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.document(staticMapEntityId) } returns document
        coEvery { document.get().await() } returns snapshot

        every { snapshot.toObject(StaticMapOnlineEntity::class.java) } returns staticMapOnlineEntity1

        val result = repo.getStaticMap(staticMapEntityId)

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(staticMapOnlineEntity1)
    }

    @Test
    fun getStaticMap_noEntityFound_returnsNull() = runTest {
        val staticMapId = "99"

        val snapshot = mockk<DocumentSnapshot>()

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.document(staticMapId) } returns document
        coEvery { document.get().await() } returns snapshot

        every { snapshot.toObject(StaticMapOnlineEntity::class.java) } returns null

        val result = repo.getStaticMap(staticMapId)

        assertThat(result).isNull()
    }

    @Test
    fun getStaticMap_firestoreFailure_wrapsInFirebaseStaticMapDownloadException() = runTest {
        val staticMapId = staticMapOnlineEntity2.universalLocalId

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.document(staticMapId) } returns document
        coEvery { document.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.getStaticMap(staticMapId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseStaticMapDownloadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun getStaticMapByPropertyId_success_returnsSingleEntity() = runTest {

        // --- GIVEN ---
        val propertyId = staticMapOnlineEntity1.universalLocalPropertyId

        val expectedEntities = staticMapOnlineEntityList
            .filter { it.universalLocalPropertyId == propertyId }

        val snapshot = mockk<QuerySnapshot>()

        val docs = expectedEntities.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(StaticMapOnlineEntity::class.java) } returns entity
            }
        }

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.whereEqualTo("universalLocalPropertyId", propertyId) } returns query
        every { query.limit(1) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        // --- WHEN ---
        val result = repo.getStaticMapByPropertyId(propertyId)

        // --- THEN ---
        val expected = expectedEntities.first()

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun getStaticMapByPropertyId_noResult_returnsNull() = runTest {
        val propertyId = "42L"
        val snapshot = mockk<QuerySnapshot>()

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.whereEqualTo("universalLocalPropertyId", propertyId) } returns query
        every { query.limit(1) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getStaticMapByPropertyId(propertyId)

        assertThat(result).isNull()
    }

    @Test
    fun getStaticMapByPropertyId_firestoreFailure_throwsException() = runTest {
        val propertyId = staticMapOnlineEntity2.universalLocalPropertyId

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.whereEqualTo("universalLocalPropertyId", propertyId) } returns query
        every { query.limit(1) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching {
            repo.getStaticMapByPropertyId(propertyId)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseStaticMapDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("Firestore failed")
    }

    @Test
    fun getAllStaticMaps_success_returnsList() = runTest {
        val snapshot = mockk<QuerySnapshot>()

        val docs = staticMapOnlineEntityList.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(StaticMapOnlineEntity::class.java) } returns entity
                every { id } returns entity.universalLocalId
            }
        }

        every { firestore.collection(STATIC_MAPS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getAllStaticMaps()

        assertThat(result).hasSize(staticMapOnlineEntityList.size)

        result.forEachIndexed { index, actual ->
            val expected = staticMapOnlineEntityList[index]

            assertThat(actual.firebaseId).isEqualTo(expected.universalLocalId)

            assertThat(actual.staticMap.ownerUid).isEqualTo(expected.ownerUid)
            assertThat(actual.staticMap.universalLocalId).isEqualTo(expected.universalLocalId)
            assertThat(actual.staticMap.universalLocalPropertyId)
                .isEqualTo(expected.universalLocalPropertyId)
            assertThat(actual.staticMap.storageUrl).isEqualTo(expected.storageUrl)
            assertThat(actual.staticMap.updatedAt).isEqualTo(expected.updatedAt)
            assertThat(actual.staticMap.isDeleted).isEqualTo(expected.isDeleted)
        }
    }

    @Test
    fun getAllStaticMaps_noResults_returnsEmptyList() = runTest {
        val snapshot = mockk<QuerySnapshot>()

        every { firestore.collection(STATIC_MAPS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getAllStaticMaps()

        assertThat(result).isEmpty()
    }

    @Test
    fun getAllStaticMaps_firestoreFailure_wrapsInFirebaseStaticMapsDownloadException() = runTest {

        every { firestore.collection(STATIC_MAPS) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.getAllStaticMaps() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseStaticMapDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("Firestore failed")
    }

    @Test
    fun downloadImageLocally_success_downloadsFileAndReturnsLocalUri() = runTest {
        val storageUrl = "https://firebase.storage.com/staticMap_1.jpg"

        val storageRef = mockk<StorageReference>()
        val downloadTask = mockk<FileDownloadTask>()

        every { storage.getReferenceFromUrl(storageUrl) } returns storageRef
        every { storageRef.getFile(any<File>()) } returns downloadTask

        coEvery { downloadTask.await() } returns mockk()
        val result = repo.downloadImageLocally(storageUrl)

        verify(exactly = 1) {
            storage.getReferenceFromUrl(storageUrl)
        }

        verify(exactly = 1) {
            storageRef.getFile(any<File>())
        }

        assertThat(result).contains("staticMap_")
    }

    @Test
    fun downloadImageLocally_storageFailure_throwsException() = runTest {

        val storageUrl = "https://firebase.storage.com/static_map_1.jpg"

        val storageRef = mockk<StorageReference>()
        val downloadTask = mockk<FileDownloadTask>()

        every { storage.getReferenceFromUrl(storageUrl) } returns storageRef
        every { storageRef.getFile(any<File>()) } returns downloadTask

        coEvery { downloadTask.await() } throws RuntimeException("Download failed")

        val thrown = runCatching {
            repo.downloadImageLocally(storageUrl)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        assertThat(thrown!!.message).contains("Download failed")
    }

    @Test
    fun markStaticMapAsDeleted_success_updatesDocument() = runTest {
        val staticMapId = staticMapOnlineEntity1.universalLocalId
        val updatedAt = 1700000000000L

        val updateTask = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.document(staticMapId) } returns document
        every {
            document.update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to updatedAt
                )
            )
        } returns updateTask

        coEvery { updateTask.await() } returns mockk()

        repo.markStaticMapAsDeleted(staticMapId, updatedAt)

        verify(exactly = 1) {
            document.update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to updatedAt
                )
            )
        }
    }

    @Test
    fun markStaticMapAsDeleted_firestoreFailure_throwsException() = runTest {
        val staticMapId = staticMapOnlineEntity1.universalLocalId
        val updatedAt = 1700000000000L

        val updateTask = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(STATIC_MAPS) } returns collection
        every { collection.document(staticMapId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns updateTask

        coEvery { updateTask.await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching {
            repo.markStaticMapAsDeleted(staticMapId, updatedAt)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        assertThat(thrown!!.message).contains("Firestore failed")
    }
}