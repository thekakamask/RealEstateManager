package com.dcac.realestatemanager.ui.propertyDetailsPage

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsUiState.*
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.model.Property
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PropertyDetailsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val crossRefRepository: PropertyPoiCrossRepository,
    private val staticMapRepository: StaticMapRepository,
    private val syncScheduler: SyncScheduler
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
                    val staticMap = staticMapRepository.getStaticMapByPropertyId(propertyId).firstOrNull()

                    val linkedPoiIds = crossRefs
                        .filter {it.universalLocalPropertyId == propertyId }
                        .map {it.universalLocalPoiId}

                    val propertyPoiS = allPoiS.filter {it.universalLocalId in linkedPoiIds}

                    val fullProperty = property.copy(
                        photos= photos,
                        poiS = propertyPoiS,
                        staticMap = staticMap
                    )

                    val firebaseUid = authRepository.currentUser?.uid
                    val currentUserId = firebaseUid?.let {
                        userRepository.getUserByFirebaseUid(it).firstOrNull()?.universalLocalId
                    }

                    val isOwnedByCurrentUser = currentUserId == property.universalLocalUserId


                    _uiState.value = Success(
                        fullProperty,
                        userName = user?.agentName ?: "Unknown",
                        isOwnedByCurrentUser = isOwnedByCurrentUser
                    )
                } else {
                    _uiState.value = Error("Property not found")
                }
            } catch (e: Exception) {
                _uiState.value = Error("Failed to load property: ${e.message}")
            }
        }
    }

    override fun deleteProperty(
        property: Property,
        onDeleted: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val propertyId = property.universalLocalId

                property.photos.forEach { photo ->
                    photo.uri.toUri().path?.let { path ->
                        runCatching {
                            val file = File(path)
                            if (file.exists()) file.delete()
                        }
                    }
                    photoRepository.markPhotoAsDeleted(photo)
                }

                property.staticMap?.let { map ->
                    map.uri.toUri().path?.let { path ->
                        runCatching {
                            val file = File(path)
                            if (file.exists()) file.delete()
                        }
                    }
                    staticMapRepository.markStaticMapAsDeleted(map)
                }

                crossRefRepository.markCrossRefsAsDeletedForProperty(propertyId)
                propertyRepository.markPropertyAsDeleted(property)
                syncScheduler.scheduleSync()

                onDeleted()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}