package com.dcac.realestatemanager.ui.settingsPage

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.BuildConfig
import com.dcac.realestatemanager.MainActivity
import com.dcac.realestatemanager.data.preferences.IUserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val application: Application
) : ViewModel(), ISettingsViewModel {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    override val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val version = BuildConfig.VERSION_NAME

    override fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.selectedLanguage,
                userPreferencesRepository.selectedCurrency
            ) { lang, currency ->
                SettingsUiState.Success(
                    currentLanguage = lang,
                    currentCurrency = currency,
                    appVersion = version
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    override fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            if (userPreferencesRepository.getCurrentLanguage() != languageCode) {
                userPreferencesRepository.setLanguage(languageCode)

                val intent = Intent(application, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                application.startActivity(intent)
            }
        }
    }

    override fun changeCurrency(currencyCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setCurrency(currencyCode)
        }
    }

    override fun resetState() {
        _uiState.value = SettingsUiState.Loading
    }
}
