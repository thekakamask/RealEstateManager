package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.property.PropertyUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyOnlineEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

    private val propertyOnlineEntity1 = FakePropertyOnlineEntity.propertyEntity1
    private val propertyOnlineEntity2 = FakePropertyOnlineEntity.propertyEntity2

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        uploadManager = PropertyUploadManager(propertyRepository, propertyOnlineRepository)
    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedProperties_propertyNotDeleted_uploadsAndUpdatesRoom()= runTest {
        coEvery { propertyRepository.uploadUnSyncedPropertiesToFirebase() } returns flowOf(listOf(propertyEntity1))
        coEvery { propertyOnlineRepository.uploadProperty(any(), any()) } returns propertyOnlineEntity1

        val result = uploadManager.syncUnSyncedProperties()

        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("Property ${propertyEntity1.id} uploaded")

        coVerify {
            propertyOnlineRepository.uploadProperty(any(), propertyEntity1.id.toString())
            propertyRepository.downloadPropertyFromFirebase(propertyOnlineEntity1)
        }
    }

    @Test
    fun uploadUnSyncedProperties_propertyMarkedDeleted_deletesFromFirebaseAndRoom()= runTest {
        val deletedProperty = propertyEntity3
        coEvery { propertyRepository.uploadUnSyncedPropertiesToFirebase() } returns flowOf(listOf(deletedProperty))

        val result = uploadManager.syncUnSyncedProperties()

        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("Property ${deletedProperty.id} deleted")

        coVerify {
            propertyOnlineRepository.deleteProperty(deletedProperty.id.toString())
            propertyRepository.deleteProperty(deletedProperty)

        }
    }

    @Test
    fun uploadUnSyncedProperties_globalFailure_returnsFailureStatus() = runTest {
        coEvery { propertyRepository.uploadUnSyncedPropertiesToFirebase() } throws RuntimeException("Room is down")

        val result = uploadManager.syncUnSyncedProperties()

        assertThat(result).hasSize(1)
        val failure = result[0] as? SyncStatus.Failure
        assertThat(failure!!.label).isEqualTo("Global download sync failed")
        assertThat(failure.error.message).isEqualTo("Room is down")
    }

    @Test
    fun uploadUnSyncedProperties_noPropertiesToUpload_returnsEmptyList() = runTest {
        coEvery { propertyRepository.uploadUnSyncedPropertiesToFirebase() } returns flowOf(emptyList())

        val result = uploadManager.syncUnSyncedProperties()

        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            propertyOnlineRepository.uploadProperty(any(), any())
            propertyRepository.downloadPropertyFromFirebase(any())
            propertyOnlineRepository.deleteProperty(any())
            propertyRepository.deleteProperty(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {
        val notSyncedNotDeleted = propertyEntity1
        val alreadySyncedNotDeleted = propertyEntity2
        val notSyncedDeleted = propertyEntity3

        coEvery {
            propertyRepository.uploadUnSyncedPropertiesToFirebase()
        } returns flowOf(listOf(notSyncedNotDeleted, notSyncedDeleted))

        coEvery {
            propertyOnlineRepository.uploadProperty(any(), notSyncedNotDeleted.id.toString())
        } returns propertyOnlineEntity1

        val result = uploadManager.syncUnSyncedProperties()

        assertThat(result).hasSize(2)

        val uploaded = result.find { it is SyncStatus.Success && it.userEmail == "Property ${notSyncedNotDeleted.id} uploaded" }
        val deleted = result.find { it is SyncStatus.Success && it.userEmail == "Property ${notSyncedDeleted.id} deleted" }

        assertThat(uploaded).isNotNull()
        assertThat(deleted).isNotNull()

        coVerify { propertyOnlineRepository.deleteProperty(notSyncedDeleted.id.toString()) }
        coVerify { propertyRepository.deleteProperty(notSyncedDeleted) }
        coVerify { propertyOnlineRepository.uploadProperty(any(), notSyncedNotDeleted.id.toString()) }
        coVerify { propertyRepository.downloadPropertyFromFirebase(propertyOnlineEntity1) }

        coVerify(exactly = 0) {
            propertyOnlineRepository.uploadProperty(any(), alreadySyncedNotDeleted.id.toString())
        }
        coVerify(exactly = 0) {
            propertyRepository.downloadPropertyFromFirebase(propertyOnlineEntity2)
        }
    }

}