package com.dcac.realestatemanager.ui.parametersPage

import androidx.compose.runtime.Immutable


sealed class ParametersUiState {
    data object Loading : ParametersUiState()

    @Immutable
    data class Success(
        val isDarkModeEnabled: Boolean,
        val currentLanguage: String,
        val appVersion: String
    ) : ParametersUiState()

    data class Error(val message: String) : ParametersUiState()
}