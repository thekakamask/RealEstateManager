package com.dcac.realestatemanager.ui.accountPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.User

sealed class AccountUiState {

    data object Loading : AccountUiState()
    data object Idle : AccountUiState()

    @Immutable
    data class Success(
        val user: User,
        val properties: List<Property> = emptyList(),
        val isEditing: Boolean = false
    ) : AccountUiState()

    data class Error(val message: String) : AccountUiState()
}