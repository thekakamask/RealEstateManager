package com.dcac.realestatemanager.ui.homePage

import kotlinx.coroutines.flow.StateFlow

interface IHomeViewModel {

    val uiState: StateFlow<HomeUiState>
    fun syncAll()
    fun loadUserInfo()
    fun observeUserProperties()
    fun resetSnackBarMessage()
    fun toggleDrawer(isOpen: Boolean)
    fun navigateTo(screen: HomeDestination)
    fun logout()
    fun resetState()
}