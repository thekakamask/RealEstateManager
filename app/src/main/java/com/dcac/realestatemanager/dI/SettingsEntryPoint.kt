package com.dcac.realestatemanager.dI

import com.dcac.realestatemanager.ui.settingsPage.SettingsStateHolder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsEntryPoint {
    fun settingsStateHolder(): SettingsStateHolder
}