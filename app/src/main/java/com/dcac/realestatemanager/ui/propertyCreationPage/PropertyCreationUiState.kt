package com.dcac.realestatemanager.ui.propertyCreationPage

import com.dcac.realestatemanager.model.Property

sealed class PropertyCreationUiState {
    data object Idle : PropertyCreationUiState()
    data object Loading : PropertyCreationUiState()

    data class StepState(
        val currentStep: PropertyCreationStep,
        val draft: PropertyDraft,
        val isNextEnabled: Boolean,
        val error: String? = null,
        val isLoadingMap: Boolean = false,
        val staticMapImageBytes: List<Byte>? = null
    ) : PropertyCreationUiState()

    data class Success(val property: Property, val isUpdate: Boolean) : PropertyCreationUiState()
    data class Error(val message: String) : PropertyCreationUiState()
}