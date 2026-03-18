package com.dcac.realestatemanager.firebaseRepositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.PROPERTY_POI_CROSS
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.FirebasePropertyPoiCrossDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.FirebasePropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.FirebasePropertyPoiCrossUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyPoiCrossOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FirebasePropertyPoiCrossRepositoryTest {

    //Mocks
    private val firestore = mockk<FirebaseFirestore>()
    private val collection = mockk<CollectionReference>()
    private val document = mockk<DocumentReference>()
    private val query = mockk<Query>()

    private lateinit var repo: PropertyPoiCrossOnlineRepository

    private val crossRefEntity1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRefEntity2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRefOnlineEntity1 = FakePropertyPoiCrossOnlineEntity.crossOnline1
    private val crossRefOnlineEntity2 = FakePropertyPoiCrossOnlineEntity.crossOnline2
    private val crossRefOnlineEntity3 = FakePropertyPoiCrossOnlineEntity.crossOnline3
    private val crossRefOnlineEntityList = FakePropertyPoiCrossOnlineEntity.propertyPoiCrossOnlineEntityList


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        repo = FirebasePropertyPoiCrossOnlineRepository(firestore)

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadCrossRef_success_uploadsEntityToFirestore_returnSyncCrossRef() = runTest {
        val crossRefId = "${crossRefEntity1.universalLocalPropertyId}-${crossRefEntity1.universalLocalPoiId}"

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.document(crossRefId) } returns document
        coEvery { document.set(crossRefOnlineEntity1).await() } returns null

        val result = repo.uploadCrossRef(crossRefOnlineEntity1)

        assertThat(result).isEqualTo(crossRefOnlineEntity1)
        coVerify { document.set(crossRefOnlineEntity1).await() }
    }

    @Test
    fun uploadCrossRef_failure_throwsFirebasePropertyPoiCrossUploadException() = runTest {
        val crossRefId = "${crossRefEntity2.universalLocalPropertyId}-${crossRefEntity2.universalLocalPoiId}"

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.document(crossRefId) } returns document
        coEvery { document.set(any()).await() } throws RuntimeException("upload fail")

        val thrown = runCatching {
            repo.uploadCrossRef(crossRefOnlineEntity2)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossUploadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("upload fail")

    }

    @Test
    fun getCrossRefsByPropertyId_success_returnsCrossRefs() = runTest {

        val propertyId = crossRefOnlineEntity1.universalLocalPropertyId

        val expected = crossRefOnlineEntityList
            .filter { it.universalLocalPropertyId == propertyId }

        val snapshot = mockk<QuerySnapshot>()

        val docs = expected.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(PropertyPoiCrossOnlineEntity::class.java) } returns entity
            }
        }

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("universalLocalPropertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getCrossRefsByPropertyId(propertyId)

        assertThat(result).hasSize(expected.size)
    }

    @Test
    fun getCrossRefByPropertyId_noResults_returnsEmptyList() = runTest {
        val propertyId = "42L"
        val snapshot = mockk<QuerySnapshot>()

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("universalLocalPropertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getCrossRefsByPropertyId(propertyId)
        assertThat(result).isEmpty()
    }

    @Test
    fun getCrossRefByPropertyId_firestoreFailure_throwsFirebasePropertyPoiCrossDownloadException() = runTest {
        val propertyId = crossRefOnlineEntity2.universalLocalPropertyId

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("universalLocalPropertyId", propertyId) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.getCrossRefsByPropertyId(propertyId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDownloadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")

    }

    @Test
    fun getCrossRefsByPoiId_success_returnsCrossRefs() = runTest {

        val poiId = crossRefOnlineEntity1.universalLocalPoiId

        val expected = crossRefOnlineEntityList
            .filter { it.universalLocalPoiId == poiId }

        val snapshot = mockk<QuerySnapshot>()

        val docs = expected.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(PropertyPoiCrossOnlineEntity::class.java) } returns entity
            }
        }

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("universalLocalPoiId", poiId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getCrossRefsByPoiId(poiId)

        assertThat(result).hasSize(expected.size)
    }

    @Test
    fun getCrossRefByPoiId_noResults_returnsEmptyList() = runTest {
        val poiId = "42L"
        val snapshot = mockk<QuerySnapshot>()

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("universalLocalPoiId", poiId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getCrossRefsByPoiId(poiId)

        assertThat(result).isEmpty()

    }


    @Test
    fun getCrossRefByPoiId_firestoreFailure_throwsFirebasePropertyPoiCrossDownloadException() = runTest {
        val poiId = crossRefOnlineEntity3.universalLocalPoiId

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("universalLocalPoiId", poiId) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.getCrossRefsByPoiId(poiId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDownloadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun getAllCrossRefs_success_returnsAllCrossRefs() = runTest {

        val snapshot = mockk<QuerySnapshot>()

        val docs = crossRefOnlineEntityList.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(PropertyPoiCrossOnlineEntity::class.java) } returns entity
                every { id } returns
                        "${entity.universalLocalPropertyId}-${entity.universalLocalPoiId}"
            }
        }

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getAllCrossRefs()

        assertThat(result).hasSize(crossRefOnlineEntityList.size)

        result.forEachIndexed { index, actual ->
            val expected = crossRefOnlineEntityList[index]

            assertThat(actual.cross).isEqualTo(expected)
        }
    }

    @Test
    fun getAllCrossRefs_noResults_returnsEmptyList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getAllCrossRefs()

        assertThat(result).isEmpty()

    }

    @Test
    fun getAllCrossRefs_firestoreFailure_throwsFirebasePropertyPoiCrossDownloadException() = runTest {
        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.getAllCrossRefs() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDownloadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun markCrossRefAsDeleted_success_updatesDocument() = runTest {

        val propertyId = crossRefOnlineEntity1.universalLocalPropertyId
        val poiId = crossRefOnlineEntity1.universalLocalPoiId
        val updatedAt = 123L

        val docId = "$propertyId-$poiId"

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.document(docId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } returns mockk()

        repo.markCrossRefAsDeleted(poiId, propertyId, updatedAt)

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
    fun markCrossRefAsDeleted_firestoreFailure_throwsException() = runTest {

        val propertyId = crossRefOnlineEntity1.universalLocalPropertyId
        val poiId = crossRefOnlineEntity1.universalLocalPoiId
        val updatedAt = 123L

        val docId = "$propertyId-$poiId"

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(PROPERTY_POI_CROSS) } returns collection
        every { collection.document(docId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } throws RuntimeException("update failed")

        val thrown = runCatching {
            repo.markCrossRefAsDeleted(poiId, propertyId, updatedAt)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        assertThat(thrown!!.message).contains("update failed")
    }

}
