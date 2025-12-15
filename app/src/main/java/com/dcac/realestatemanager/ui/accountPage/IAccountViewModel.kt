package com.dcac.realestatemanager.ui.accountPage

import kotlinx.coroutines.flow.StateFlow

interface IAccountViewModel {

    val uiState: StateFlow<AccountUiState>
    fun checkAndLoadUser()
    fun getUserIdOrNull(): String?
    fun loadUser(userId: String)
    fun enterEditMode()
    fun updateUser(newName: String)
    fun setError(message: String)
    fun resetState()
}