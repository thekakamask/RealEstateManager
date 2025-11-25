package com.dcac.realestatemanager.ui.userPropertiesPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed class UserPropertiesUiState {
    data object Idle : UserPropertiesUiState()
    data object Loading : UserPropertiesUiState()

    @Immutable
    data class Success(
        val properties: List<Property>
    ) : UserPropertiesUiState()

    @Immutable
    data class Error(val message: String) : UserPropertiesUiState()
}