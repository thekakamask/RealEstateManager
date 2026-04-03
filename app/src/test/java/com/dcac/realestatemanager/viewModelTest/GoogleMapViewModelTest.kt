package com.dcac.realestatemanager.viewModelTest

import android.location.Location
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakePoiModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.homePage.googleMapScreen.GoogleMapUiState
import com.dcac.realestatemanager.ui.homePage.googleMapScreen.GoogleMapViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoogleMapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val property1 = FakePropertyModel.property1
    private val poi1 = FakePoiModel.poi1

    private val mapRepository = mockk<GoogleMapRepository>(relaxed = true)
    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)

    private lateinit var viewModel: GoogleMapViewModel

    @Before
    fun setup() {
        viewModel = GoogleMapViewModel(
            mapRepository,
            propertyRepository
        )
    }

    private fun mockUserLocation(): Location {
        val location = mockk<Location>()
        every { location.latitude } returns 48.8566
        every { location.longitude } returns 2.3522
        return location
    }


    @Test
    fun loadMapData_success_updatesSuccessState() = runTest {
        val userLocation = mockUserLocation()

        val properties = listOf(FakePropertyModel.property1)
        val poiS = listOf(FakePoiModel.poi1)

        coEvery { mapRepository.getUserLocation() } returns userLocation
        every { mapRepository.getAllProperties() } returns flowOf(properties)
        every { mapRepository.getAllPoiS() } returns flowOf(poiS)

        viewModel.loadMapData()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is GoogleMapUiState.Success)

        state as GoogleMapUiState.Success

        assertEquals(2.3522, state.userLocation?.longitude)
        assertEquals(48.8566, state.userLocation?.latitude)
        assertEquals(1, state.properties.size)
        assertEquals(1, state.poiS.size)
    }

    @Test
    fun loadMapData_repositoryFails_returnsError() = runTest {
        coEvery { mapRepository.getUserLocation() } throws RuntimeException("GPS error")

        viewModel.loadMapData()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is GoogleMapUiState.Error)
        assertTrue((state as GoogleMapUiState.Error).message.contains("Map load failed"))
    }

    @Test
    fun applyFilters_success_returnsFilteredSuccess() = runTest {
        val filters = PropertyFilters(
            minPrice = 100000,
            maxPrice = 500000
        )

        val userLocation = mockUserLocation()
        val properties = listOf(property1)
        val poiS = listOf(poi1)

        coEvery { mapRepository.getUserLocation() } returns userLocation
        coEvery {
            propertyRepository.searchProperties(
                any(), any(), any(), any(),
                any(), any(), any()
            )
        } returns flowOf(properties)

        every { mapRepository.getAllPoiS() } returns flowOf(poiS)

        viewModel.applyFilters(filters)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is GoogleMapUiState.Success)

        state as GoogleMapUiState.Success

        assertTrue(state.isFiltered)
        assertEquals(filters, state.activeFilters)
    }

    @Test
    fun applyFilters_repositoryFails_returnsError() = runTest {
        val filters = PropertyFilters()

        coEvery { mapRepository.getUserLocation() } returns mockUserLocation()
        coEvery {
            propertyRepository.searchProperties(
                any(), any(), any(), any(),
                any(), any(), any()
            )
        } throws RuntimeException("DB error")

        viewModel.applyFilters(filters)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is GoogleMapUiState.Error)
        assertTrue(
            (state as GoogleMapUiState.Error)
                .message.contains("Filtered map load failed")
        )
    }

    @Test
    fun resetFilters_reloadMapData() = runTest {
        val userLocation = mockUserLocation()

        coEvery { mapRepository.getUserLocation() } returns userLocation
        every { mapRepository.getAllProperties() } returns flowOf(listOf(property1))
        every { mapRepository.getAllPoiS() } returns flowOf(listOf(poi1))

        viewModel.resetFilters()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is GoogleMapUiState.Success)
    }

    @Test
    fun selectProperty_updatesSelectedProperty() = runTest {
        loadSuccessState()

        viewModel.selectProperty("property_1")

        val state = viewModel.uiState.value as GoogleMapUiState.Success

        assertEquals("property_1", state.selectedPropertyId)
    }

    @Test
    fun clearSelectedProperty_resetsSelection() = runTest {
        loadSuccessState()

        viewModel.selectProperty("property_1")
        viewModel.clearSelectedProperty()

        val state = viewModel.uiState.value as GoogleMapUiState.Success

        assertNull(state.selectedPropertyId)
    }

    @Test
    fun resetState_setsIdle() {
        viewModel.resetState()

        assertTrue(viewModel.uiState.value is GoogleMapUiState.Idle)
    }

    private fun loadSuccessState() = runTest {
        val userLocation = mockUserLocation()

        coEvery { mapRepository.getUserLocation() } returns userLocation
        every { mapRepository.getAllProperties() } returns flowOf(listOf(property1))
        every { mapRepository.getAllPoiS() } returns flowOf(listOf(FakePoiModel.poi1))

        viewModel.loadMapData()
        advanceUntilIdle()
    }

}