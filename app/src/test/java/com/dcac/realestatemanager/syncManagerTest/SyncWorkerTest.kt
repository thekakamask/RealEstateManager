package com.dcac.realestatemanager.syncManagerTest

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.dcac.realestatemanager.data.AppContainer
import com.dcac.realestatemanager.data.sync.SyncWorker
import com.dcac.realestatemanager.data.sync.globalManager.DownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.globalManager.UploadInterfaceManager
import com.dcac.realestatemanager.network.NetworkMonitor
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.AppContainerProvider
import io.mockk.mockkConstructor
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import io.mockk.every
import io.mockk.unmockkAll
import org.junit.After
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.work.ListenableWorker.Result
import io.mockk.coEvery

class SyncWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var syncWorker: SyncWorker

    private val container = mockk<AppContainer>()
    private val uploadManager = mockk<UploadInterfaceManager>()
    private val downloadManager = mockk<DownloadInterfaceManager>()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)

        // 👇 mock creation who combine Context + AppContainerProvider
        val appContainerProvider = mockk<Context>(
            relaxed = true,
            moreInterfaces = arrayOf(AppContainerProvider::class)
        )
        every { (appContainerProvider as AppContainerProvider).container } returns container
        every { context.applicationContext } returns appContainerProvider

        every { container.uploadManager } returns uploadManager
        every { container.downloadManager } returns downloadManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun doWork_noInternet_returnsRetry() = runTest {
        mockkConstructor(NetworkMonitor::class)
        every { anyConstructed<NetworkMonitor>().isConnected() } returns false

        syncWorker = SyncWorker(context, workerParams)

        val result = syncWorker.doWork()
        assertEquals(Result.retry(), result)
    }

    @Test
    fun doWork_uploadAndDownloadSuccess_returnsSuccess() = runTest {
        mockkConstructor(NetworkMonitor::class)
        every { anyConstructed<NetworkMonitor>().isConnected() } returns true

        coEvery { uploadManager.syncAll() } returns listOf(SyncStatus.Success("Uploaded"))
        coEvery { downloadManager.downloadAll() } returns listOf(SyncStatus.Success("Downloaded"))

        syncWorker = SyncWorker(context, workerParams)

        val result = syncWorker.doWork()
        assertEquals(Result.success(), result)
    }

    @Test
    fun doWork_exceptionThrown_returnsRetry() = runTest {
        mockkConstructor(NetworkMonitor::class)
        every { anyConstructed<NetworkMonitor>().isConnected() } returns true

        coEvery { uploadManager.syncAll() } throws RuntimeException("fail")

        syncWorker = SyncWorker(context, workerParams)

        val result = syncWorker.doWork()
        assertEquals(Result.retry(), result)
    }
}