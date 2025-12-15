package com.dcac.realestatemanager.ui.settingsPage

import androidx.compose.runtime.Immutable


sealed class SettingsUiState {
    data object Loading : SettingsUiState()

    @Immutable
    data class Success(
        val currentLanguage: String,
        val currentCurrency: String,
        val appVersion: String
    ) : SettingsUiState()

    data class Error(val message: String) : SettingsUiState()
}