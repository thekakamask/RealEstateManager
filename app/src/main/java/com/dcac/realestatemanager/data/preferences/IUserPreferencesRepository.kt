package com.dcac.realestatemanager.data.preferences

import kotlinx.coroutines.flow.Flow

interface IUserPreferencesRepository {

    val selectedLanguage: Flow<String>
    val selectedCurrency: Flow<String>
    suspend fun getCurrentLanguage(): String
    suspend fun setLanguage(lang: String)
    suspend fun setCurrency(currency: String)
}