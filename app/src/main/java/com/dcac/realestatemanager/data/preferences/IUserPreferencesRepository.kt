package com.dcac.realestatemanager.data.preferences

import kotlinx.coroutines.flow.Flow

interface IUserPreferencesRepository {

    val darkModeEnabled: Flow<Boolean>
    val selectedLanguage: Flow<String>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setLanguage(lang: String)

}