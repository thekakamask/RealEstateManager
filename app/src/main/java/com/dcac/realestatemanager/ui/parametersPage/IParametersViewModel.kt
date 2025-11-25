package com.dcac.realestatemanager.ui.parametersPage

import kotlinx.coroutines.flow.StateFlow

interface IParametersViewModel {

    val uiState: StateFlow<ParametersUiState>
    fun loadSettings()
    fun toggleDarkMode()
    fun changeLanguage(languageCode: String)
    fun resetState()
}