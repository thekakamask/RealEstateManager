package com.dcac.realestatemanager.ui.accountPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.User

sealed class AccountUiState {

    data object Loading : AccountUiState()
    data object Idle : AccountUiState()

    @Immutable
    data class Viewing(
        val user: User
    ) : AccountUiState()

    @Immutable
    data class Editing(
        val user: User
    ) : AccountUiState()

    @Immutable
    data class Success(
        val updatedUser: User
    ) : AccountUiState()

    data class Error(val message: String) : AccountUiState()
}