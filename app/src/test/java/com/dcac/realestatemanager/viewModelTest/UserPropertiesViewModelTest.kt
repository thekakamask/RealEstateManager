package com.dcac.realestatemanager.viewModelTest

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder
import com.dcac.realestatemanager.ui.userPropertiesPage.UserPropertiesUiState
import com.dcac.realestatemanager.ui.userPropertiesPage.UserPropertiesViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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
class UserPropertiesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)

    private val user1 = FakeUserModel.user1
    private val property1 = FakePropertyModel.property1
    private val property2 = FakePropertyModel.property2
    private val firebaseUser = mockk<FirebaseUser>()

    private lateinit var viewModel: UserPropertiesViewModel

    @Before
    fun setup() {
        viewModel = UserPropertiesViewModel(
            userRepository,
            propertyRepository,
            authRepository
        )
    }

    @Test
    fun getUserIdOrNull_shouldReturnLocalUserId() = runTest {
        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)

        val result = viewModel.getUserIdOrNull()

        assertEquals(user1.universalLocalId, result)
    }

    @Test
    fun getUserIdOrNull_withoutCurrentUser_shouldReturnNull() = runTest {
        every { authRepository.currentUser } returns null

        val result = viewModel.getUserIdOrNull()

        assertNull(result)
    }

    @Test
    fun resetState_shouldSetIdle() = runTest {
        viewModel.resetState()

        assertTrue(viewModel.uiState.value is UserPropertiesUiState.Idle)
    }

    @Test
    fun applyFilters_withEmptyFiltersAndAlphabetic_shouldEmitSuccess() = runTest {
        val filters = PropertyFilters(sortOrder = PropertySortOrder.ALPHABETIC)

        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)
        coEvery {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        } returns flowOf(listOf(property1, property2))

        viewModel.applyFilters(filters)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        }

        val state = viewModel.uiState.value
        assertTrue(state is UserPropertiesUiState.Success)

        state as UserPropertiesUiState.Success
        assertEquals(2, state.properties.size)
        assertFalse(state.isFiltered)
        assertEquals(PropertySortOrder.ALPHABETIC, state.sortOrder)
        assertNull(state.activeFilters)
    }

    @Test
    fun applyFilters_withEmptyFiltersAndDate_shouldUseDateRepo() = runTest {
        val filters = PropertyFilters(sortOrder = PropertySortOrder.DATE)

        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)
        coEvery {
            propertyRepository.getFullPropertiesByUserIdDate(user1.universalLocalId)
        } returns flowOf(listOf(property1))

        viewModel.applyFilters(filters)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            propertyRepository.getFullPropertiesByUserIdDate(user1.universalLocalId)
        }

        val state = viewModel.uiState.value as UserPropertiesUiState.Success
        assertEquals(1, state.properties.size)
        assertFalse(state.isFiltered)
        assertEquals(PropertySortOrder.DATE, state.sortOrder)
    }

    @Test
    fun applyFilters_withNonEmptyFilters_shouldUseSearch() = runTest {
        val filters = PropertyFilters(
            selectedType = "House",
            sortOrder = PropertySortOrder.ALPHABETIC
        )

        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)
        coEvery {
            propertyRepository.searchUserProperties(user1.universalLocalId, filters)
        } returns flowOf(listOf(property1))

        viewModel.applyFilters(filters)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            propertyRepository.searchUserProperties(user1.universalLocalId, filters)
        }

        val state = viewModel.uiState.value as UserPropertiesUiState.Success
        assertTrue(state.isFiltered)
        assertEquals(filters, state.activeFilters)
        assertEquals(filters, state.filters)
    }

    @Test
    fun applyFilters_withoutUser_shouldDoNothing() = runTest {
        every { authRepository.currentUser } returns null

        viewModel.applyFilters(PropertyFilters())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UserPropertiesUiState.Idle)
    }

    @Test
    fun applyFilters_whenRepositoryThrows_shouldEmitError() = runTest {
        val filters = PropertyFilters(sortOrder = PropertySortOrder.ALPHABETIC)

        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)
        coEvery {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException("boom")
        }

        viewModel.applyFilters(filters)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UserPropertiesUiState.Error)
        assertTrue((state as UserPropertiesUiState.Error).message.contains("boom"))
    }

    @Test
    fun toggleFilterSheet_shouldUpdateStateWhenSuccess() = runTest {
        val filters = PropertyFilters(sortOrder = PropertySortOrder.ALPHABETIC)

        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)
        coEvery {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        } returns flowOf(listOf(property1))

        viewModel.applyFilters(filters)
        advanceUntilIdle()

        viewModel.toggleFilterSheet(true)

        val state = viewModel.uiState.value as UserPropertiesUiState.Success
        assertTrue(state.showFilterSheet)
    }

    @Test
    fun toggleFilterSheet_whenNotSuccess_shouldDoNothing() = runTest {
        viewModel.toggleFilterSheet(true)

        assertTrue(viewModel.uiState.value is UserPropertiesUiState.Idle)
    }

    @Test
    fun resetFilters_shouldReloadDefaultFilters() = runTest {
        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)
        coEvery {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        } returns flowOf(listOf(property1, property2))

        viewModel.resetFilters()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        }

        val state = viewModel.uiState.value as UserPropertiesUiState.Success
        assertFalse(state.isFiltered)
        assertNull(state.activeFilters)
        assertEquals(PropertySortOrder.ALPHABETIC, state.sortOrder)
    }


}