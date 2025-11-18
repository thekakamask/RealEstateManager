package com.dcac.realestatemanager.ui.propertyCreationPage

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapConfig
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyPoiCross
import com.dcac.realestatemanager.ui.propertyCreationPage.PropertyCreationUiState.*
import com.dcac.realestatemanager.utils.Utils.saveUriToAppStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class PropertyCreationViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val poiRepository: PoiRepository,
    private val photoRepository: PhotoRepository,
    private val crossRefRepository: PropertyPoiCrossRepository,
    private val authRepository: AuthRepository,
    private val staticMapRepository: StaticMapRepository
) : ViewModel(), IPropertyCreationViewModel {

    private val _uiState = MutableStateFlow<PropertyCreationUiState>(Idle)
    val uiState: StateFlow<PropertyCreationUiState> = _uiState.asStateFlow()

    var currentStep by mutableStateOf<PropertyCreationStep>(PropertyCreationStep.Intro)
        private set

    var propertyDraft by mutableStateOf(PropertyDraft())
        private set

    override fun goToNext() {
        currentStep = when (currentStep) {
            is PropertyCreationStep.Intro -> PropertyCreationStep.PropertyType
            is PropertyCreationStep.PropertyType -> PropertyCreationStep.Address
            is PropertyCreationStep.Address -> PropertyCreationStep.PoiS
            is PropertyCreationStep.PoiS -> PropertyCreationStep.Description
            is PropertyCreationStep.Description -> PropertyCreationStep.Photos
            is PropertyCreationStep.Photos -> PropertyCreationStep.StaticMap
            is PropertyCreationStep.StaticMap -> PropertyCreationStep.Confirmation
            is PropertyCreationStep.Confirmation -> currentStep
        }
    }

    override fun goToPrevious() {
        currentStep = when (currentStep) {
            is PropertyCreationStep.Confirmation -> PropertyCreationStep.StaticMap
            is PropertyCreationStep.StaticMap -> PropertyCreationStep.Photos
            is PropertyCreationStep.Photos -> PropertyCreationStep.Description
            is PropertyCreationStep.Description -> PropertyCreationStep.PoiS
            is PropertyCreationStep.PoiS -> PropertyCreationStep.Address
            is PropertyCreationStep.Address -> PropertyCreationStep.PropertyType
            is PropertyCreationStep.PropertyType -> PropertyCreationStep.Intro
            is PropertyCreationStep.Intro -> currentStep
        }
    }

    override val isNextEnabled: Boolean
        get() = when (currentStep) {
            is PropertyCreationStep.PropertyType -> propertyDraft.type.isNotBlank()
            is PropertyCreationStep.Address -> propertyDraft.street.isNotBlank() &&
                    propertyDraft.city.isNotBlank() &&
                    propertyDraft.postalCode.isNotBlank() &&
                    propertyDraft.country.isNotBlank()
            is PropertyCreationStep.PoiS -> propertyDraft.poiS.any { it.name.isNotBlank() && it.type.isNotBlank() }
            is PropertyCreationStep.Description ->propertyDraft.price > 0 &&
                    propertyDraft.surface > 0 &&
                    propertyDraft.rooms > 0 &&
                    propertyDraft.description.isNotBlank()
            is PropertyCreationStep.Photos -> propertyDraft.photos.any { it.uri.isNotBlank() }
            else -> true
        }


    override fun updateType(type: String) {
        propertyDraft = propertyDraft.copy(type = type)
    }

    override fun updateStreet(value: String) {
        propertyDraft = propertyDraft.copy(street = value)
    }

    override fun updateCity(value: String) {
        propertyDraft = propertyDraft.copy(city = value)
    }

    override fun updatePostalCode(value: String) {
        propertyDraft = propertyDraft.copy(postalCode = value)
    }

    override fun updateCountry(value: String) {
        propertyDraft = propertyDraft.copy(country = value)
    }

    override fun updatePoiType(index: Int, type: String) {
        val currentList = propertyDraft.poiS.toMutableList()
        val existing = currentList.getOrNull(index) ?: PoiDraft("", "", "", "", "", "")
        val updated = existing.copy(type = type)

        while (currentList.size <= index) {
            currentList.add(PoiDraft("", "", "", "", "", ""))
        }

        currentList[index] = updated
        propertyDraft = propertyDraft.copy(poiS = currentList)
    }

    override fun updatePoiName(index: Int, name: String) {
        val currentList = propertyDraft.poiS.toMutableList()
        val existing = currentList.getOrNull(index) ?: PoiDraft("", "", "", "", "", "")
        val updated = existing.copy(name = name)

        while (currentList.size <= index) {
            currentList.add(PoiDraft("", "", "", "", "", ""))
        }

        currentList[index] = updated
        propertyDraft = propertyDraft.copy(poiS = currentList)
    }

    override fun updatePoiStreet(index: Int, street: String) {
        val currentList = propertyDraft.poiS.toMutableList()
        val existing = currentList.getOrNull(index) ?: PoiDraft("", "", "", "", "", "")
        val updated = existing.copy(street = street)

        while (currentList.size <= index) {
            currentList.add(PoiDraft("", "", "", "", "", ""))
        }

        currentList[index] = updated
        propertyDraft = propertyDraft.copy(poiS = currentList)
    }

    override fun updatePoiCity(index: Int, city: String) {
        val currentList = propertyDraft.poiS.toMutableList()
        val existing = currentList.getOrNull(index) ?: PoiDraft("", "", "", "", "", "")
        val updated = existing.copy(city = city)

        while (currentList.size <= index) {
            currentList.add(PoiDraft("", "", "", "", "", ""))
        }

        currentList[index] = updated
        propertyDraft = propertyDraft.copy(poiS = currentList)
    }

    override fun updatePoiPostalCode(index: Int, postalCode: String) {
        val currentList = propertyDraft.poiS.toMutableList()
        val existing = currentList.getOrNull(index) ?: PoiDraft("", "", "", "", "", "")
        val updated = existing.copy(postalCode = postalCode)

        while (currentList.size <= index) {
            currentList.add(PoiDraft("", "", "", "", "", ""))
        }

        currentList[index] = updated
        propertyDraft = propertyDraft.copy(poiS = currentList)
    }

    override fun updatePoiCountry(index: Int, country: String) {
        val currentList = propertyDraft.poiS.toMutableList()
        val existing = currentList.getOrNull(index) ?: PoiDraft("", "", "", "", "", "")
        val updated = existing.copy(country = country)

        while (currentList.size <= index) {
            currentList.add(PoiDraft("", "", "", "", "", ""))
        }

        currentList[index] = updated
        propertyDraft = propertyDraft.copy(poiS = currentList)
    }

    override fun updatePrice(value : Int) {
        propertyDraft = propertyDraft.copy(price = value)
    }

    override fun updateSurface(value : Int) {
        propertyDraft = propertyDraft.copy(surface = value)
    }


    override fun updateRooms(value : Int) {
        propertyDraft = propertyDraft.copy(rooms = value)
    }

    override fun updateDescription(value : String) {
        propertyDraft = propertyDraft.copy(description = value)
    }

    override fun updatePhotoAt(index: Int, uri: Uri, description: String?) {
        val currentList = propertyDraft.photos.toMutableList()

        while (currentList.size <= index) {
            currentList.add(
                Photo(
                    universalLocalPropertyId = "TEMP",
                    uri = "",
                    description = null
                )
            )
        }

        val updated = currentList[index].copy(
            uri = uri.toString(),
            description = description,
            updatedAt = System.currentTimeMillis()
        )

        currentList[index] = updated
        propertyDraft = propertyDraft.copy(photos = currentList)
    }

    private var pendingPhotoIndex: Int? = null

    override fun onPhotoCellClicked(index: Int, launchPicker: () -> Unit) {
        pendingPhotoIndex = index
        launchPicker()
    }

    override fun handlePhotoPicked(context: Context, uri: Uri) {
        val index = pendingPhotoIndex ?: return
        val localFile = saveUriToAppStorage(context, uri)
        val localUri = localFile?.toUri() ?: return

        updatePhotoAt(index, localUri)
        pendingPhotoIndex = null // reset
    }

    override fun removePhotoAt(index: Int) {
        val currentList = propertyDraft.photos.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(uri = "")
            propertyDraft = propertyDraft.copy(photos = currentList)
        }
    }

    val staticMapImageBytes = mutableStateOf<ByteArray?>(null)
    val isLoadingMap = mutableStateOf(false)

    private val cleanStyles = listOf(
        "feature:poi|visibility:off",
        "feature:transit|visibility:off",
        "feature:administrative|visibility:off",
        "feature:landscape|visibility:simplified",
        "feature:water|visibility:simplified"
    )


    override fun fetchStaticMap(context: Context) {
        viewModelScope.launch {
            isLoadingMap.value = true

            val fullAddress = listOf(
                propertyDraft.street,
                propertyDraft.postalCode,
                propertyDraft.city,
                propertyDraft.country
            ).filter { it.isNotBlank() }.joinToString(" ")

            if (fullAddress.isBlank()) {
                isLoadingMap.value = false
                return@launch
            }

            val markers = mutableListOf<String>()
            markers.add("color:blue|label:P|$fullAddress")

            val labelMap = mapOf(
                "Butcher"     to "B",
                "Bakery"      to "B",
                "Restaurant"  to "R",
                "School"      to "S",
                "Supermarket" to "S"
            )

            markers.addAll(
                propertyDraft.poiS.mapNotNull { poi ->
                    if (poi.name.isNotBlank() && poi.type.isNotBlank()) {
                        val poiAddress = listOf(
                            poi.street,
                            poi.postalCode,
                            poi.city,
                            poi.country
                        )
                            .filter { it.isNotBlank() }
                            .joinToString(" ")

                        if (poiAddress.isBlank()) return@mapNotNull null

                        val label = labelMap[poi.type] ?: "P" // fallback

                        "color:red|label:$label|$poiAddress"
                    } else null
                }
            )
            val config = StaticMapConfig(
                center = fullAddress,
                markers = markers,
                styles = cleanStyles
            )
            val bytes = staticMapRepository.getStaticMapImage(config)
            if (bytes != null) {
                staticMapImageBytes.value = bytes
                val filePath = staticMapRepository.saveStaticMapToLocal(context, "temp_static_map.png", bytes)
                propertyDraft = propertyDraft.copy(staticMapPath = filePath)
            }

            isLoadingMap.value = false
        }
    }



    override fun createModelFromDraft() {
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUser?.uid ?: return@launch

                val propertyId = java.util.UUID.randomUUID().toString()

                val poiList = propertyDraft.poiS
                    .filter { it.name.isNotBlank() && it.type.isNotBlank() }
                    .map { draft ->
                        Poi(
                            name = draft.name,
                            type = draft.type,
                            address = "${draft.street}, ${draft.postalCode} ${draft.city}, ${draft.country}",
                            updatedAt = System.currentTimeMillis()
                        )
                    }

                val property = Property(
                    universalLocalId = propertyId,
                    universalLocalUserId = userId,
                    title = propertyDraft.title,
                    type = propertyDraft.type,
                    price = propertyDraft.price,
                    surface = propertyDraft.surface,
                    rooms = propertyDraft.rooms,
                    description = propertyDraft.description,
                    address = "${propertyDraft.street}, ${propertyDraft.postalCode} ${propertyDraft.city}, ${propertyDraft.country}",
                    isSold = false,
                    entryDate = LocalDate.now(),
                    saleDate = null,
                    staticMapPath = propertyDraft.staticMapPath,
                    photos = propertyDraft.photos
                        .filter { it.uri.isNotBlank() }
                        .map { photo ->
                            photo.copy(universalLocalPropertyId = propertyId)
                        },
                    poiS = poiList
                )

                propertyRepository.insertPropertyFromUI(property)
                poiRepository.insertPoiSInsertFromUi(property.poiS)
                photoRepository.insertPhotosInsertFromUI(property.photos)

                property.poiS.forEach { poi ->
                    val crossRef = PropertyPoiCross(
                        universalLocalPropertyId = property.universalLocalId,
                        universalLocalPoiId = poi.universalLocalId
                    )
                    crossRefRepository.insertCrossRefInsertFromUI(crossRef)
                }

                _uiState.value = Success(
                    createdOrUpdatedProperty = property,
                    isUpdate = false
                )

                resetState()

            } catch (e: Exception) {
                _uiState.value = Error("Failed to create property: ${e.message}")
            }
        }
    }


    override fun resetState() {
        propertyDraft = PropertyDraft()
        currentStep = PropertyCreationStep.Intro
        _uiState.value = Idle
    }
}
