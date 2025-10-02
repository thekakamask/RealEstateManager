package com.dcac.realestatemanager.firebaseRepositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserDeleteException
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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

    // --- Mocked Firebase objects ---
    private val firestore = mockk<FirebaseFirestore>() // Fake Firestore database root
    private val collection = mockk<CollectionReference>() // Fake reference to a Firestore collection
    private val document = mockk<DocumentReference>() // Fake reference to a single Firestore document

    private lateinit var repo: UserOnlineRepository

    private val userEntity1 = FakeUserEntity.user1
    private val userEntity2 = FakeUserEntity.user2
    private val userEntity3 = FakeUserEntity.user3
    private val userOnlineEntity1 = FakeUserOnlineEntity.userOnline1
    private val userOnlineEntity2 = FakeUserOnlineEntity.userOnline2
    private val userOnlineEntityList = FakeUserOnlineEntity.userOnlineEntityList

    @Before
    fun setup(){
        // Initialize all MockK annotations (relaxUnitFun = true means void/unit functions are auto-stubbed)
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Create repository with mocked Firestore (instead of real Firebase)
        repo = FirebaseUserOnlineRepository(firestore)

        // 🔑 Mock Firebase "await()" extension function from kotlinx.coroutines.tasks
        // Without this, calling .await() on Firestore Task<T> would crash in JVM tests
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        // 🔑 Mock Android Log.d() to avoid "Method d in android.util.Log not mocked" crash
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0 // Always return 0 instead of printing logs
    }

    @After
    fun tearDown(){
        // Clean up all mocks after each test to avoid memory leaks or conflicts
        unmockkAll()
    }

    @Test
    fun uploadUser_success_writesToFirestore_returnsEntity() = runTest {
        val userId = userEntity1.id.toString()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.set(userOnlineEntity1).await() } returns null

        val result = repo.uploadUser(userOnlineEntity1, userId)

        assertThat(result).isEqualTo(userOnlineEntity1)
        coVerify { document.set(userOnlineEntity1).await() }
    }

    @Test
    fun uploadUser_failure_throwsFirebaseUserUploadException() = runTest {
        val userId = userEntity2.id.toString()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.set(any()).await() } throws RuntimeException("upload fail")

        val thrown = runCatching {
            repo.uploadUser(userOnlineEntity1, userId)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseUserUploadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("upload fail")


    }

    @Test
    fun getUser_success_returnsEntity() = runTest {
        val userId = userOnlineEntity1.roomId.toString()
        val snapshot = mockk<DocumentSnapshot>()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(UserOnlineEntity::class.java) } returns userOnlineEntity1

        val result = repo.getUser(userId)

        assertThat(result).isEqualTo(userOnlineEntity1)

    }

    @Test
    fun getUser_noEntityFound_returnsNull() = runTest {
        val userId = "404"
        val snapshot = mockk<DocumentSnapshot>()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(UserOnlineEntity::class.java) } returns null

        val result = repo.getUser(userId)
        assertThat(result).isNull()
    }

    @Test
    fun getUser_failure_throwsFirebaseUserDownloadException() = runTest {
        val userId = userOnlineEntity2.roomId.toString()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } throws RuntimeException("get fail")

        val thrown = runCatching { repo.getUser(userId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseUserDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("get fail")

    }

    @Test
    fun getAllUser_success_returnsList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        val docs = userOnlineEntityList.map { entity ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(UserOnlineEntity::class.java) } returns entity
            }
        }

        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        val result = repo.getAllUsers()

        assertThat(result).hasSize(userOnlineEntityList.size)
        assertThat(result).containsExactlyElementsIn(userOnlineEntityList)

    }

    @Test
    fun getAllUser_noResults_returnsEmptyList() = runTest {
        val snapshot = mockk<QuerySnapshot>()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        val result = repo.getAllUsers()

        assertThat(result).isEmpty()
    }

    @Test
    fun getAllUser_failure_throwsFirebasePoiDownloadException() = runTest {
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        coEvery { collection.get().await() } throws RuntimeException("download fail")

        val thrown = runCatching { repo.getAllUsers() }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseUserDownloadException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("download fail")
    }

    @Test
    fun deleteUser_success_deletesDocument() = runTest {
        val userId = userEntity3.id.toString()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.delete().await() } returns null

        repo.deleteUser(userId)

        coVerify { document.delete().await() }
    }

    @Test
    fun deleteUser_failure_throwsFirebaseUserDeleteException() = runTest {
        val userId = userEntity1.id.toString()
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
        every { collection.document(userId) } returns document
        coEvery { document.delete().await() } throws RuntimeException("delete fail")

        val thrown = runCatching { repo.deleteUser(userId) }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(FirebaseUserDeleteException::class.java)
        assertThat(thrown!!.cause!!.message).isEqualTo("delete fail")


    }
}