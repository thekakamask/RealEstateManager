package com.dcac.realestatemanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRemoteDataSource
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class StaticMapRemoteDataSourceTest {

    private lateinit var remoteDataSource: StaticMapRemoteDataSource
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        remoteDataSource = StaticMapRemoteDataSource(FakeStaticMapApiService())
    }

    @Test
    fun saveStaticMapToLocal_shouldSaveFileAndReturnPath() {

        val fileName = "test_static_map.png"
        val bytes = "fake_image_bytes".toByteArray()

        val result = remoteDataSource.saveStaticMapToLocal(
            context,
            fileName,
            bytes
        )

        assertNotNull(result)

        val file = File(result!!.replace("file://", ""))

        assertTrue(file.exists())

        file.delete()
    }
}