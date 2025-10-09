package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyOnlineEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PropertyDownloadManagerTest {

    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val propertyOnlineRepository = mockk<PropertyOnlineRepository>(relaxed = true)

    private lateinit var downloadManager: PropertyDownloadInterfaceManager

    private val propertyEntity1 = FakePropertyEntity.property1
    private val propertyEntity2 = FakePropertyEntity.property2
    private val propertyEntity3 = FakePropertyEntity.property3
    private val propertyEntityList = FakePropertyEntity.propertyEntityList

    private val propertyOnlineEntity1 = FakePropertyOnlineEntity.propertyEntity1
    private val propertyOnlineEntity2 = FakePropertyOnlineEntity.propertyEntity2
    private val propertyOnlineEntity3 = FakePropertyOnlineEntity.propertyEntity3
    private val propertyOnlineEntityList = FakePropertyOnlineEntity.propertyOnlineEntityList

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        downloadManager = PropertyDownloadManager(propertyRepository, propertyOnlineRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedProperty_localPropertyNull_downloadsAndInsertsProperty() = runTest {

        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(propertyOnlineEntity1)

        every { propertyRepository.getPropertyEntityById(propertyOnlineEntity1.roomId) } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success!!.userEmail).isEqualTo("Property ${propertyOnlineEntity1.roomId} downloaded")

        coVerify(exactly = 1) {
            propertyRepository.downloadPropertyFromFirebase(propertyOnlineEntity1)
        }
    }

    @Test
    fun downloadUnSyncedProperties_allPropertiesMissingLocally_downloadsAndInsertsAll() = runTest {

        coEvery { propertyOnlineRepository.getAllProperties() } returns propertyOnlineEntityList

        propertyOnlineEntityList.forEach {
            every { propertyRepository.getPropertyEntityById(it.roomId) } returns flowOf(null)
        }

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(propertyOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success.userEmail).isEqualTo("Property ${propertyOnlineEntityList[index].roomId} downloaded")
        }

        propertyOnlineEntityList.forEach {
            coVerify { propertyRepository.downloadPropertyFromFirebase(it) }
        }

    }

    @Test
    fun downloadUnSyncedProperties_localPropertyOutdated_downloadsAndUpdatesProperty() = runTest {
        val outdatedLocal = propertyEntity1.copy(updatedAt = 1700000000000)
        val updatedOnline = propertyOnlineEntity1.copy(updatedAt = 1700000002000)

        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(updatedOnline)
        every { propertyRepository.getPropertyEntityById(updatedOnline.roomId) } returns flowOf(outdatedLocal)

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val success = result[0] as SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success.userEmail).isEqualTo("Property ${updatedOnline.roomId} downloaded")

        coVerify(exactly = 1) {
            propertyRepository.downloadPropertyFromFirebase(updatedOnline)
        }
    }

    @Test
    fun downloadUnSyncedProperties_allPropertiesOutdatedLocally_downloadsAndUpdatesAll() = runTest {

        val outdatedLocalsProperties = propertyEntityList.mapIndexed { index, property ->
            property.copy(updatedAt = 1700000000000 + index)
        }

        val newerOnlineProperties = propertyOnlineEntityList.mapIndexed { index, property ->
            property.copy(updatedAt = 1700000000000 + index + 5)
        }

        coEvery { propertyOnlineRepository.getAllProperties() } returns newerOnlineProperties

        newerOnlineProperties.forEachIndexed { index, propertyOnline ->
            every { propertyRepository.getPropertyEntityById(propertyOnline.roomId) } returns flowOf(outdatedLocalsProperties[index])
        }

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(newerOnlineProperties.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success.userEmail).isEqualTo("Property ${newerOnlineProperties[index].roomId} downloaded")
        }

        newerOnlineProperties.forEach {
            coVerify { propertyRepository.downloadPropertyFromFirebase(it) }
        }
    }

    @Test
    fun downloadUnSyncedProperties_propertyAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(propertyOnlineEntity1)

        every { propertyRepository.getPropertyEntityById(propertyOnlineEntity1.roomId) } returns flowOf(propertyEntity1)

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)
        val success = result[0] as SyncStatus.Success
        assertThat(success).isNotNull()
        assertThat(success.userEmail).isEqualTo("Property ${propertyOnlineEntity1.roomId} already up-to-date")

        coVerify(exactly = 0) {
            propertyRepository.downloadPropertyFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedProperties_allPropertiesAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        coEvery { propertyOnlineRepository.getAllProperties() } returns propertyOnlineEntityList

        propertyOnlineEntityList.forEachIndexed { index, onlineProperty ->
            every { propertyRepository.getPropertyEntityById(onlineProperty.roomId) } returns flowOf(propertyEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(propertyOnlineEntityList.size)

        result.forEachIndexed { index, syncStatus ->
            val success = syncStatus as SyncStatus.Success
            assertThat(success).isNotNull()
            assertThat(success.userEmail).isEqualTo("Property ${propertyOnlineEntityList[index].roomId} already up-to-date")
        }

        coVerify(exactly = 0) {
            propertyRepository.downloadPropertyFromFirebase(any())
        }
    }

    @Test
    fun downloadUnSyncedProperties_mixedCases_returnsCorrectStatuses() = runTest {
        val propertyOnlineEntity4 = PropertyOnlineEntity(
            title = "error appart",
            type = "error type",
            price = 1000000,
            surface = 100,
            rooms = 1,
            description = "error description",
            address = "error address",
            entryDate = "2025-10-25",
            userId = 3L,
            updatedAt = 1700000008000,
            roomId = 4L

        )

        val propertyInsert = propertyOnlineEntity1
        val propertyUpdate = propertyOnlineEntity2.copy(updatedAt = 1700000006000)
        val propertySkip = propertyOnlineEntity3
        val propertyError = propertyOnlineEntity4.copy(updatedAt = 1700000008000)

        val outdatedLocal = propertyEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocal = propertyEntity3

        val onlineProperties = listOf(propertyInsert, propertyUpdate, propertySkip, propertyError)

        coEvery { propertyOnlineRepository.getAllProperties() } returns onlineProperties

        every { propertyRepository.getPropertyEntityById(propertyInsert.roomId) } returns flowOf(null)
        every { propertyRepository.getPropertyEntityById(propertyUpdate.roomId) } returns flowOf(outdatedLocal)
        every { propertyRepository.getPropertyEntityById(propertySkip.roomId) } returns flowOf(upToDateLocal)
        every { propertyRepository.getPropertyEntityById(propertyError.roomId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(4)

        val statusInsert = result[0] as? SyncStatus.Success
        assertThat(statusInsert).isNotNull()
        assertThat(statusInsert!!.userEmail).isEqualTo("Property ${propertyInsert.roomId} downloaded")

        // Check update
        val statusUpdate = result[1] as? SyncStatus.Success
        assertThat(statusUpdate).isNotNull()
        assertThat(statusUpdate!!.userEmail).isEqualTo("Property ${propertyUpdate.roomId} downloaded")

        // Check skip
        val statusSkip = result[2] as? SyncStatus.Success
        assertThat(statusSkip).isNotNull()
        assertThat(statusSkip!!.userEmail).isEqualTo("Property ${propertySkip.roomId} already up-to-date")

        // Check error
        val statusError = result[3] as? SyncStatus.Failure
        assertThat(statusError).isNotNull()
        assertThat(statusError!!.label).isEqualTo("Property ${propertyError.roomId} failed to sync")
        assertThat(statusError.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 1) { propertyRepository.downloadPropertyFromFirebase(propertyInsert) }
        coVerify(exactly = 1) { propertyRepository.downloadPropertyFromFirebase(propertyUpdate) }
        coVerify(exactly = 0) { propertyRepository.downloadPropertyFromFirebase(propertySkip) }
        coVerify(exactly = 0) { propertyRepository.downloadPropertyFromFirebase(propertyError) }
    }

    @Test
    fun downloadUnSyncedProperty_individualFailure_returnsPartialSuccessWithFailure() =  runTest {

        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(propertyOnlineEntity1)

        every { propertyRepository.getPropertyEntityById(propertyOnlineEntity1.roomId) } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure.label).isEqualTo("Property ${propertyOnlineEntity1.roomId} failed to sync")
        assertThat(failure.error.message).isEqualTo("DB crash")
    }

    @Test
    fun downloadUnSyncedProperty_globalFailure_returnsFailureStatus() = runTest {
        coEvery { propertyOnlineRepository.getAllProperties() } throws RuntimeException("Firestore down")

        val result = downloadManager.downloadUnSyncedProperties()
        assertThat(result).hasSize(1)

        val failure = result[0] as SyncStatus.Failure
        assertThat(failure).isNotNull()
        assertThat(failure.label).isEqualTo("Global PROPERTY download failed")
        assertThat(failure.error.message).isEqualTo("Firestore down")
    }

    @Test
    fun downloadUnSyncedProperties_noPropertiesOnline_reutrnsEmptyList() = runTest {
        coEvery { propertyOnlineRepository.getAllProperties() } returns emptyList()

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            propertyRepository.downloadPropertyFromFirebase(any())
        }
    }

}