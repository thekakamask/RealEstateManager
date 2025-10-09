package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadManager
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyPoiCrossOnlineEntity
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

class CrossRefUploadManagerTest {

    private val crossRefRepository = mockk<PropertyPoiCrossRepository>(relaxed = true)
    private val crossRefOnlineRepository = mockk<PropertyPoiCrossOnlineRepository>(relaxed = true)

    private lateinit var uploadManager: PropertyPoiCrossUploadInterfaceManager

    private val crossRefEntity1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRefEntity2 = FakePropertyPoiCrossEntity.propertyPoiCross3
    private val crossRefEntity3 = FakePropertyPoiCrossEntity.propertyPoiCross5

    private val crossRefOnlineEntity1 = FakePropertyPoiCrossOnlineEntity.cross1
    private val crossRefOnlineEntity2 = FakePropertyPoiCrossOnlineEntity.cross2


    @Before
    fun setup(){
        MockKAnnotations.init(this, relaxUnitFun = true)
        uploadManager = PropertyPoiCrossUploadManager(crossRefRepository, crossRefOnlineRepository)
    }

    @After
    fun tearDown(){
        unmockkAll()
    }

    @Test
    fun uploadUnSyncedPoiS_poiNotDeleted_uploadsAndUpdatesRoom() = runTest {
        coEvery {
            crossRefRepository.uploadUnSyncedPropertiesPoiSCross()
        } returns flowOf(listOf(crossRefEntity1))

        coEvery {
            crossRefOnlineRepository.uploadCrossRef(any())
        } returns crossRefOnlineEntity1

        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)

        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("CrossRef (${crossRefEntity1.propertyId}, ${crossRefEntity1.poiId}) uploaded")

        coVerify {
            crossRefOnlineRepository.uploadCrossRef(match {
                it.propertyId == crossRefEntity1.propertyId &&
                        it.poiId == crossRefEntity1.poiId
            })
        }

        coVerify {
            crossRefRepository.downloadCrossRefFromFirebase(match {
                it.propertyId == crossRefEntity1.propertyId &&
                        it.poiId == crossRefEntity1.poiId
            })
        }
    }

    @Test
    fun uploadUnSyncedPoiS_poiMarkedDeleted_deletesFromFirebaseAndRoom() = runTest {
        val deletedCrossRef = crossRefEntity3
        coEvery { crossRefRepository.uploadUnSyncedPropertiesPoiSCross() } returns flowOf(listOf(deletedCrossRef))

        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)
        val success = result[0] as? SyncStatus.Success
        assertThat(success!!.userEmail).isEqualTo("CrossRef (${deletedCrossRef.propertyId}, ${deletedCrossRef.poiId}) deleted")

        coVerify {
            crossRefOnlineRepository.deleteCrossRef(deletedCrossRef.propertyId, deletedCrossRef.poiId)
            crossRefRepository.deleteCrossRef(deletedCrossRef)
        }
    }

    @Test
    fun uploadUnSyncedPoiS_globalFailure_returnsFailureStatus() = runTest {
        coEvery { crossRefRepository.uploadUnSyncedPropertiesPoiSCross() } throws RuntimeException("Room is down")

        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(1)
        val failure = result[0] as? SyncStatus.Failure
        assertThat(failure!!.label).isEqualTo("Global CrossRef upload failed")
        assertThat(failure.error.message).isEqualTo("Room is down")
    }

    @Test
    fun uploadUnSyncedPoiS_noPoiSToUpload_returnsEmptyList() = runTest {
        coEvery { crossRefRepository.uploadUnSyncedPropertiesPoiSCross() } returns flowOf(emptyList())

        // Act
        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        // Assert
        assertThat(result).isEmpty()

        coVerify(exactly = 0) {
            crossRefOnlineRepository.uploadCrossRef(any())
            crossRefRepository.downloadCrossRefFromFirebase(any())
            crossRefOnlineRepository.deleteCrossRef(crossRefOnlineEntity2.propertyId, crossRefOnlineEntity2.poiId)
            crossRefRepository.deleteCrossRef(any())
        }
    }

    @Test
    fun uploadUnSyncedPoiS_mixedCases_returnsCorrectStatuses() = runTest {
        val notSyncedNotDeleted = crossRefEntity1
        val alreadySyncedNotDeleted = crossRefEntity2
        val notSyncedDeleted = crossRefEntity3

        coEvery {
            crossRefRepository.uploadUnSyncedPropertiesPoiSCross()
        } returns flowOf(listOf(notSyncedNotDeleted, notSyncedDeleted))

        coEvery {
            crossRefOnlineRepository.uploadCrossRef(match {
                it.propertyId == notSyncedNotDeleted.propertyId &&
                        it.poiId == notSyncedNotDeleted.poiId
            })
        } returns crossRefOnlineEntity1

        val result = uploadManager.syncUnSyncedPropertyPoiCross()

        assertThat(result).hasSize(2)

        val uploaded = result.find {
            it is SyncStatus.Success &&
                    it.userEmail == "CrossRef (${notSyncedNotDeleted.propertyId}, ${notSyncedNotDeleted.poiId}) uploaded"
        }

        val deleted = result.find {
            it is SyncStatus.Success &&
                    it.userEmail == "CrossRef (${notSyncedDeleted.propertyId}, ${notSyncedDeleted.poiId}) deleted"
        }

        assertThat(uploaded).isNotNull()
        assertThat(deleted).isNotNull()

        coVerify { crossRefOnlineRepository.deleteCrossRef(notSyncedDeleted.propertyId, notSyncedDeleted.poiId) }
        coVerify { crossRefRepository.deleteCrossRef(notSyncedDeleted) }

        coVerify(exactly = 1) {
            crossRefOnlineRepository.uploadCrossRef(match {
                it.propertyId == notSyncedNotDeleted.propertyId &&
                        it.poiId == notSyncedNotDeleted.poiId
            })
        }

        coVerify {
            crossRefRepository.downloadCrossRefFromFirebase(match {
                it.propertyId == crossRefEntity1.propertyId &&
                        it.poiId == crossRefEntity1.poiId
            })
        }

        coVerify(exactly = 0) {
            crossRefOnlineRepository.uploadCrossRef(match {
                it.propertyId == alreadySyncedNotDeleted.propertyId &&
                        it.poiId == alreadySyncedNotDeleted.poiId
            })
        }

        coVerify(exactly = 0) {
            crossRefRepository.downloadCrossRefFromFirebase(crossRefOnlineEntity2)
        }
    }
}