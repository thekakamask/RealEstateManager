package com.dcac.realestatemanager.ui.accountPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.User

interface AccountUiState {

    data object Loading : AccountUiState
    data object Idle : AccountUiState

    @Immutable
    data class Viewing(
        val user: User
    ) : AccountUiState
    // → When the user is just viewing their info.

    @Immutable
    data class Editing(
        val user: User
    ) : AccountUiState
    // → When the user is in edit mode (form with current values).

    @Immutable
    data class Success(
        val updatedUser: User
    ) : AccountUiState
    // → When update is successful (return to Viewing or show toast/snackbar).

    data class Error(val message: String) : AccountUiState
}