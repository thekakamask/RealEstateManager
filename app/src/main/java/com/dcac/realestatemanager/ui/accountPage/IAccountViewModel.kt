package com.dcac.realestatemanager.ui.accountPage

import kotlinx.coroutines.flow.StateFlow

interface IAccountViewModel {

    val uiState: StateFlow<AccountUiState>
    fun loadUser(userId: String)
    fun enterEditMode()
    fun updateUser(newName: String, newEmail: String)
    fun resetState()
}