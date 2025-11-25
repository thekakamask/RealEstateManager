package com.dcac.realestatemanager.ui.parametersPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.preferences.IUserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ParametersViewModel @Inject constructor(
    private val userPreferencesRepository: IUserPreferencesRepository
) : ViewModel(), IParametersViewModel {

    private val _uiState = MutableStateFlow<ParametersUiState>(ParametersUiState.Loading)
    override val uiState: StateFlow<ParametersUiState> = _uiState.asStateFlow()

    private val version = "1.0.0" // Replace with BuildConfig.VERSION_NAME if needed

    override fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.darkModeEnabled,
                userPreferencesRepository.selectedLanguage
            ) { isDark, lang ->
                ParametersUiState.Success(
                    isDarkModeEnabled = isDark,
                    currentLanguage = lang,
                    appVersion = version
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    override fun toggleDarkMode() {
        viewModelScope.launch {
            val current = (_uiState.value as? ParametersUiState.Success)?.isDarkModeEnabled ?: false
            userPreferencesRepository.setDarkMode(!current)
        }
    }

    override fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(languageCode)
        }
    }

    override fun resetState() {
        _uiState.value = ParametersUiState.Loading
    }
}
