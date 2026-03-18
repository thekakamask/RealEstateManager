package com.dcac.realestatemanager.firebaseRepositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.poi.FirebasePoiDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.poi.FirebasePoiOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.poi.FirebasePoiUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePoiOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.test.*
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import com.google.firebase.firestore.QuerySnapshot

@RunWith(JUnit4::class)
class FirebasePoiRepositoryTest {

    // --- Mocked Firebase objects ---
    private val firestore = mockk<FirebaseFirestore>() // Fake Firestore database root
    private val collection = mockk<CollectionReference>() // Fake reference to a Firestore collection
    private val document = mockk<DocumentReference>() // Fake reference to a single Firestore document

    // Repository under test
    private lateinit var repo: PoiOnlineRepository

    private val poiEntity1 = FakePoiEntity.poi1
    private val poiEntity2 = FakePoiEntity.poi2
    private val poiOnlineEntity1 = FakePoiOnlineEntity.poiOnline1
    private val poiOnlineEntity2 = FakePoiOnlineEntity.poiOnline2
    private val poiOnlineEntityList = FakePoiOnlineEntity.poiOnlineEntityList

    @Before
    fun setup(){
        // Initialize all MockK annotations (relaxUnitFun = true means void/unit functions are auto-stubbed)
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Create repository with mocked Firestore (instead of real Firebase)
        repo = FirebasePoiOnlineRepository(firestore)

        // 🔑 Mock Firebase "await()" extension function from kotlinx.coroutines.tasks
        // Without this, calling .await() on Firestore Task<T> would crash in JVM tests
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        // 🔑 Mock Android Log.d() to avoid "Method d in android.util.Log not mocked" crash
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0 // Always return 0 instead of printing logs
    }

    @After
    fun tearDown() {
        // Clean up all mocks after each test to avoid memory leaks or conflicts
        unmockkAll()
    }

    @Test
    fun uploadPoi_success_writesToFirestore_returnsEntity() = runTest {
        val poiId = poiEntity1.id
        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        every { collection.document(poiId) } returns document
        every { document.set(any<Map<String, Any>>()) } returns mockk()
        coEvery { document.set(any<Map<String, Any>>()).await() } returns null

        val result = repo.uploadPoi(poiOnlineEntity1, poiId)

        assertThat(result).isEqualTo(poiOnlineEntity1)
        verify { document.set(any<Map<String, Any>>()) }
    }

    @Test
    fun uploadPoi_failure_throwsFirebasePoiUploadException() = runTest {
        val poiId = poiEntity2.id
        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        every { collection.document(poiId) } returns document
        coEvery { document.set(any()).await() } throws RuntimeException("upload fail")

        val thrown = runCatching {
            repo.uploadPoi(poiOnlineEntity1, poiId)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePoiUploadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("upload fail")
    }

    @Test
    fun getPoi_success_returnsEntity() = runTest {
        val poiId = poiOnlineEntity1.universalLocalId
        val snapshot = mockk<DocumentSnapshot>()
        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        every { collection.document(poiId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(PoiOnlineEntity::class.java) } returns poiOnlineEntity1

        val result = repo.getPoi(poiId)

        assertThat(result).isEqualTo(poiOnlineEntity1)
    }

    @Test
    fun getPoi_noEntityFound_returnsNull() = runTest {
        val poiId = "404"
        val snapshot = mockk<DocumentSnapshot>()
        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        every { collection.document(poiId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(PoiOnlineEntity::class.java) } returns null

        val result = repo.getPoi(poiId)

        assertThat(result).isNull()
    }

    @Test
    fun getPoi_failure_throwsFirebasePoiDownloadException() = runTest {
        val poiId = poiOnlineEntity2.universalLocalId
        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        every { collection.document(poiId) } returns document
        coEvery { document.get().await() } throws RuntimeException("get fail")

        val thrown = runCatching { repo.getPoi(poiId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePoiDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("get fail")
    }

    @Test
    fun getAllPoiS_success_returnsList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val docs = poiOnlineEntityList.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(PoiOnlineEntity::class.java) } returns entity
                every { id } returns entity.universalLocalId
            }
        }

        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getAllPoiS()

        assertThat(result).hasSize(poiOnlineEntityList.size)

        result.forEachIndexed { index, actual ->
            val expected = poiOnlineEntityList[index]

            assertThat(actual.poi).isEqualTo(expected)
        }
    }

    @Test
    fun getAllPoiS_noResults_returnsEmptyList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getAllPoiS()

        assertThat(result).isEmpty()
    }

    @Test
    fun getAllPoiS_failure_throwsFirebasePoiDownloadException() = runTest {
        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("download fail")

        val thrown = runCatching { repo.getAllPoiS() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePoiDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("download fail")
    }

    @Test
    fun markPoiAsDeleted_success_updatesDocument() = runTest {

        val poiId = poiOnlineEntity1.universalLocalId
        val updatedAt = 123L

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        every { collection.document(poiId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } returns mockk()

        repo.markPoiAsDeleted(poiId, updatedAt)

        verify {
            document.update(
                match {
                    it["isDeleted"] == true &&
                            it["updatedAt"] == updatedAt
                }
            )
        }
    }

    @Test
    fun markPoiAsDeleted_firestoreFailure_throwsException() = runTest {

        val poiId = poiOnlineEntity1.universalLocalId
        val updatedAt = 123L

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(FirestoreCollections.POIS) } returns collection
        every { collection.document(poiId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } throws RuntimeException("update fail")

        val thrown = runCatching {
            repo.markPoiAsDeleted(poiId, updatedAt)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        assertThat(thrown!!.message).contains("update fail")
    }

}
