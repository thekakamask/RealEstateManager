package com.dcac.realestatemanager.ui.userPropertiesPage

import kotlinx.coroutines.flow.StateFlow


interface IUserPropertiesViewModel {

    val uiState: StateFlow<UserPropertiesUiState>
    fun loadUserProperties()
    fun resetState()
}