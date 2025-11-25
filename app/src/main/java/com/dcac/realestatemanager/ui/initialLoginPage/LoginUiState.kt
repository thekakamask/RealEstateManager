package com.dcac.realestatemanager.ui.initialLoginPage

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.google.firebase.auth.FirebaseUser

sealed class LoginUiState {

    data object Idle : LoginUiState()

    data object Loading : LoginUiState()

    @Immutable
    data class Success(
        val firebaseUser: FirebaseUser
    ) : LoginUiState()

    @Immutable
    data class Error(
        @StringRes val messageResId: Int
    ) : LoginUiState()
}