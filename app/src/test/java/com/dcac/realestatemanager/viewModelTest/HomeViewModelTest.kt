package com.dcac.realestatemanager.viewModelTest

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.globalManager.DownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.globalManager.UploadInterfaceManager
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.homePage.HomeDestination
import com.dcac.realestatemanager.ui.homePage.HomeUiState
import com.dcac.realestatemanager.ui.homePage.HomeViewModel
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val user1 = FakeUserModel.user1
    private val property1 = FakePropertyModel.property1

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val uploadManager = mockk<UploadInterfaceManager>(relaxed = true)
    private val downloadManager = mockk<DownloadInterfaceManager>(relaxed = true)
    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    private fun createViewModel() {
        viewModel = HomeViewModel(
            authRepository,
            uploadManager,
            downloadManager,
            propertyRepository,
            userRepository
        )
    }

    private fun loadSuccessState() = runTest {
        every { authRepository.currentUser } returns mockk {
            every { email } returns user1.email
            every { displayName } returns user1.agentName
            every { uid } returns user1.firebaseUid
        }

        coEvery {
            userRepository.getUserByFirebaseUid(user1.firebaseUid)
        } returns flowOf(user1)

        every {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        } returns flowOf(listOf(property1))

        createViewModel()

        advanceUntilIdle()
    }

    @Test
    fun init_userLoggedIn_setsSuccess() = runTest {
        every { authRepository.currentUser } returns mockk {
            every { email } returns user1.email
            every { displayName } returns user1.agentName
            every { uid } returns user1.firebaseUid
        }

        coEvery {
            userRepository.getUserByFirebaseUid(user1.firebaseUid)
        } returns flowOf(user1)

        every {
            propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId)
        } returns flowOf(listOf(property1))

        createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is HomeUiState.Success)

        state as HomeUiState.Success

        assertEquals(user1.email, state.userEmail)
        assertEquals(user1.agentName, state.userName)
        assertEquals(1, state.totalProperties)
    }

    @Test
    fun loadUserInfo_noUser_returnsError() {
        every { authRepository.currentUser } returns null

        createViewModel()

        val state = viewModel.uiState.value

        assertTrue(state is HomeUiState.Error)
    }

    @Test
    fun toggleDrawer_updatesState() = runTest {
        loadSuccessState()

        viewModel.toggleDrawer(true)

        val state = viewModel.uiState.value as HomeUiState.Success

        assertTrue(state.isDrawerOpen)
    }

    @Test
    fun toggleFilterSheet_updatesState() = runTest {
        loadSuccessState()

        viewModel.toggleFilterSheet(true)

        val state = viewModel.uiState.value as HomeUiState.Success

        assertTrue(state.showFilterSheet)
    }

    @Test
    fun applyFilters_updatesFilters() = runTest {
        loadSuccessState()

        val filters = PropertyFilters(minPrice = 100000)

        viewModel.applyFilters(filters)

        val state = viewModel.uiState.value as HomeUiState.Success

        assertEquals(filters, state.filters)
    }

    @Test
    fun resetFilters_resetsToDefault() = runTest {
        loadSuccessState()

        viewModel.applyFilters(PropertyFilters(minPrice = 100000))
        viewModel.resetFilters()

        val state = viewModel.uiState.value as HomeUiState.Success

        assertEquals(PropertyFilters(), state.filters)
    }

    @Test
    fun navigateTo_updatesCurrentScreen() = runTest {
        loadSuccessState()

        viewModel.navigateTo(HomeDestination.GoogleMap)

        val state = viewModel.uiState.value as HomeUiState.Success

        assertEquals(HomeDestination.GoogleMap, state.currentScreen)
    }

    @Test
    fun logout_setsIdle() = runTest {
        loadSuccessState()

        coEvery { authRepository.signOut() } returns Unit

        viewModel.logout()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Idle)
    }

    @Test
    fun resetState_setsIdle() {
        createViewModel()
        viewModel.resetState()

        assertTrue(viewModel.uiState.value is HomeUiState.Idle)
    }

    @Test
    fun resetSnackBarMessage_clearsMessage() = runTest {
        loadSuccessState()

        coEvery { uploadManager.syncAll() } returns emptyList()
        coEvery { downloadManager.downloadAll() } returns emptyList()

        viewModel.syncAll()
        advanceUntilIdle()

        viewModel.resetSnackBarMessage()

        val state = viewModel.uiState.value as HomeUiState.Success

        assertNull(state.snackBarMessage)
    }

    @Test
    fun syncAll_success_updatesSnackBar() = runTest {
        loadSuccessState()

        coEvery { uploadManager.syncAll() } returns emptyList()
        coEvery { downloadManager.downloadAll() } returns emptyList()

        viewModel.syncAll()

        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeUiState.Success

        assertTrue(!state.isSyncing)
        assertTrue(state.snackBarMessage?.contains("Sync completed") == true)
    }

    @Test
    fun syncAll_failure_updatesSnackBarWithError() = runTest {
        loadSuccessState()

        coEvery { uploadManager.syncAll() } throws RuntimeException("Upload failed")

        viewModel.syncAll()

        advanceUntilIdle()

        val state = viewModel.uiState.value as HomeUiState.Success

        assertTrue(!state.isSyncing)
        assertTrue(state.snackBarMessage?.contains("Sync failed") == true)
    }



}