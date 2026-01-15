package com.dcac.realestatemanager.ui.accountPage

import com.dcac.realestatemanager.model.Property
import kotlinx.coroutines.flow.StateFlow

interface IAccountViewModel {

    val uiState: StateFlow<AccountUiState>
    fun checkAndLoadUser()
    fun getUserIdOrNull(): String?
    fun loadUser(userId: String)
    fun enterEditMode()
    fun updateUser(newName: String)
    fun deleteProperty(property: Property)
    fun setError(message: String)
    fun resetState()
}