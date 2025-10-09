package com.dcac.realestatemanager.syncManagerTest.downloadManagerTest

import android.util.Log
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.globalManager.DownloadManager
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadInterfaceManager
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

class GlobalDownloadManagerTest {

    private lateinit var downloadManager: DownloadManager

    private val userManager = mockk<UserDownloadInterfaceManager>()
    private val photoManager = mockk<PhotoDownloadInterfaceManager>()
    private val poiManager = mockk<PoiDownloadInterfaceManager>()
    private val crossManager = mockk<PropertyPoiCrossDownloadInterfaceManager>()
    private val propertyManager = mockk<PropertyDownloadInterfaceManager>()

    @Before
    fun setUp() {

        mockkStatic(Log::class)

        every { Log.e(any(), any()) } returns 0

        downloadManager = DownloadManager(
            userDownloadManager = userManager,
            photoDownloadManager = photoManager,
            poiDownloadManager = poiManager,
            propertyPoiCrossDownloadManager = crossManager,
            propertyDownloadManager = propertyManager
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun downloadAll_allEntitiesSynced_successStatusesReturned() = runTest {
        val expectedStatuses = listOf(
            SyncStatus.Success("User 1 downloaded"),
            SyncStatus.Success("Photo 1 downloaded"),
            SyncStatus.Success("Poi 1 downloaded"),
            SyncStatus.Success("CrossRef (1,1) downloaded"),
            SyncStatus.Success("Property 1 downloaded")
        )

        coEvery { userManager.downloadUnSyncedUsers() } returns listOf(expectedStatuses[0])
        coEvery { photoManager.downloadUnSyncedPhotos() } returns listOf(expectedStatuses[1])
        coEvery { poiManager.downloadUnSyncedPoiS() } returns listOf(expectedStatuses[2])
        coEvery { crossManager.downloadUnSyncedPropertyPoiCross() } returns listOf(expectedStatuses[3])
        coEvery { propertyManager.downloadUnSyncedProperties() } returns listOf(expectedStatuses[4])

        val result = downloadManager.downloadAll()

        assertThat(result).containsExactlyElementsIn(expectedStatuses)
    }

    @Test
    fun downloadAll_someManagersFail_returnsMixedResults() = runTest {
        val success = SyncStatus.Success("User downloaded")
        val failure = SyncStatus.Failure("Photo download failed", Exception("download error"))

        coEvery { userManager.downloadUnSyncedUsers() } returns listOf(success)
        coEvery { photoManager.downloadUnSyncedPhotos() } returns listOf(failure)
        coEvery { poiManager.downloadUnSyncedPoiS() } returns emptyList()
        coEvery { crossManager.downloadUnSyncedPropertyPoiCross() } returns emptyList()
        coEvery { propertyManager.downloadUnSyncedProperties() } returns emptyList()

        val result = downloadManager.downloadAll()

        assertThat(result).containsExactly(success, failure)
    }

    @Test
    fun downloadAll_allManagersFail_returnsOnlyFailures() = runTest {
        val failure1 = SyncStatus.Failure("User download failed", Exception("err1"))
        val failure2 = SyncStatus.Failure("Photo download failed", Exception("err2"))
        val failure3 = SyncStatus.Failure("POI download failed", Exception("err3"))
        val failure4 = SyncStatus.Failure("CrossRef download failed", Exception("err4"))
        val failure5 = SyncStatus.Failure("Property download failed", Exception("err5"))

        coEvery { userManager.downloadUnSyncedUsers() } returns listOf(failure1)
        coEvery { photoManager.downloadUnSyncedPhotos() } returns listOf(failure2)
        coEvery { poiManager.downloadUnSyncedPoiS() } returns listOf(failure3)
        coEvery { crossManager.downloadUnSyncedPropertyPoiCross() } returns listOf(failure4)
        coEvery { propertyManager.downloadUnSyncedProperties() } returns listOf(failure5)

        val result = downloadManager.downloadAll()

        assertThat(result).containsExactly(failure1, failure2, failure3, failure4, failure5)
    }

    @Test
    fun downloadAll_someManagersReturnEmptyList() = runTest {
        val success = SyncStatus.Success("POI downloaded")

        coEvery { userManager.downloadUnSyncedUsers() } returns emptyList()
        coEvery { photoManager.downloadUnSyncedPhotos() } returns emptyList()
        coEvery { poiManager.downloadUnSyncedPoiS() } returns listOf(success)
        coEvery { crossManager.downloadUnSyncedPropertyPoiCross() } returns emptyList()
        coEvery { propertyManager.downloadUnSyncedProperties() } returns emptyList()

        val result = downloadManager.downloadAll()

        assertThat(result).containsExactly(success)
    }
}