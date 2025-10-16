package com.dcac.realestatemanager.ui.homePage

interface IHomeViewModel {

    fun syncAll()
    fun loadUserInfo()
    fun resetSnackBarMessage()
    fun toggleDrawer(isOpen: Boolean)
    fun navigateTo(screen: HomeDestination)
    fun logout()
    fun resetState()
}