package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapConfig
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRemoteDataSource
import com.dcac.realestatemanager.fakeData.fakeApiService.FakeStaticMapApiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertArrayEquals

class StaticMapRemoteDataSourceTest {

    private lateinit var fakeApi: FakeStaticMapApiService
    private lateinit var remoteDataSource: StaticMapRemoteDataSource

    @Before
    fun setup() {
        fakeApi = FakeStaticMapApiService()
        remoteDataSource = StaticMapRemoteDataSource(fakeApi)
    }

    @Test
    fun getStaticMapImage_whenApiSuccess_shouldReturnByteArray() = runTest {

        fakeApi.shouldSucceed = true

        val config = StaticMapConfig(
            center = "48.8566,2.3522",
            zoom = 15,
            size = "600x300",
            mapType = "roadmap",
            markers = listOf("48.8566,2.3522"),
            styles = emptyList()
        )

        val result = remoteDataSource.getStaticMapImage(config)

        assertNotNull(result)

        val expected = fakeApi.returnedBody.toByteArray()

        assertArrayEquals(expected, result)
    }

    @Test
    fun getStaticMapImage_whenApiFails_shouldReturnNull() = runTest {

        fakeApi.shouldSucceed = false

        val config = StaticMapConfig(
            center = "48.8566,2.3522",
            zoom = 15,
            size = "600x300",
            mapType = "roadmap",
            markers = listOf("48.8566,2.3522"),
            styles = emptyList()
        )

        val result = remoteDataSource.getStaticMapImage(config)

        assertNull(result)
    }

}