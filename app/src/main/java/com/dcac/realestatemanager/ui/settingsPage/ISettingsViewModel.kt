package com.dcac.realestatemanager.ui.settingsPage

import kotlinx.coroutines.flow.StateFlow

interface ISettingsViewModel {

    val uiState: StateFlow<SettingsUiState>
    fun loadSettings()
    fun changeLanguage(languageCode: String)
    fun changeCurrency(currencyCode: String)
    fun resetState()
}