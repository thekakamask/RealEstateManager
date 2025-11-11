package com.dcac.realestatemanager.ui.propertyCreationPage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapConfig
import com.dcac.realestatemanager.model.Property

import com.dcac.realestatemanager.ui.propertyCreationPage.PropertyCreationUiState.*
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
class PropertyCreationViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val staticMapRepository: StaticMapRepository,
    @ApplicationContext private val appContext: Context // required to save static map
) : ViewModel(), IPropertyCreationViewModel {

    private val _uiState = MutableStateFlow<PropertyCreationUiState>(Idle)
    val uiState: StateFlow<PropertyCreationUiState> = _uiState.asStateFlow()

    override fun createProperty(property: Property) {
        /*viewModelScope.launch {
            _uiState.value = Loading

            try {
                val config = StaticMapConfig.createFromProperty(property)
                val bytes = staticMapRepository.getStaticMapImage(config)

                val savedPath = if (bytes != null) {
                    val fileName = "static_map_created_${System.currentTimeMillis()}.png"
                    staticMapRepository.saveStaticMapToLocal(appContext, fileName, bytes)
                } else null

                val finalProperty = if (savedPath != null) {
                    property.copy(staticMapPath = savedPath)
                } else property

                val insertedId = propertyRepository.insertProperty(finalProperty)
                val created = finalProperty.copy(id = insertedId)

                _uiState.value = Success(createdOrUpdatedProperty = created, isUpdate = false)

            } catch (e: Exception) {
                _uiState.value = Error("Failed to create property: ${e.message}")
            }
        }*/
    }

    override fun updateProperty(property: Property) {
        /*viewModelScope.launch {
            _uiState.value = Loading

            try {
                // Optional: regenerate static map if needed
                val config = StaticMapConfig.createFromProperty(property)
                val bytes = staticMapRepository.getStaticMapImage(config)

                val savedPath = if (bytes != null) {
                    val fileName = "static_map_updated_${System.currentTimeMillis()}.png"
                    staticMapRepository.saveStaticMapToLocal(appContext, fileName, bytes)
                } else null

                val updatedProperty = if (savedPath != null) {
                    property.copy(staticMapPath = savedPath)
                } else property

                propertyRepository.updateProperty(updatedProperty)

                _uiState.value = Success(createdOrUpdatedProperty = updatedProperty, isUpdate = true)

            } catch (e: Exception) {
                _uiState.value = Error("Failed to update property: ${e.message}")
            }
        }*/
    }

    override fun loadPropertyForEditing(propertyId: Long) {
        /*viewModelScope.launch {
            _uiState.value = Loading

            propertyRepository.getPropertyById(propertyId)
                .catch { e ->
                    _uiState.value = Error("Failed to load property: ${e.message}")
                }
                .collectLatest { property ->
                    if (property != null) {
                        _uiState.value = Editing(property)
                    } else {
                        _uiState.value = Error("Property not found")
                    }
                }
        }*/
    }

    override fun resetState() {
        //_uiState.value = Idle
    }
}
