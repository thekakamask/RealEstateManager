package com.dcac.realestatemanager.ui.propertyDetailsPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsUiState.*
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val crossRefRepository: PropertyPoiCrossRepository,
) : ViewModel(), IPropertyDetailsViewModel {

    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(Loading)
    override val uiState: StateFlow<PropertyDetailsUiState> = _uiState.asStateFlow()

    override fun loadPropertyDetails(propertyId: String) {
        viewModelScope.launch {
            try {
                val property = propertyRepository.getPropertyById(propertyId).firstOrNull()
                if (property != null) {
                    val user = userRepository.getUserById(property.universalLocalUserId).firstOrNull()
                    val photos = photoRepository.getPhotosByPropertyId(propertyId).firstOrNull() ?: emptyList()
                    val crossRefs = crossRefRepository.getAllCrossRefs().firstOrNull() ?: emptyList()
                    val allPoiS = poiRepository.getAllPoiS().firstOrNull() ?: emptyList()

                    val linkedPoiIds = crossRefs
                        .filter {it.universalLocalPropertyId == propertyId }
                        .map {it.universalLocalPoiId}

                    val propertyPoiS = allPoiS.filter {it.universalLocalId in linkedPoiIds}

                    val fullProperty = property.copy(
                        photos= photos,
                        poiS = propertyPoiS
                    )

                    _uiState.value = Success(
                        fullProperty,
                        userName = user?.agentName ?: "Unknown"
                    )
                } else {
                    _uiState.value = Error("Property not found")
                }
            } catch (e: Exception) {
                _uiState.value = Error("Failed to load property: ${e.message}")
            }
        }
    }
}