package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.property.PropertyUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyOnlineEntity
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PropertyUploadManagerTest {

    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val propertyOnlineRepository = mockk<PropertyOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: PropertyUploadInterfaceManager

    private val propertyEntity1 = FakePropertyEntity.property1
    private val propertyEntity2 = FakePropertyEntity.property2
    private val propertyEntity3 = FakePropertyEntity.property3
    private val propertyOnlineEntity1 = FakePropertyOnlineEntity.propertyOnline1

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkStatic(FirebaseAuth::class)

        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>()

        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        uploadManager = PropertyUploadManager(propertyRepository, propertyOnlineRepository)
    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedProperties_propertyNotDeleted_uploadsAndUpdatesRoom()= runTest {
        every {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        } returns flowOf(listOf(propertyEntity1))

        coEvery {
            propertyOnlineRepository.uploadProperty(any(), any())
        } returns propertyOnlineEntity1

        val result = uploadManager.syncUnSyncedProperties()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly("Property ${propertyEntity1.id} uploaded to Firebase")

        coVerify(exactly = 1) {
            propertyOnlineRepository.uploadProperty(
                any(),
                propertyEntity1.firestoreDocumentId!!
            )
        }

        val updatedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = 1) {
            propertyRepository.updatePropertyFromFirebase(
                capture(updatedProperties),
                propertyEntity1.firestoreDocumentId!!
            )
        }

        assertThat(updatedProperties.first().universalLocalId)
            .isEqualTo(propertyEntity1.id)

        coVerify(exactly = 0) {
            propertyRepository.deleteProperty(any())
        }
    }

    @Test
    fun uploadUnSyncedProperties_propertyMarkedDeleted_deletesFromFirebaseAndRoom()= runTest {
        every {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        } returns flowOf(listOf(propertyEntity3))

        coEvery {
            propertyOnlineRepository.markPropertyAsDeleted(any(), any())
        } returns Unit

        val result = uploadManager.syncUnSyncedProperties()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages)
            .containsExactly(
                "Property ${propertyEntity3.id} marked deleted online & removed locally"
            )

        coVerify(exactly = 1) {
            propertyOnlineRepository.markPropertyAsDeleted(
                propertyEntity3.firestoreDocumentId!!,
                propertyEntity3.updatedAt
            )
        }

        coVerify(exactly = 1) {
            propertyRepository.deleteProperty(propertyEntity3)
        }
        coVerify(exactly = 0) {
            propertyOnlineRepository.uploadProperty(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
    }

    @Test
    fun uploadUnSyncedProperties_globalFailure_returnsFailureStatus() = runTest {
        every {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        } throws RuntimeException("DB crash")

        try {
            uploadManager.syncUnSyncedProperties()
            throw AssertionError("Exception expected but not thrown")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("DB crash")
        }

        coVerify(exactly = 1) {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        }
        coVerify(exactly = 0) {
            propertyOnlineRepository.uploadProperty(any(), any())
        }
        coVerify(exactly = 0) {
            propertyOnlineRepository.markPropertyAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.deleteProperty(any())
        }
    }

    @Test
    fun uploadUnSyncedProperties_noPropertiesToUpload_returnsEmptyList() = runTest {
        every {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        } returns flowOf(emptyList())

        val result = uploadManager.syncUnSyncedProperties()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        }
        coVerify(exactly = 0) {
            propertyOnlineRepository.uploadProperty(any(), any())
        }
        coVerify(exactly = 0) {
            propertyOnlineRepository.markPropertyAsDeleted(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.deleteProperty(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {
        val propertyInsert = propertyEntity1
        val propertyDelete = propertyEntity3
        val propertyError = propertyEntity2

        every {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        } returns flowOf(listOf(propertyInsert, propertyDelete, propertyError))
        coEvery {
            propertyOnlineRepository.uploadProperty(any(), any())
        } returns propertyOnlineEntity1
        coEvery {
            propertyOnlineRepository.markPropertyAsDeleted(any(), any())
        } returns Unit
        coEvery {
            propertyOnlineRepository.uploadProperty(
                match { it.universalLocalId == propertyError.id },
                any()
            )
        } throws RuntimeException("upload failed")

        val result = uploadManager.syncUnSyncedProperties()

        assertThat(result).hasSize(3)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "Property ${propertyInsert.id} uploaded to Firebase",
            "Property ${propertyDelete.id} marked deleted online & removed locally"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Property ${propertyError.id}")
        assertThat(failure.error).hasMessageThat().isEqualTo("upload failed")

        coVerify(exactly = 2) {
            propertyOnlineRepository.uploadProperty(any(), any())
        }

        val updatedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = 1) {
            propertyRepository.updatePropertyFromFirebase(
                capture(updatedProperties),
                any()
            )
        }

        assertThat(updatedProperties.first().universalLocalId)
            .isEqualTo(propertyInsert.id)

        coVerify(exactly = 1) {
            propertyOnlineRepository.markPropertyAsDeleted(
                propertyDelete.firestoreDocumentId!!,
                propertyDelete.updatedAt
            )
        }

        coVerify(exactly = 1) {
            propertyRepository.deleteProperty(propertyDelete)
        }
    }
}
