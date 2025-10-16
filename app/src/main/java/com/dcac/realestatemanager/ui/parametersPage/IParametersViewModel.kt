package com.dcac.realestatemanager.ui.parametersPage

interface IParametersViewModel {
    fun loadSettings()
    fun toggleDarkMode()
    fun changeLanguage(languageCode: String)
    fun resetState()
}