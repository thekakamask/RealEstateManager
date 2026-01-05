package com.dcac.realestatemanager.ui.propertyCreationPage

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapConfig
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject
import android.content.Context
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID

@HiltViewModel
class PropertyCreationViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val propertyRepository: PropertyRepository,
    private val poiRepository: PoiRepository,
    private val photoRepository: PhotoRepository,
    private val crossRefRepository: PropertyPoiCrossRepository,
    private val authRepository: AuthRepository,
    private val staticMapRepository: StaticMapRepository,
    private val userRepository: UserRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel(), IPropertyCreationViewModel {

    private var propertyToEdit: Property? = null
    private var sectionToEdit: EditSection? = null

    private val _uiState = MutableStateFlow<PropertyCreationUiState>(
        StepState(
            currentStep = PropertyCreationStep.Intro,
            draft = PropertyDraft(),
            isNextEnabled =  validateStep(PropertyCreationStep.Intro, PropertyDraft())
        )
    )
    override val uiState: StateFlow<PropertyCreationUiState> = _uiState.asStateFlow()

    private fun stepState(): StepState {
        val state = _uiState.value
        require(state is StepState) {
            "The ViewModel is not currently in a StepState, current state = $state"
        }
        return state
    }

    private fun updateState(
        draft: PropertyDraft = stepState().draft,
        step: PropertyCreationStep = stepState().currentStep,
        error: String? = null,
        isLoadingMap: Boolean = stepState().isLoadingMap,
        staticMapImageBytes: List<Byte>? = stepState().staticMapImageBytes
    ) {
        _uiState.value = StepState(
            currentStep = step,
            draft = draft,
            isNextEnabled = validateStep(step, draft),
            error = error,
            isLoadingMap = isLoadingMap,
            staticMapImageBytes = staticMapImageBytes
        )
    }

    fun updateStep(step: PropertyCreationStep) {
        updateState(step = step)
    }

    private fun validateStep(step: PropertyCreationStep, draft: PropertyDraft): Boolean = when (step) {
        PropertyCreationStep.PropertyType -> draft.type.isNotBlank()
        PropertyCreationStep.Address -> draft.street.isNotBlank() &&
                draft.city.isNotBlank() && draft.postalCode.isNotBlank() && draft.country.isNotBlank()
        PropertyCreationStep.PoiS -> draft.poiS.any { it.name.isNotBlank() && it.type.isNotBlank() }
        PropertyCreationStep.Description ->
            draft.title.isNotBlank() &&
                    draft.price > 0 &&
                    draft.surface > 0 &&
                draft.rooms > 0 &&
                    draft.description.isNotBlank() &&
                (!draft.isSold || draft.saleDate != null)
        PropertyCreationStep.Photos -> draft.photos.any { it.uri.isNotBlank() }
        else -> true
    }

    override val isNextEnabled: Boolean
        get() = (uiState.value as? StepState)?.isNextEnabled ?: false

    override fun goToNext() {
        val next = when (stepState().currentStep) {
            PropertyCreationStep.Intro -> PropertyCreationStep.PropertyType
            PropertyCreationStep.PropertyType -> PropertyCreationStep.Address
            PropertyCreationStep.Address -> PropertyCreationStep.PoiS
            PropertyCreationStep.PoiS -> PropertyCreationStep.Description
            PropertyCreationStep.Description -> PropertyCreationStep.Photos
            PropertyCreationStep.Photos -> PropertyCreationStep.StaticMap
            PropertyCreationStep.StaticMap -> PropertyCreationStep.Confirmation
            PropertyCreationStep.Confirmation -> return
        }
        updateState(step = next)
    }

    override fun goToPrevious() {
        val previous = when (stepState().currentStep) {
            PropertyCreationStep.Confirmation -> PropertyCreationStep.StaticMap
            PropertyCreationStep.StaticMap -> PropertyCreationStep.Photos
            PropertyCreationStep.Photos -> PropertyCreationStep.Description
            PropertyCreationStep.Description -> PropertyCreationStep.PoiS
            PropertyCreationStep.PoiS -> PropertyCreationStep.Address
            PropertyCreationStep.Address -> PropertyCreationStep.PropertyType
            PropertyCreationStep.PropertyType -> PropertyCreationStep.Intro
            PropertyCreationStep.Intro -> return
        }
        updateState(step = previous)
    }

    private fun updateDraft(modifier: (PropertyDraft) -> PropertyDraft) {
        val current = stepState().draft
        updateState(draft = modifier(current))
    }

    override fun updateType(type: String) = updateDraft { it.copy(type = type) }
    override fun updateStreet(value: String) = updateDraft { it.copy(street = value) }
    override fun updateCity(value: String) = updateDraft { it.copy(city = value) }
    override fun updatePostalCode(value: String) = updateDraft { it.copy(postalCode = value) }
    override fun updateCountry(value: String) = updateDraft { it.copy(country = value) }
    override fun updateTitle(value: String) = updateDraft { it.copy(title = value) }
    override fun updatePrice(value: Int) = updateDraft { it.copy(price = value) }
    override fun updateSurface(value: Int) = updateDraft { it.copy(surface = value) }
    override fun updateRooms(value: Int) = updateDraft { it.copy(rooms = value) }
    override fun updateDescription(value: String) = updateDraft { it.copy(description = value) }
    override fun updateIsSold(isSold: Boolean) = updateDraft {
        it.copy(
            isSold = isSold,
            saleDate = if (isSold) it.saleDate else null
        )
    }
    override fun updateSaleDate(date: LocalDate) = updateDraft {
        it.copy(saleDate = date)
    }

    private fun updatePoi(index: Int, transform: (PoiDraft) -> PoiDraft) {
        val current = stepState().draft
        val list = current.poiS.toMutableList()
        while (list.size <= index) list.add(PoiDraft())
        list[index] = transform(list[index])
        updateDraft { it.copy(poiS = list) }
    }

    override fun updatePoiType(index: Int, type: String) = updatePoi(index) { it.copy(type = type) }
    override fun updatePoiName(index: Int, name: String) = updatePoi(index) { it.copy(name = name) }
    override fun updatePoiStreet(index: Int, street: String) = updatePoi(index) { it.copy(street = street) }
    override fun updatePoiCity(index: Int, city: String) = updatePoi(index) { it.copy(city = city) }
    override fun updatePoiPostalCode(index: Int, postalCode: String) = updatePoi(index) { it.copy(postalCode = postalCode) }
    override fun updatePoiCountry(index: Int, country: String) = updatePoi(index) { it.copy(country = country) }


    override fun updatePhotoAt(index: Int, uri: Uri, description: String?) {
        val current = stepState().draft
        val list = current.photos.toMutableList()
        while (list.size <= index) {
            list.add(Photo(universalLocalPropertyId = "TEMP", uri = ""))
        }
        list[index] = list[index].copy(
            uri = uri.toString(),
            description = description,
            updatedAt = System.currentTimeMillis()
        )
        updateDraft { it.copy(photos = list) }
    }

    override fun removePhotoAt(index: Int) {
        val current = stepState().draft
        val photos = current.photos.toMutableList()
        if (index in photos.indices) {
            photos[index] = photos[index].copy(uri = "")
            updateDraft { it.copy(photos = photos) }
        }
    }

    private var pendingPhotoIndex: Int? = null

    override fun onPhotoCellClicked(index: Int, launchPicker: () -> Unit) {
        pendingPhotoIndex = index
        launchPicker()
    }

    override fun handlePhotoPicked(context: Context, uri: Uri) {
        val index = pendingPhotoIndex ?: return
        val file = saveUriToAppStorage(context, uri) ?: return
        updatePhotoAt(index, file.toUri())
        pendingPhotoIndex = null
    }

    override fun fetchStaticMap(context: Context) {
        viewModelScope.launch {
            updateState(isLoadingMap = true)

            val draft = stepState().draft
            val address = listOf(draft.street, draft.postalCode, draft.city, draft.country)
                .filter { it.isNotBlank() }
                .joinToString(" ")

            if (address.isBlank()) {
                updateState(isLoadingMap = false)
                return@launch
            }

            val markers = mutableListOf("color:blue|label:P|$address")
            val labelMap = mapOf("Butcher" to "B", "Bakery" to "B", "Restaurant" to "R", "School" to "S", "Grocery" to "G")

            draft.poiS.forEach {
                val label = labelMap[it.type] ?: "P"
                val poiAddress = listOf(it.street, it.postalCode, it.city, it.country)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                if (poiAddress.isNotBlank()) {
                    markers.add("color:red|label:$label|$poiAddress")
                }
            }

            val config = StaticMapConfig(
                center = address,
                markers = markers,
                styles = listOf(
                    "feature:poi|visibility:off",
                    "feature:transit|visibility:off",
                    "feature:administrative|visibility:off",
                    "feature:landscape|visibility:simplified",
                    "feature:water|visibility:simplified"
                )
            )

            val bytes = staticMapRepository.getStaticMapImage(config)

            if (bytes == null) {
                updateState(isLoadingMap = false)
                return@launch
            }

            val fileName = "static_map_${UUID.randomUUID()}.png"

            val localPath = staticMapRepository.saveStaticMapToLocal(context, fileName, bytes)
            updateDraft { it.copy(staticMapPath = localPath) }

            updateState(
                isLoadingMap = false,
                staticMapImageBytes = bytes.toList()
            )
        }
    }
    override fun createModelFromDraft(context: Context, currency: String) {
        viewModelScope.launch {
            try {
                val draft = stepState().draft

                val firebaseUid = authRepository.currentUser?.uid ?: return@launch
                val localUser = userRepository.getUserByFirebaseUid(firebaseUid).firstOrNull()
                    ?: throw IllegalStateException("No local user found for uid=$firebaseUid")

                val propertyId = UUID.randomUUID().toString()
                val propertyAddress = "${draft.street}, ${draft.postalCode} ${draft.city}, ${draft.country}"
                val propertyLatLng = geocodeAddress(context, propertyAddress)

                val poiS = draft.poiS.filter { it.name.isNotBlank() && it.type.isNotBlank() }.map {
                    val poiAddress = "${it.street}, ${it.postalCode} ${it.city}, ${it.country}"
                    val latLng = geocodeAddress(context, poiAddress)
                    Poi(
                        name = it.name,
                        type = it.type,
                        address = poiAddress,
                        latitude = latLng?.latitude,
                        longitude = latLng?.longitude,
                        updatedAt = System.currentTimeMillis()
                    )
                }

                val photos = draft.photos.filter { it.uri.isNotBlank() }.map {
                    it.copy(universalLocalPropertyId = propertyId)
                }

                val priceToStore = if (currency == "EUR") {
                    CurrencyHelper.convertEuroToDollar(draft.price)
                } else {
                    draft.price
                }

                val property = Property(
                    universalLocalId = propertyId,
                    universalLocalUserId = localUser.universalLocalId,
                    title = draft.title,
                    type = draft.type,
                    price = priceToStore,
                    surface = draft.surface,
                    rooms = draft.rooms,
                    description = draft.description,
                    address = propertyAddress,
                    latitude = propertyLatLng?.latitude,
                    longitude = propertyLatLng?.longitude,
                    isSold = draft.isSold,
                    entryDate = LocalDate.now(),
                    saleDate = draft.saleDate,
                    staticMapPath = draft.staticMapPath,
                    photos = photos,
                    poiS = poiS
                )

                propertyRepository.insertPropertyFromUI(property)
                poiRepository.insertPoiSInsertFromUi(poiS)
                photoRepository.insertPhotosInsertFromUI(photos)

                poiS.forEach {
                    crossRefRepository.insertCrossRefInsertFromUI(PropertyPoiCross(propertyId, it.universalLocalId))
                }

                syncScheduler.scheduleSync()

                _uiState.value = Success(property, isUpdate = false)

            } catch (e: Exception) {
                Log.e("PropertyCreation", "Failed to insert property", e)
                _uiState.value = Error("Error: ${e.message}")
            }
        }
    }

    override fun loadDraftFromProperty(property: Property, section: EditSection, currency: String) {
        this.propertyToEdit = property
        this.sectionToEdit = section

        val parsedAddress = parseAddress(property.address)

        val price = if (currency == "EUR") {
            CurrencyHelper.convertDollarToEuro(property.price)
        } else {
            property.price
        }
        val draft = PropertyDraft(
            title = property.title,
            type = property.type,
            price = price,
            surface = property.surface,
            rooms = property.rooms,
            description = property.description,
            street = parsedAddress.street,
            city = parsedAddress.city,
            postalCode = parsedAddress.postalCode,
            country = parsedAddress.country,
            poiS = property.poiS.map {
                val parsedPoiAddress = parseAddress(it.address)
                PoiDraft(
                    name = it.name,
                    type = it.type,
                    street = parsedPoiAddress.street,
                    city = parsedPoiAddress.city,
                    postalCode = parsedPoiAddress.postalCode,
                    country = parsedPoiAddress.country
                )
            },
            photos = property.photos,
            staticMapPath = property.staticMapPath,
            isSold = property.isSold,
            saleDate = property.saleDate
        )

        _uiState.value = StepState(
            currentStep = PropertyCreationStep.Intro,
            draft = draft,
            isNextEnabled = true
        )
    }

    override fun updateModelFromDraft(context: Context, currency: String) {

        viewModelScope.launch {
            try {
                val draft = stepState().draft
                val original = propertyToEdit ?: return@launch
                val section = sectionToEdit ?: return@launch

                when (section) {
                    EditSection.TYPE -> {
                        val updated = original.copy(type = draft.type)
                        propertyRepository.updatePropertyFromUI(updated)
                    }

                    EditSection.ADDRESS -> {
                        val address = "${draft.street}, ${draft.postalCode} ${draft.city}, ${draft.country}"
                        val latLng = geocodeAddress(context, address)
                        val updated = original.copy(
                            address = address,
                            latitude = latLng?.latitude,
                            longitude = latLng?.longitude
                        )
                        propertyRepository.updatePropertyFromUI(updated)
                    }

                    EditSection.DESCRIPTION -> {

                        val priceToStore = if (currency == "EUR") {
                            CurrencyHelper.convertEuroToDollar(draft.price)
                        } else {
                            draft.price
                        }

                        val updated = original.copy(
                            title = draft.title,
                            price = priceToStore,
                            surface = draft.surface,
                            rooms = draft.rooms,
                            description = draft.description,
                            isSold = draft.isSold,
                            saleDate = draft.saleDate
                        )
                        propertyRepository.updatePropertyFromUI(updated)
                    }

                    EditSection.PHOTOS -> {
                        val originalPhotos = original.photos.filter { it.uri.isNotBlank() }
                        val draftPhotos = draft.photos.filter { it.uri.isNotBlank() }

                        val originalUris = originalPhotos.map { it.uri }

                        draftPhotos
                            .filter { it.uri in originalUris }
                            .forEach { draftPhoto ->
                                val originalPhoto = originalPhotos.first { it.uri == draftPhoto.uri }
                                val updatedPhoto = originalPhoto.copy(
                                    description = draftPhoto.description,
                                    updatedAt = System.currentTimeMillis()
                                )
                                photoRepository.updatePhotoFromUI(updatedPhoto)
                            }

                        draftPhotos
                            .filter { it.uri !in originalUris }
                            .forEach { newPhoto ->
                                val inserted = newPhoto.copy(
                                    universalLocalPropertyId = original.universalLocalId,
                                    updatedAt = System.currentTimeMillis()
                                )
                                photoRepository.insertPhotoInsertFromUI(inserted)
                            }

                        originalPhotos
                            .filter { it.uri !in draftPhotos.map { p -> p.uri } }
                            .forEach { photoRepository.markPhotoAsDeleted(it) }
                    }

                    EditSection.POIS -> {
                        val propertyId = original.universalLocalId
                        val originalPoiS = original.poiS
                        val draftPoiS = draft.poiS

                        for (i in draftPoiS.indices) {
                            val draftPoi = draftPoiS[i]
                            val address = "${draftPoi.street}, ${draftPoi.postalCode} ${draftPoi.city}, ${draftPoi.country}"
                            val latLng = geocodeAddress(context, address)

                            if (i < originalPoiS.size) {
                                val existingPoi = originalPoiS[i]
                                val updatedPoi = existingPoi.copy(
                                    name = draftPoi.name,
                                    type = draftPoi.type,
                                    address = address,
                                    latitude = latLng?.latitude,
                                    longitude = latLng?.longitude,
                                    updatedAt = System.currentTimeMillis()
                                )
                                poiRepository.updatePoiFromUI(updatedPoi)

                            } else {
                                val newPoi = Poi(
                                    universalLocalId = UUID.randomUUID().toString(),
                                    name = draftPoi.name,
                                    type = draftPoi.type,
                                    address = address,
                                    latitude = latLng?.latitude,
                                    longitude = latLng?.longitude,
                                    updatedAt = System.currentTimeMillis()
                                )
                                poiRepository.insertPoiInsertFromUI(newPoi)
                                crossRefRepository.insertCrossRefInsertFromUI(PropertyPoiCross(propertyId, newPoi.universalLocalId))
                            }
                        }
                    }

                }

                syncScheduler.scheduleSync()

                _uiState.value = Success(original.copy(
                ), isUpdate = true)

            } catch (e: Exception) {
                Log.e("PropertyUpdate", "Failed to update property", e)
                _uiState.value = Error("Error: ${e.message}")
            }
        }
    }

    override fun resetState() {
        _uiState.value = StepState(
            currentStep = PropertyCreationStep.Intro,
            draft = PropertyDraft(),
            isNextEnabled =  validateStep(PropertyCreationStep.Intro, PropertyDraft())
        )
    }

    val topBarTitle: String
        get() {
            val state = uiState.value
            val context = appContext
            if (state is StepState) {
                val section = sectionToEdit
                return if (propertyToEdit != null && section != null) {
                    when (section) {
                        EditSection.TYPE -> context.getString(R.string.property_creation_type_modification_title)
                        EditSection.ADDRESS -> context.getString(R.string.property_creation_address_modification_title)
                        EditSection.DESCRIPTION -> context.getString(R.string.property_creation_description_modification_title)
                        EditSection.PHOTOS -> context.getString(R.string.property_creation_photo_modification_title)
                        EditSection.POIS -> context.getString(R.string.property_creation_poi_modification_title)
                    }
                } else {
                    context.getString(R.string.property_creation_title)
                }
            }
            return ""
        }
}


