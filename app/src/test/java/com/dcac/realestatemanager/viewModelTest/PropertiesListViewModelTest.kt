package com.dcac.realestatemanager.viewModelTest

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder
import com.dcac.realestatemanager.ui.homePage.propertiesListScreen.PropertiesListUiState
import com.dcac.realestatemanager.ui.homePage.propertiesListScreen.PropertiesListViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PropertiesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val user1 = FakeUserModel.user1
    private val property1 = FakePropertyModel.property1

    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private lateinit var viewModel: PropertiesListViewModel

    @Before
    fun setup() {
        viewModel = PropertiesListViewModel(
            propertyRepository,
            userRepository)
    }

    @Test
    fun applyFilters_emptyAlphabetic_returnsSuccess() = runTest {
        val filters = PropertyFilters(
            sortOrder = PropertySortOrder.ALPHABETIC
        )

        val properties = listOf(property1)

        every { propertyRepository.getAllPropertiesByAlphabetic() } returns flowOf(properties)
        coEvery {
            userRepository.getUserById(property1.universalLocalUserId)
        } returns flowOf(user1)

        viewModel.applyFilters(filters)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is PropertiesListUiState.Success)

        state as PropertiesListUiState.Success

        assertEquals(1, state.properties.size)
        assertEquals(PropertySortOrder.ALPHABETIC, state.sortOrder)
        assertEquals(false, state.isFiltered)
    }

    @Test
    fun applyFilters_emptyDate_returnsSuccess() = runTest {
        val filters = PropertyFilters(
            sortOrder = PropertySortOrder.DATE
        )

        val properties = listOf(property1)

        every { propertyRepository.getAllPropertiesByDate() } returns flowOf(properties)
        coEvery {
            userRepository.getUserById(property1.universalLocalUserId)
        } returns flowOf(user1)

        viewModel.applyFilters(filters)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is PropertiesListUiState.Success)

        state as PropertiesListUiState.Success

        assertEquals(PropertySortOrder.DATE, state.sortOrder)
        assertEquals(false, state.isFiltered)
    }

    @Test
    fun applyFilters_withFilters_returnsFilteredSuccess() = runTest {
        val filters = PropertyFilters(
            minPrice = 100000,
            maxPrice = 500000
        )

        val properties = listOf(property1)

        coEvery {
            propertyRepository.searchProperties(
                any(), any(), any(), any(),
                any(), any(), any()
            )
        } returns flowOf(properties)

        coEvery {
            userRepository.getUserById(property1.universalLocalUserId)
        } returns flowOf(user1)

        viewModel.applyFilters(filters)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is PropertiesListUiState.Success)

        state as PropertiesListUiState.Success

        assertTrue(state.isFiltered)
        assertEquals(filters, state.activeFilters)
    }

    @Test
    fun applyFilters_repositoryFails_returnsError() = runTest {
        val filters = PropertyFilters()

        every { propertyRepository.getAllPropertiesByAlphabetic() } returns
                flow {
                    throw RuntimeException("DB error")
                }

        viewModel.applyFilters(filters)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is PropertiesListUiState.Error)
        assertTrue(
            (state as PropertiesListUiState.Error)
                .message.contains("Search failed")
        )
    }

    @Test
    fun resetFilters_returnsSuccess() = runTest {
        every { propertyRepository.getAllPropertiesByAlphabetic() } returns
                flowOf(listOf(property1))

        coEvery {
            userRepository.getUserById(property1.universalLocalUserId)
        } returns flowOf(user1)

        viewModel.resetFilters()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PropertiesListUiState.Success)
    }

    @Test
    fun resetState_setsIdle() {
        viewModel.resetState()

        assertTrue(viewModel.uiState.value is PropertiesListUiState.Idle)
    }

}