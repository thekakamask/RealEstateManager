package com.dcac.realestatemanager.ui.settingsPage

import com.dcac.realestatemanager.data.preferences.IUserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

data class SettingsState(
    val language: String,
    val currency: String
)

@Singleton
class SettingsStateHolder @Inject constructor(
    repo: IUserPreferencesRepository
) {
    val settingsState: StateFlow<SettingsState> = combine(
        repo.selectedLanguage,
        repo.selectedCurrency
    ) { lang, curr ->
        SettingsState(lang, curr)
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = SettingsState("en", "USD")
    )
}