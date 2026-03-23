package com.dcac.realestatemanager.syncManagerTest.uploadManagerTest

import android.util.Log
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.globalManager.UploadManager
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserUploadInterfaceManager
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class GlobalUploadManagerTest {

    private lateinit var uploadManager: UploadManager

    private val userManager = mockk<UserUploadInterfaceManager>()
    private val photoManager = mockk<PhotoUploadInterfaceManager>()
    private val poiManager = mockk<PoiUploadInterfaceManager>()
    private val crossManager = mockk<PropertyPoiCrossUploadInterfaceManager>()
    private val propertyManager = mockk<PropertyUploadInterfaceManager>()
    private val staticMapManager = mockk<StaticMapUploadInterfaceManager>()

    @Before
    fun setUp() {

        mockkStatic(Log::class)

        every { Log.e(any(), any()) } returns 0


        uploadManager = UploadManager(
            userUploadManager = userManager,
            photoUploadManager = photoManager,
            poiUploadManager = poiManager,
            crossSyncManager = crossManager,
            propertyUploadManager = propertyManager,
            staticMapUploadManager = staticMapManager
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun syncAll_allEntitiesSynced_successStatusesReturned() = runTest {
        val expectedStatuses = listOf(
            SyncStatus.Success("User 1 uploaded"),
            SyncStatus.Success("Photo 1 uploaded"),
            SyncStatus.Success("Poi 1 uploaded"),
            SyncStatus.Success("CrossRef (1,1) uploaded"),
            SyncStatus.Success("Property 1 uploaded"),
            SyncStatus.Success("StaticMap 1 uploaded")
        )

        coEvery { userManager.syncUnSyncedUsers() } returns listOf(expectedStatuses[0])
        coEvery { photoManager.syncUnSyncedPhotos() } returns listOf(expectedStatuses[1])
        coEvery { poiManager.syncUnSyncedPoiS() } returns listOf(expectedStatuses[2])
        coEvery { crossManager.syncUnSyncedPropertyPoiCross() } returns listOf(expectedStatuses[3])
        coEvery { propertyManager.syncUnSyncedProperties() } returns listOf(expectedStatuses[4])
        coEvery { staticMapManager.syncUnSyncedStaticMaps() } returns listOf(expectedStatuses[5])

        val result = uploadManager.syncAll()

        assertThat(result).containsExactlyElementsIn(expectedStatuses)
    }

    @Test
    fun syncAll_someManagersFail_returnsMixedResults() = runTest {
        val success = SyncStatus.Success("User uploaded")
        val failure = SyncStatus.Failure("Photo failed", Exception("upload error"))

        coEvery { userManager.syncUnSyncedUsers() } returns listOf(success)
        coEvery { photoManager.syncUnSyncedPhotos() } returns listOf(failure)
        coEvery { poiManager.syncUnSyncedPoiS() } returns emptyList()
        coEvery { crossManager.syncUnSyncedPropertyPoiCross() } returns emptyList()
        coEvery { propertyManager.syncUnSyncedProperties() } returns emptyList()
        coEvery { staticMapManager.syncUnSyncedStaticMaps() } returns emptyList()

        val result = uploadManager.syncAll()

        assertThat(result).containsExactly(success, failure)
    }

    @Test
    fun syncAll_allManagersFail_returnsOnlyFailures() = runTest {
        val failure1 = SyncStatus.Failure("User sync failed", Exception("err1"))
        val failure2 = SyncStatus.Failure("Photo sync failed", Exception("err2"))
        val failure3 = SyncStatus.Failure("POI sync failed", Exception("err3"))
        val failure4 = SyncStatus.Failure("CrossRef sync failed", Exception("err4"))
        val failure5 = SyncStatus.Failure("Property sync failed", Exception("err5"))
        val failure6 = SyncStatus.Failure("StaticMap sync failed", Exception("err6"))

        coEvery { userManager.syncUnSyncedUsers() } returns listOf(failure1)
        coEvery { photoManager.syncUnSyncedPhotos() } returns listOf(failure2)
        coEvery { poiManager.syncUnSyncedPoiS() } returns listOf(failure3)
        coEvery { crossManager.syncUnSyncedPropertyPoiCross() } returns listOf(failure4)
        coEvery { propertyManager.syncUnSyncedProperties() } returns listOf(failure5)
        coEvery { staticMapManager.syncUnSyncedStaticMaps() } returns listOf(failure6)

        val result = uploadManager.syncAll()

        assertThat(result).containsExactly(failure1, failure2, failure3, failure4, failure5, failure6)
    }

    @Test
    fun syncAll_someManagersReturnEmptyList() = runTest {
        val success = SyncStatus.Success("Photo uploaded")

        coEvery { userManager.syncUnSyncedUsers() } returns emptyList()
        coEvery { photoManager.syncUnSyncedPhotos() } returns listOf(success)
        coEvery { poiManager.syncUnSyncedPoiS() } returns emptyList()
        coEvery { crossManager.syncUnSyncedPropertyPoiCross() } returns emptyList()
        coEvery { propertyManager.syncUnSyncedProperties() } returns emptyList()
        coEvery { staticMapManager.syncUnSyncedStaticMaps() } returns emptyList()

        val result = uploadManager.syncAll()

        assertThat(result).containsExactly(success)
    }

}
