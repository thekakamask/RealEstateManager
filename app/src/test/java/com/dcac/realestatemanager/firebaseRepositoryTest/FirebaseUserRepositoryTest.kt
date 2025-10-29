package com.dcac.realestatemanager.firebaseRepositoryTest


import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserDeleteException
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserDownloadException
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserUploadException
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
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


    // --- Mocked Firebase objects ---
    private val firestore = mockk<FirebaseFirestore>()
    private val collection = mockk<CollectionReference>()
    private val document = mockk<DocumentReference>()
    private val query = mockk<Query>() // For uniqueness checks

    private lateinit var repo: UserOnlineRepository

    // --- Fake Data ---
    private val userOnlineEntity1 = FakeUserOnlineEntity.userOnline1
    private val firebaseUserDocument1 = FakeUserOnlineEntity.firestoreUserDocument1
    private val firebaseUserDocumentList = FakeUserOnlineEntity.firestoreUserDocumentList

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        repo = FirebaseUserOnlineRepository(firestore)

        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0

        // Common setup for most tests
        every { firestore.collection(FirestoreCollections.USERS) } returns collection
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun uploadUser_success_writesToFirestore_returnsEntity() = runTest {
        // Arrange: New user with unique email and roomId
        val userId = firebaseUserDocument1.id
        val userToUpload = firebaseUserDocument1.user
        val emptySnapshot = mockk<QuerySnapshot> { every { documents } returns emptyList() }

        // Mock uniqueness checks to return nothing
        every { collection.whereEqualTo(any<String>(), any()) } returns query
        coEvery { query.get().await() } returns emptySnapshot

        // Mock the document write operation
        every { collection.document(userId) } returns document
        coEvery { document.set(userToUpload).await() } returns mockk()

        // Act
        val result = repo.uploadUser(userToUpload, userId)

        // Assert
        assertThat(result).isEqualTo(userToUpload)
        coVerify { document.set(userToUpload).await() }
    }

    @Test
    fun uploadUser_failure_throwsFirebaseUserUploadException_whenEmailExists() = runTest {
        // Arrange: New user but their email already exists under a different ID
        val newUserUpload = userOnlineEntity1
        val newUserId = "new_user_id"
        val existingDocSnapshot = mockk<DocumentSnapshot> { every { id } returns "existing_user_id_123" }
        val querySnapshotWithEmail = mockk<QuerySnapshot> { every { documents } returns listOf(existingDocSnapshot) }

        // Mock the email check to find an existing user
        every { collection.whereEqualTo("email", newUserUpload.email) } returns query
        coEvery { query.get().await() } returns querySnapshotWithEmail

        // Act
        val thrown = runCatching {
            repo.uploadUser(newUserUpload, newUserId)
        }.exceptionOrNull()

        // Assert
        assertThat(thrown).isInstanceOf(FirebaseUserUploadException::class.java)
        assertThat(thrown?.message).contains("Email already in use")
    }

    @Test
    fun getUser_success_returnsFirestoreUserDocument() = runTest {
        // Arrange
        val userId = firebaseUserDocument1.id
        val snapshot = mockk<DocumentSnapshot>()
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(UserOnlineEntity::class.java) } returns firebaseUserDocument1.user
        every { snapshot.id } returns userId

        // Act
        val result = repo.getUser(userId)

        // Assert
        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(firebaseUserDocument1)
    }

    @Test
    fun getUser_noEntityFound_returnsNull() = runTest {
        // Arrange
        val userId = "404"
        val snapshot = mockk<DocumentSnapshot>()
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } returns snapshot
        every { snapshot.toObject(UserOnlineEntity::class.java) } returns null

        // Act
        val result = repo.getUser(userId)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun getUser_failure_throwsFirebaseUserDownloadException() = runTest {
        // Arrange
        val userId = "any_id"
        every { collection.document(userId) } returns document
        coEvery { document.get().await() } throws RuntimeException("get fail")

        // Act
        val thrown = runCatching { repo.getUser(userId) }.exceptionOrNull()

        // Assert
        assertThat(thrown).isInstanceOf(FirebaseUserDownloadException::class.java)
        assertThat(thrown?.cause?.message).isEqualTo("get fail")
    }

    @Test
    fun getAllUser_success_returnsListOfFirestoreUserDocument() = runTest {
        // Arrange
        val snapshot = mockk<QuerySnapshot>()
        val docs = firebaseUserDocumentList.map { firestoreDoc ->
            mockk<DocumentSnapshot>().apply {
                every { id } returns firestoreDoc.id
                every { toObject(UserOnlineEntity::class.java) } returns firestoreDoc.user
            }
        }
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns docs

        // Act
        val result = repo.getAllUsers()

        // Assert
        assertThat(result).hasSize(firebaseUserDocumentList.size)
        assertThat(result).containsExactlyElementsIn(firebaseUserDocumentList)
    }

    @Test
    fun getAllUser_noResults_returnsEmptyList() = runTest {
        // Arrange
        val snapshot = mockk<QuerySnapshot>()
        coEvery { collection.get().await() } returns snapshot
        every { snapshot.documents } returns emptyList()

        // Act
        val result = repo.getAllUsers()

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun getAllUser_failure_throwsFirebaseUserDownloadException() = runTest {
        // Arrange
        coEvery { collection.get().await() } throws RuntimeException("download fail")

        // Act
        val thrown = runCatching { repo.getAllUsers() }.exceptionOrNull()

        // Assert
        assertThat(thrown).isInstanceOf(FirebaseUserDownloadException::class.java)
        assertThat(thrown?.cause?.message).isEqualTo("download fail")
    }

    @Test
    fun deleteUser_success_deletesDocument() = runTest {
        // Arrange
        val userId = firebaseUserDocument1.id
        every { collection.document(userId) } returns document
        coEvery { document.delete().await() } returns mockk() // .await() on delete returns Void

        // Act
        repo.deleteUser(userId)

        // Assert
        coVerify { document.delete().await() }
    }

    @Test
    fun deleteUser_failure_throwsFirebaseUserDeleteException() = runTest {
        // Arrange
        val userId = firebaseUserDocument1.id
        every { collection.document(userId) } returns document
        coEvery { document.delete().await() } throws RuntimeException("delete fail")

        // Act
        val thrown = runCatching { repo.deleteUser(userId) }.exceptionOrNull()

        // Assert
        assertThat(thrown).isInstanceOf(FirebaseUserDeleteException::class.java)
        assertThat(thrown?.cause?.message).isEqualTo("delete fail")
    }
}