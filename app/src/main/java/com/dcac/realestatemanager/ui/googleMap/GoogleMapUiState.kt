package com.dcac.realestatemanager.ui.googleMap

import android.location.Location
import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property

sealed interface GoogleMapUiState {

    @Immutable
    data class Success(
        val userLocation: Location?,
        val properties: List<Property>,
        val poi: List<Poi>
    ) : GoogleMapUiState

    data class Error(val message: String) : GoogleMapUiState

    data object Loading : GoogleMapUiState

}