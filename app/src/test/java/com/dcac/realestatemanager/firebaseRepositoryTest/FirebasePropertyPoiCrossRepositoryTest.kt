package com.dcac.realestatemanager.firebaseRepositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.FirebasePropertyPoiCrossDeleteException
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
    private val crossRefEntity3 = FakePropertyPoiCrossEntity.propertyPoiCross3
    private val crossRefEntity4 = FakePropertyPoiCrossEntity.propertyPoiCross4
    private val crossRefEntity5 = FakePropertyPoiCrossEntity.propertyPoiCross5
    private val crossRefEntity6 = FakePropertyPoiCrossEntity.propertyPoiCross6

    private val crossRefOnlineEntity1 = FakePropertyPoiCrossOnlineEntity.cross1
    private val crossRefOnlineEntity2 = FakePropertyPoiCrossOnlineEntity.cross2
    private val crossRefOnlineEntity3 = FakePropertyPoiCrossOnlineEntity.cross3
    private val crossRefOnlineEntity4 = FakePropertyPoiCrossOnlineEntity.cross4

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
        val crossRefId = "${crossRefEntity1.propertyId}-${crossRefEntity1.poiId}"

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.document(crossRefId) } returns document
        coEvery { document.set(crossRefOnlineEntity1).await() } returns null

        val result = repo.uploadCrossRef(crossRefOnlineEntity1)

        assertThat(result).isEqualTo(crossRefOnlineEntity1)
        coVerify { document.set(crossRefOnlineEntity1).await() }
    }

    @Test
    fun uploadCrossRef_failure_throwsFirebasePropertyPoiCrossUploadException() = runTest {
        val crossRefId = "${crossRefEntity2.propertyId}-${crossRefEntity2.poiId}"

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.document(crossRefId) } returns document
        coEvery { document.set(any()).await() } throws RuntimeException("upload fail")

        val thrown = runCatching {
            repo.uploadCrossRef(crossRefOnlineEntity2)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossUploadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("upload fail")

    }

    @Test
    fun getCrossRefByPropertyId_success_returnsCrossRef() = runTest {
        val propertyId = crossRefOnlineEntity1.propertyId

        val expectedEntities = crossRefOnlineEntityList
                .filter { it.propertyId == propertyId }

        val snapshot = mockk<QuerySnapshot>()

        val docs = expectedEntities.map { entity ->
            mockk<DocumentSnapshot>(). apply {
                every { toObject(PropertyPoiCrossOnlineEntity::class.java) } returns entity
                every { id } returns entity.roomId.toString()
            }
        }

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getCrossRefsByPropertyId(propertyId)

        assertThat(result).hasSize(expectedEntities.size)

        result.forEachIndexed { index, actual ->
            val expected = expectedEntities[index]

            assertThat(actual.propertyId).isEqualTo(propertyId)
            assertThat(actual.poiId).isEqualTo(expected.poiId)
            assertThat(actual.updatedAt).isEqualTo(expected.updatedAt)
            assertThat(actual.roomId).isEqualTo(expected.roomId)
        }
    }

    @Test
    fun getCrossRefByPropertyId_noResults_returnsEmptyList() = runTest {
        val propertyId = 42L
        val snapshot = mockk<QuerySnapshot>()

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getCrossRefsByPropertyId(propertyId)
        assertThat(result).isEmpty()
    }

    @Test
    fun getCrossRefByPropertyId_firestoreFailure_throwsFirebasePropertyPoiCrossDownloadException() = runTest {
        val propertyId = crossRefOnlineEntity2.propertyId

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.getCrossRefsByPropertyId(propertyId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDownloadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")

    }

    @Test
    fun getCrossRefByPoiId_success_returnsCrossRef() = runTest {
        val poiId = crossRefOnlineEntity3.poiId

        val expectedEntities = crossRefOnlineEntityList
            .filter { it.poiId == poiId }

        val snapshot = mockk<QuerySnapshot>()

        val docs = expectedEntities.map { entity ->
            mockk<DocumentSnapshot>(). apply {
                every { toObject(PropertyPoiCrossOnlineEntity::class.java) } returns entity
                every { id } returns entity.roomId.toString()
            }
        }

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("poiId", poiId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getCrossRefsByPoiId(poiId)

        assertThat(result).hasSize(expectedEntities.size)

        result.forEachIndexed { index, actual ->
            val expected = expectedEntities[index]

            assertThat(actual.propertyId).isEqualTo(expected.propertyId)
            assertThat(actual.poiId).isEqualTo(poiId)
            assertThat(actual.updatedAt).isEqualTo(expected.updatedAt)
            assertThat(actual.roomId).isEqualTo(expected.roomId)
        }
    }

    @Test
    fun getCrossRefByPoiId_noResults_returnsEmptyList() = runTest {
        val poiId = 42L
        val snapshot = mockk<QuerySnapshot>()

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("poiId", poiId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getCrossRefsByPoiId(poiId)

        assertThat(result).isEmpty()

    }


    @Test
    fun getCrossRefByPoiId_firestoreFailure_throwsFirebasePropertyPoiCrossDownloadException() = runTest {
        val poiId = crossRefOnlineEntity4.poiId

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("poiId", poiId) } returns query
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
                every { id } returns entity.roomId.toString()
            }
        }

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getAllCrossRefs()

        assertThat(result).hasSize(crossRefOnlineEntityList.size)


        result.forEachIndexed { index, actual ->
            val expected = crossRefOnlineEntityList[index]
            assertThat(actual.propertyId).isEqualTo(expected.propertyId)
            assertThat(actual.poiId).isEqualTo(expected.poiId)
            assertThat(actual.updatedAt).isEqualTo(expected.updatedAt)
            assertThat(actual.roomId).isEqualTo(expected.roomId)

        }

    }

    @Test
    fun getAllCrossRefs_noResults_returnsEmptyList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getAllCrossRefs()

        assertThat(result).isEmpty()

    }

    @Test
    fun getAllCrossRefs_firestoreFailure_throwsFirebasePropertyPoiCrossDownloadException() = runTest {
        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.getAllCrossRefs() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDownloadException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")

    }

    @Test
    fun deleteCrossRef_success_callsFirestoreDelete() = runTest {
        val crossRefId = "${crossRefEntity5.propertyId}-${crossRefEntity5.poiId}"


        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.document(crossRefId) } returns document
        coEvery { document.delete().await() } returns null

        repo.deleteCrossRef(crossRefEntity5.propertyId, crossRefEntity5.poiId)

        coVerify(exactly = 1) { document.delete().await() }
    }

    @Test
    fun deleteCrossRef_firestoreFailure_throwsException()= runTest {
        val crossRefId = "${crossRefEntity6.propertyId}-${crossRefEntity6.poiId}"



        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.document(crossRefId) } returns document
        coEvery { document.delete().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.deleteCrossRef(crossRefEntity6.propertyId, crossRefEntity6.poiId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDeleteException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")

    }

    @Test
    fun deleteAllCrossRefForProperty_success_deleteAllDocs()= runTest{
        val propertyId = crossRefEntity6.propertyId
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val ref1 = mockk<DocumentReference>()
        val ref2 = mockk<DocumentReference>()

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } returns snapshot

        every { snapshot.documents } returns listOf(doc1, doc2)


        every { doc1.reference } returns ref1
        every { doc2.reference } returns ref2

        coEvery { ref1.delete().await() } returns null
        coEvery { ref2.delete().await() } returns null

        repo.deleteAllCrossRefsForProperty(propertyId)

        coVerify { ref1.delete().await() }
        coVerify { ref2.delete().await() }
    }

    @Test
    fun deleteAllCrossRefForProperty_firestoreFailure_throwsException()= runTest{
        val propertyId = crossRefEntity3.propertyId

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("propertyId", propertyId) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.deleteAllCrossRefsForProperty(propertyId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDeleteException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")
    }

    @Test
    fun deleteAllCrossRefForPoi_success_deleteAllDocs()= runTest{
        val poiId = crossRefEntity5.poiId
        val snapshot = mockk<QuerySnapshot>()
        val doc1 = mockk<DocumentSnapshot>()
        val doc2 = mockk<DocumentSnapshot>()
        val ref1 = mockk<DocumentReference>()
        val ref2 = mockk<DocumentReference>()

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("poiId", poiId) } returns query
        coEvery { query.get().await() } returns snapshot
        every { snapshot.documents } returns listOf(doc1, doc2)

        every { doc1.reference } returns ref1
        every { doc2.reference } returns ref2

        coEvery { ref1.delete().await() } returns null
        coEvery { ref2.delete().await() } returns null

        repo.deleteAllCrossRefsForPoi(poiId)

        coVerify { ref1.delete().await() }
        coVerify { ref2.delete().await() }

    }

    @Test
    fun deleteAllCrossRefForPoi_firestoreFailure_throwsException()= runTest{
        val poiId = crossRefEntity4.propertyId

        every { firestore.collection(FirestoreCollections.PROPERTY_POI_CROSS) } returns collection
        every { collection.whereEqualTo("poiId", poiId) } returns query
        coEvery { query.get().await() } throws RuntimeException("Firestore failed")

        val thrown = runCatching { repo.deleteAllCrossRefsForPoi(poiId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebasePropertyPoiCrossDeleteException::class.java)
        assertThat(thrown!!.cause?.message).isEqualTo("Firestore failed")

    }
}
