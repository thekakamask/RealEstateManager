package com.dcac.realestatemanager.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Extension on context for usage everywhere
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : IUserPreferencesRepository {

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override val darkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_MODE] ?: false }

    override val selectedLanguage: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[LANGUAGE] ?: "en" }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    override suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }
}
