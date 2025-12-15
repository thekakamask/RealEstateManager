package com.dcac.realestatemanager.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Extension on context for usage everywhere
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : IUserPreferencesRepository {

    companion object {
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY = stringPreferencesKey("currency")
    }

    override val selectedLanguage: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[LANGUAGE] ?: "en" }

    override val selectedCurrency: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[CURRENCY] ?: "USD" }

    override suspend fun getCurrentLanguage(): String {
        return selectedLanguage.first()
    }

    override suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    override suspend fun setCurrency(currency: String) {
        context.dataStore.edit { it[CURRENCY] = currency }
    }
}
