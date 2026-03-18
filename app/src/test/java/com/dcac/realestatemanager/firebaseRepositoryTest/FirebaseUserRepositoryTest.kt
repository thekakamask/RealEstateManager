package com.dcac.realestatemanager.firebaseRepositoryTest


import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.USERS
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirestoreUserDocument
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.MockKAnnotations
import org.junit.Before
import io.mockk.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FirebaseUserRepositoryTest {


    private val firestore = mockk<FirebaseFirestore>()
    private val collection = mockk<CollectionReference>()
    private val document = mockk<DocumentReference>()

    private lateinit var repo: UserOnlineRepository

    private val userEntity1 = FakeUserEntity.user1
    private val userEntity2 = FakeUserEntity.user2
    private val userOnlineEntity1 = FakeUserOnlineEntity.userOnline1
    private val userOnlineEntity2 = FakeUserOnlineEntity.userOnline2
    private val userOnlineEntityList = FakeUserOnlineEntity.userOnlineEntityList



    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        repo = FirebaseUserOnlineRepository(firestore)

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0

    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadUser_success_writesToFirestore_returnsEntity() = runTest {
        val userId = userEntity1.id

        val query = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { firestore.collection(USERS) } returns collection

        every { collection.whereEqualTo("email", userOnlineEntity1.email) } returns query
        coEvery { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        every { collection.document(userId) } returns document
        coEvery { document.set(userOnlineEntity1).await() } returns null

        val result = repo.uploadUser(userOnlineEntity1, userId)

        assertThat(result).isEqualTo(userOnlineEntity1)

        coVerify {
            document.set(userOnlineEntity1).await()
        }
    }

    @Test
    fun uploadUser_failure_throwsFirebaseUserUploadException() = runTest {
        val userId = userEntity2.id

        val query = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { firestore.collection(USERS) } returns collection

        every { collection.whereEqualTo("email", userOnlineEntity2.email) } returns query
        coEvery { query.get().await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        every { collection.document(userId) } returns document
        coEvery { document.set(userOnlineEntity2).await() } throws RuntimeException("upload fail")

        val thrown = runCatching {
            repo.uploadUser(userOnlineEntity2, userId)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseUserUploadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("upload fail")
    }

    @Test
    fun getUser_success_returnsEntity() = runTest {
        val userId = userOnlineEntity1.universalLocalId
        val snapshot = mockk<DocumentSnapshot>()

        every { firestore.collection(USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.id } returns userId
        every { snapshot.toObject(UserOnlineEntity::class.java) } returns userOnlineEntity1

        val result = repo.getUser(userId)

        assertThat(result).isEqualTo(
            FirestoreUserDocument(
                firebaseId = userId,
                user = userOnlineEntity1
            )
        )
    }

    @Test
    fun getUser_noEntityFound_returnsNull() = runTest {
        val userId = "404"
        val snapshot = mockk<DocumentSnapshot>()
        every { firestore.collection(USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(UserOnlineEntity::class.java) } returns null

        val result = repo.getUser(userId)

        assertThat(result).isNull()
    }

    @Test
    fun getUser_failure_throwsFirebaseUserDownloadException() = runTest {
        val userId = userOnlineEntity2.universalLocalId
        every { firestore.collection(USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } throws RuntimeException("get fail")

        val thrown = runCatching { repo.getUser(userId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseUserDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("get fail")
    }

    @Test
    fun getAllUsers_success_returnList()= runTest {
        val snapshot = mockk<QuerySnapshot>()

        val docs = userOnlineEntityList.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(UserOnlineEntity::class.java) } returns entity
                every { id } returns entity.universalLocalId
            }
        }

        every { firestore.collection(USERS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getAllUsers()

        assertThat(result).hasSize(userOnlineEntityList.size)

        result.forEachIndexed { index, actual ->
            val expected = userOnlineEntityList[index]

            assertThat(actual.firebaseId).isEqualTo(expected.universalLocalId)
            assertThat(actual.user).isEqualTo(expected)
        }
    }

    @Test
    fun getAllUsers_noResults_returnsEmptyList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        every { firestore.collection(USERS)} returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getAllUsers()

        assertThat(result).isEmpty()
    }

    @Test
    fun getAllUsers_failure_throwsFirebaseUserDownloadException() = runTest {
        every { firestore.collection(USERS) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("download fail")

        val thrown = runCatching { repo.getAllUsers() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseUserDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("download fail")
    }

    @Test
    fun markUserAsDeleted_success_updatesDocument() = runTest {

        val userId = userOnlineEntity1.universalLocalId
        val updatedAt = 123L

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(USERS) } returns collection
        every { collection.document(userId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } returns mockk()

        repo.markUserAsDeleted(userId, updatedAt)

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
    fun markUserAsDeleted_firestoreFailure_throwsException() = runTest {

        val userId = userOnlineEntity1.universalLocalId
        val updatedAt = 123L

        val task = mockk<com.google.android.gms.tasks.Task<Void>>()

        every { firestore.collection(USERS) } returns collection
        every { collection.document(userId) } returns document
        every { document.update(any<Map<String, Any>>()) } returns task

        coEvery { task.await() } throws RuntimeException("update failed")

        val thrown = runCatching {
            repo.markUserAsDeleted(userId, updatedAt)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        assertThat(thrown!!.message).contains("update failed")
    }
}
