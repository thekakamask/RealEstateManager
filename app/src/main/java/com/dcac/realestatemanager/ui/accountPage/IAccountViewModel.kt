package com.dcac.realestatemanager.ui.accountPage

interface IAccountViewModel {

    fun loadUser(userId: String)
    fun enterEditMode()
    fun updateUser(newName: String, newEmail: String)
    fun resetState()
}