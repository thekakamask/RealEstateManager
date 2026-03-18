package com.dcac.realestatemanager.firebaseRepositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.property.FirebasePropertyDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.property.FirebasePropertyOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.FirebasePropertyUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FirebasePropertyRepositoryTest {

    // --- Mocked Firebase objects ---
    private val firestore = mockk<FirebaseFirestore>() // Fake Firestore database root
    private val collection = mockk<CollectionReference>() // Fake reference to a Firestore collection
    private val document = mockk<DocumentReference>() // Fake reference to a single Firestore document

    // Repository under test
    private lateinit var repo: PropertyOnlineRepository

    private val propertyEntity1 = FakePropertyEntity.property1
    private val propertyEntity2 = FakePropertyEntity.property2
    private val propertyOnlineEntity1 = FakePropertyOnlineEntity.propertyOnline1
    private val propertyOnlineEntity2 = FakePropertyOnlineEntity.propertyOnline2
    private val propertyOnlineEntityList = FakePropertyOnlineEntity.propertyOnlineEntityList


    @Before
    fun setup() {
        // Initialize all MockK annotations (relaxUnitFun = true means void/unit functions are auto-stubbed)
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Create repository with mocked Firestore (instead of real Firebase)
        repo = FirebasePropertyOnlineRepository(firestore)

        // 🔑 Mock Firebase "await()" extension function from kotlinx.coroutines.tasks
        // Without this, calling .await() on Firestore Task<T> would crash in JVM tests
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        // 🔑 Mock Android Log.d() to avoid "Method d in android.util.Log not mocked" crash
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0 // Always return 0 instead of printing logs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadProperty_success_writesToFirestore_returnsEntity() = runTest {
        val propertyId = propertyEntity1.id

        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        every { collection.document(propertyId) } returns document
        coEvery { document.set(propertyOnlineEntity1).await() } returns null

        val result = repo.uploadProperty(propertyOnlineEntity1, propertyId)

        assertThat(result).isEqualTo(propertyOnlineEntity1)
        coVerify { document.set(propertyOnlineEntity1).await() }
    }

    @Test
    fun uploadProperty_failure_throwsFirebasePropertyUploadException() = runTest {
        val propertyId = propertyEntity2.id
        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        every { collection.document(propertyId) } returns document
        coEvery { document.set(any()).await() } throws RuntimeException("upload fail")

        val thrown = runCatching {
            repo.uploadProperty(propertyOnlineEntity2, propertyId)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyUploadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("upload fail")
    }

    @Test
    fun getProperty_success_returnsEntity() = runTest {
        val propertyId = propertyOnlineEntity1.universalLocalId
        val snapshot = mockk<DocumentSnapshot>()
        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        every { collection.document(propertyId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(PropertyOnlineEntity::class.java) } returns propertyOnlineEntity1

        val result = repo.getProperty(propertyId)

        assertThat(result).isEqualTo(propertyOnlineEntity1)
    }

    @Test
    fun getProperty_noEntityFound_returnsNull() = runTest {
        val propertyId = "404"
        val snapshot = mockk<DocumentSnapshot>()
        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        every { collection.document(propertyId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(PropertyOnlineEntity::class.java) } returns null

        val result = repo.getProperty(propertyId)

        assertThat(result).isNull()
    }

    @Test
    fun getProperty_failure_throwsFirebasePropertyDownloadException() = runTest {
        val propertyId = propertyOnlineEntity2.universalLocalId
        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        every { collection.document(propertyId) } returns document
        coEvery { document.get().await() } throws RuntimeException("get fail")

        val thrown = runCatching { repo.getProperty(propertyId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("get fail")
    }

    @Test
    fun getAllProperties_success_returnsList() = runTest {

        val snapshot = mockk<QuerySnapshot>()

        val docs = propertyOnlineEntityList.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(PropertyOnlineEntity::class.java) } returns entity
                every { id } returns entity.universalLocalId
            }
        }

        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getAllProperties()

        assertThat(result).hasSize(propertyOnlineEntityList.size)

        result.forEachIndexed { index, actual ->
            val expected = propertyOnlineEntityList[index]

            assertThat(actual.firebaseId).isEqualTo(expected.universalLocalId)
            assertThat(actual.property).isEqualTo(expected)
        }
    }

    @Test
    fun getAllProperties_noResults_returnsEmptyList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getAllProperties()

        assertThat(result).isEmpty()
    }

    @Test
    fun getAllProperties_failure_throwsFirebasePropertyDownloadException() = runTest {
        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("download fail")

        val thrown = runCatching { repo.getAllProperties() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("download fail")
    }


    @Test
    fun markPropertyAsDeleted_success_updatesDocument() = runTest {

        val propertyId = propertyOnlineEntity1.universalLocalId
        val updatedAt = 123L

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        every { collection.document(propertyId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } returns mockk()

        repo.markPropertyAsDeleted(propertyId, updatedAt)

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
    fun markPropertyAsDeleted_firestoreFailure_throwsException() = runTest {

        val propertyId = propertyOnlineEntity1.universalLocalId
        val updatedAt = 123L

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(FirestoreCollections.PROPERTIES) } returns collection
        every { collection.document(propertyId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } throws RuntimeException("update failed")

        val thrown = runCatching {
            repo.markPropertyAsDeleted(propertyId, updatedAt)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        assertThat(thrown!!.message).contains("update failed")
    }

}
