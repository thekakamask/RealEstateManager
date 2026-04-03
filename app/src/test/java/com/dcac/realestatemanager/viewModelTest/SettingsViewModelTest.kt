package com.dcac.realestatemanager.viewModelTest

import android.app.Application
import com.dcac.realestatemanager.data.preferences.IUserPreferencesRepository
import com.dcac.realestatemanager.ui.settingsPage.SettingsUiState
import com.dcac.realestatemanager.ui.settingsPage.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userPreferencesRepository = mockk<IUserPreferencesRepository>(relaxed = true)
    private val application = mockk<Application>(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel(
            userPreferencesRepository,
            application
        )
    }

    @Test
    fun loadSettings_shouldEmitSuccess() = runTest {
        every { userPreferencesRepository.selectedLanguage } returns flowOf("fr")
        every { userPreferencesRepository.selectedCurrency } returns flowOf("EUR")

        viewModel.loadSettings()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SettingsUiState.Success)

        state as SettingsUiState.Success
        assertEquals("fr", state.currentLanguage)
        assertEquals("EUR", state.currentCurrency)
    }

    @Test
    fun changeLanguage_whenSame_shouldDoNothing() = runTest {
        coEvery { userPreferencesRepository.getCurrentLanguage() } returns "fr"

        viewModel.changeLanguage("fr")
        advanceUntilIdle()

        coVerify(exactly = 0) { userPreferencesRepository.setLanguage(any()) }
        verify(exactly = 0) { application.startActivity(any()) }
    }

    @Test
    fun changeLanguage_whenDifferent_shouldUpdateLanguageAndRestartApp() = runTest {
        coEvery { userPreferencesRepository.getCurrentLanguage() } returns "en"

        viewModel.changeLanguage("fr")
        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferencesRepository.setLanguage("fr") }
        verify(exactly = 1) { application.startActivity(any()) }
    }

    @Test
    fun changeCurrency_shouldCallRepository() = runTest {
        viewModel.changeCurrency("EUR")
        advanceUntilIdle()

        coVerify { userPreferencesRepository.setCurrency("EUR") }
    }

    @Test
    fun resetState_shouldSetLoading() = runTest {
        viewModel.resetState()

        assertTrue(viewModel.uiState.value is SettingsUiState.Loading)
    }

}