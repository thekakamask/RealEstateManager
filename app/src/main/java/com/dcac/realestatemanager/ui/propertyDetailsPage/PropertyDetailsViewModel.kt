package com.dcac.realestatemanager.ui.propertyDetailsPage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsUiState.*
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapConfig
import com.dcac.realestatemanager.data.offlineStaticMap.createFromProperty
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository, // Gets full property data (with POIs, photos, user)
    private val staticMapRepository: StaticMapRepository, // Used to fetch and save the static map image
    @ApplicationContext private val appContext: Context // Needed to save the image in local storage
) : ViewModel(), IPropertyDetailsViewModel {

    // Backing property for UI state (Loading, Success, Error)
    private val _uiState = MutableStateFlow<PropertyDetailsUiState>(Loading)
    val uiState: StateFlow<PropertyDetailsUiState> = _uiState.asStateFlow()

    // Called when navigating to the PropertyDetails screen
    override fun loadPropertyDetails(propertyId: Long) {
        /*viewModelScope.launch {
            _uiState.value = Loading  // Start with loading state

            // Fetch property from local DB (as Flow)
            propertyRepository.getPropertyById(propertyId)
                .catch { e -> // If anything fails (DB error, etc.)
                    _uiState.value = Error("Failed to load property: ${e.message}")
                }
                .collectLatest { property -> // Collect the latest value emitted
                    if (property == null) {
                        _uiState.value = Error("Property not found.")
                        return@collectLatest
                    }

                    // If the property already has a static map saved, skip generation
                    if (!property.staticMapPath.isNullOrEmpty()) {
                        _uiState.value = Success(property)
                        return@collectLatest
                    }

                    // Creates the StaticMapConfig (based on address)
                    val config = StaticMapConfig.createFromProperty(property)

                    // Call Retrofit API to get map image bytes
                    val bytes = staticMapRepository.getStaticMapImage(config)

                    if (bytes != null) {
                        // Save image locally
                        val fileName = "static_map_${property.id}.png"
                        val savedPath = staticMapRepository.saveStaticMapToLocal(appContext, fileName, bytes)

                        // If saved successfully, update property
                        val updatedProperty = if (savedPath != null) {
                            property.copy(staticMapPath = savedPath)
                        } else {
                            property // fallback: return without static map
                        }

                        _uiState.value = Success(updatedProperty)
                    } else {
                        // No map, but still return property (not critical)
                        _uiState.value = Success(property)
                    }
                }
        }*/
    }
}
