package com.dcac.realestatemanager.ui.accountPage

interface IAccountViewModel {

    fun loadUser(userId: Long)
    fun enterEditMode()
    fun updateUser(newName: String, newEmail: String)
    fun resetState()
}