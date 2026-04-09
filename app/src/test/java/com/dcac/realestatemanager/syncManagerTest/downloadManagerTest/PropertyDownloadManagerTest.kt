package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.notification.SyncNotificationHelper
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
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
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val syncNotificationHelper = mockk<SyncNotificationHelper>(relaxed = true)

    private lateinit var downloadManager: PropertyDownloadInterfaceManager

    private val propertyEntity1 = FakePropertyEntity.property1
    private val propertyEntity2 = FakePropertyEntity.property2
    private val propertyEntity3 = FakePropertyEntity.property3
    private val propertyEntityList = FakePropertyEntity.propertyEntityList
    private val propertyEntityListNotDeleted = FakePropertyEntity.propertyEntityListNotDeleted
    private val propertyOnlineEntity1 = FakePropertyOnlineEntity.propertyOnline1
    private val propertyOnlineEntity2 = FakePropertyOnlineEntity.propertyOnline2
    private val propertyOnlineEntity3 = FakePropertyOnlineEntity.propertyOnline3
    private val propertyOnlineEntityListNotDeleted = FakePropertyOnlineEntity.propertyOnlineEntityListNotDeleted
    private val firestorePropertyDocument1 = FakePropertyOnlineEntity.firestorePropertyDocument1
    private val firestorePropertyDocument2 = FakePropertyOnlineEntity.firestorePropertyDocument2
    private val firestorePropertyDocument3 = FakePropertyOnlineEntity.firestorePropertyDocument3


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        downloadManager = PropertyDownloadManager(propertyRepository, propertyOnlineRepository, syncNotificationHelper, userRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadUnSyncedProperty_localPropertyNull_downloadsAndInsertsPropertyExceptDeleted() = runTest {
        val propertyId = propertyOnlineEntity1.universalLocalId

        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(firestorePropertyDocument1)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyId)
        } returns flowOf(null)

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Property $propertyId inserted")

        val insertedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = 1) {
            propertyRepository.insertPropertyInsertFromFirebase(
                capture(insertedProperties),
                firestorePropertyDocument1.firebaseId
            )
        }

        assertThat(insertedProperties.first().universalLocalId).isEqualTo(propertyId)

        coVerify(exactly = 0 ) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedProperties_allPropertiesMissingLocally_downloadsAndInsertsAll() = runTest {
        val firestoreDocs = listOf(
            firestorePropertyDocument1,
            firestorePropertyDocument2,
            firestorePropertyDocument3
        )

        coEvery { propertyOnlineRepository.getAllProperties() } returns firestoreDocs

        firestoreDocs.forEach { doc ->
            every {
                propertyRepository.getPropertyByIdIncludeDeleted(doc.property.universalLocalId)
            } returns flowOf(null)
        }

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(2)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly(
            "Property ${propertyEntity1.id} inserted",
            "Property ${propertyEntity2.id} inserted"
        )

        val insertedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = 2) {
            propertyRepository.insertPropertyInsertFromFirebase(
                capture(insertedProperties),
                any()
            )
        }

        val insertedIds = insertedProperties.map { it.universalLocalId }

        assertThat(insertedIds)
            .containsExactly(propertyEntity1.id, propertyEntity2.id)

    }

    @Test
    fun downloadUnSyncedProperties_localPropertyOutdated_downloadsAndUpdatesProperty() = runTest {
        val outdatedLocalProperty = propertyEntity1.copy(updatedAt = 1700000000000)
        val updatedOnlineProperty = propertyOnlineEntity1.copy(updatedAt = 1700000002000)
        val propertyId = updatedOnlineProperty.universalLocalId

        val firestoreDoc = firestorePropertyDocument1.copy(
            property = updatedOnlineProperty
        )

        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(firestoreDoc)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyId)
        } returns flowOf(outdatedLocalProperty)

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Property $propertyId updated")

        val updatedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = 1) {
            propertyRepository.updatePropertyFromFirebase(
                capture(updatedProperties),
                firestoreDoc.firebaseId
            )
        }

        assertThat(updatedProperties.first().universalLocalId).isEqualTo(propertyId)

        coVerify(exactly = 0) {
            propertyRepository.insertPropertyInsertFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedProperties_allPropertiesOutdatedLocally_downloadsAndUpdatesAll() = runTest {
        val outdatedLocalProperties = propertyEntityListNotDeleted.mapIndexed { index, property ->
            property.copy(updatedAt = 1700000000000 + index)
        }
        val newerOnlineProperties = propertyOnlineEntityListNotDeleted.mapIndexed { index, property ->
            property.copy(updatedAt = 1700000000000 + index + 5)
        }
        val baseDocs = listOf(
            firestorePropertyDocument1,
            firestorePropertyDocument2
        )
        val firestoreDocs = baseDocs.mapIndexed { index, doc ->
            doc.copy(property = newerOnlineProperties[index])
        }

        coEvery { propertyOnlineRepository.getAllProperties() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                propertyRepository.getPropertyByIdIncludeDeleted(doc.property.universalLocalId)
            } returns flowOf(outdatedLocalProperties[index])
        }

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "Property ${it.property.universalLocalId} updated"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        val updatedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = firestoreDocs.size) {
            propertyRepository.updatePropertyFromFirebase(
                capture(updatedProperties),
                any()
            )
        }

        assertThat(updatedProperties.map { it.universalLocalId })
            .containsExactlyElementsIn(
                firestoreDocs.map { it.property.universalLocalId }
            )

        coVerify(exactly = 0) {
            propertyRepository.insertPropertyInsertFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedProperties_propertyAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val propertyId = propertyOnlineEntity1.universalLocalId
        val firestoreDoc = firestorePropertyDocument1

        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(firestoreDoc)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyId)
        } returns flowOf(propertyEntity1)

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val messages = result.map { (it as SyncStatus.Success).message }

        assertThat(messages).containsExactly("Property $propertyId already up-to-date")

        coVerify(exactly = 0) {
            propertyRepository.insertPropertyInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyId)
        }
    }

    @Test
    fun downloadUnSyncedProperties_allPropertiesAlreadyUpToDate_returnsSuccessWithoutSaving() = runTest {
        val firestoreDocs = listOf(
            firestorePropertyDocument1,
            firestorePropertyDocument2
        )

        coEvery { propertyOnlineRepository.getAllProperties() } returns firestoreDocs

        firestoreDocs.forEachIndexed { index, doc ->
            every {
                propertyRepository.getPropertyByIdIncludeDeleted(doc.property.universalLocalId)
            } returns flowOf(propertyEntityList[index])
        }

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(firestoreDocs.size)

        val messages = result.map { (it as SyncStatus.Success).message }

        val expectedMessages = firestoreDocs.map {
            "Property ${it.property.universalLocalId} already up-to-date"
        }

        assertThat(messages).containsExactlyElementsIn(expectedMessages)

        coVerify(exactly = 0) {
            propertyRepository.insertPropertyInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }

        firestoreDocs.forEach { doc ->
            coVerify(exactly = 1) {
                propertyRepository.getPropertyByIdIncludeDeleted(doc.property.universalLocalId)
            }
        }
    }

    @Test
    fun downloadUnSyncedProperties_mixedCases_returnsCorrectStatuses() = runTest {
        val propertyInsert = propertyOnlineEntity1
        val propertyUpdate = propertyOnlineEntity2.copy(updatedAt = 1700000006000)
        val propertySkip = propertyOnlineEntity3.copy(isDeleted = false)
        val propertyError = propertyOnlineEntity3.copy(
            universalLocalId = "error_id",
            updatedAt = 1700000008000
        )
        val propertyDelete = propertyOnlineEntity3

        val outdatedLocalProperty = propertyEntity2.copy(updatedAt = 1700000001000)
        val upToDateLocalProperty = propertyEntity3.copy(isDeleted = false)
        val localPropertyToDelete = propertyEntity3.copy(isDeleted = false)

        val firestoreDocs = listOf(
            firestorePropertyDocument1.copy(property = propertyInsert),
            firestorePropertyDocument2.copy(property = propertyUpdate),
            firestorePropertyDocument3.copy(property = propertySkip),
            firestorePropertyDocument3.copy(property = propertyError),
            firestorePropertyDocument3.copy(property = propertyDelete)
        )

        coEvery { propertyOnlineRepository.getAllProperties() } returns firestoreDocs
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyInsert.universalLocalId)
        } returns flowOf(null)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyUpdate.universalLocalId)
        } returns flowOf(outdatedLocalProperty)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertySkip.universalLocalId)
        } returns flowOf(upToDateLocalProperty)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyError.universalLocalId)
        } throws RuntimeException("DB fail")
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyDelete.universalLocalId)
        } returns flowOf(localPropertyToDelete)

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(5)

        val successes = result.filterIsInstance<SyncStatus.Success>()
        val failures = result.filterIsInstance<SyncStatus.Failure>()

        val successMessages = successes.map { it.message }

        assertThat(successMessages).containsExactly(
            "Property ${propertyInsert.universalLocalId} inserted",
            "Property ${propertyUpdate.universalLocalId} updated",
            "Property ${propertySkip.universalLocalId} already up-to-date",
            "Property ${propertyDelete.universalLocalId} deleted locally (remote deleted)"
        )

        assertThat(failures).hasSize(1)

        val failure = failures.first()
        assertThat(failure.label).isEqualTo("Property ${propertyError.universalLocalId}")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB fail")

        val insertedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = 1) {
            propertyRepository.insertPropertyInsertFromFirebase(
                capture(insertedProperties),
                any()
            )
        }

        assertThat(insertedProperties.first().universalLocalId)
            .isEqualTo(propertyInsert.universalLocalId)

        val updatedProperties = mutableListOf<PropertyOnlineEntity>()

        coVerify(exactly = 1) {
            propertyRepository.updatePropertyFromFirebase(
                capture(updatedProperties),
                any()
            )
        }

        assertThat(updatedProperties.first().universalLocalId)
            .isEqualTo(propertyUpdate.universalLocalId)

        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(propertySkip, any())
        }
        coVerify(exactly = 1) {
            propertyRepository.deleteProperty(localPropertyToDelete)
        }
    }

    @Test
    fun downloadUnSyncedProperty_individualFailure_returnsPartialSuccessWithFailure() =  runTest {
        val propertyId = propertyOnlineEntity1.universalLocalId
        val firestoreDoc = firestorePropertyDocument1

        coEvery { propertyOnlineRepository.getAllProperties() } returns listOf(firestoreDoc)
        every {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyId)
        } throws RuntimeException("DB crash")

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Property $propertyId")
        assertThat(failure.error).hasMessageThat().isEqualTo("DB crash")

        coVerify(exactly = 0) {
            propertyRepository.insertPropertyInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
        coVerify(exactly = 1) {
            propertyRepository.getPropertyByIdIncludeDeleted(propertyId)
        }
    }

    @Test
    fun downloadUnSyncedProperty_globalFailure_returnsFailureStatus() = runTest {
        coEvery { propertyOnlineRepository.getAllProperties() } throws RuntimeException("Firebase is down")

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).hasSize(1)

        val failures = result.filterIsInstance<SyncStatus.Failure>()

        assertThat(failures).hasSize(1)

        val failure = failures.first()

        assertThat(failure.label).isEqualTo("Global property download failed")
        assertThat(failure.error).hasMessageThat().isEqualTo("Firebase is down")

        coVerify(exactly = 1) {
            propertyOnlineRepository.getAllProperties()
        }
        coVerify(exactly = 0) {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            propertyRepository.insertPropertyInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
    }

    @Test
    fun downloadUnSyncedProperties_noPropertiesOnline_returnsEmptyList() = runTest {
        coEvery { propertyOnlineRepository.getAllProperties() } returns emptyList()

        val result = downloadManager.downloadUnSyncedProperties()

        assertThat(result).isEmpty()

        coVerify(exactly = 1) {
            propertyOnlineRepository.getAllProperties()
        }
        coVerify(exactly = 0) {
            propertyRepository.getPropertyByIdIncludeDeleted(any())
        }
        coVerify(exactly = 0) {
            propertyRepository.insertPropertyInsertFromFirebase(any(), any())
        }
        coVerify(exactly = 0) {
            propertyRepository.updatePropertyFromFirebase(any(), any())
        }
    }

}
