package com.dcac.realestatemanager.ui.propertyCreationPage

import android.net.Uri
import android.content.Context
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection
import kotlinx.coroutines.flow.StateFlow
import org.threeten.bp.LocalDate

interface IPropertyCreationViewModel {

    val uiState: StateFlow<PropertyCreationUiState>
    val isNextEnabled: Boolean

    fun goToNext()
    fun goToPrevious()

    // Step 1
    fun updateType(type: String)

    // Step 2
    fun updateStreet(value: String)
    fun updateCity(value: String)
    fun updatePostalCode(value: String)
    fun updateCountry(value: String)

    // Step 3
    fun updatePoiType(index: Int, type: String)
    fun updatePoiName(index: Int, name: String)
    fun updatePoiStreet(index: Int, street: String)
    fun updatePoiCity(index: Int, city: String)
    fun updatePoiPostalCode(index: Int, postalCode: String)
    fun updatePoiCountry(index: Int, country: String)

    // Step 4
    fun updateTitle(value: String)
    fun updatePrice(value: Int)
    fun updateSurface(value: Int)
    fun updateRooms(value: Int)
    fun updateDescription(value: String)
    fun updateIsSold(isSold: Boolean)
    fun updateSaleDate(date: LocalDate)

    // Step 5
    fun updatePhotoAt(index: Int, uri: Uri, description: String? = null)
    fun removePhotoAt(index: Int)
    fun onPhotoCellClicked(index: Int, launchPicker: () -> Unit)
    fun handlePhotoPicked(context: Context, uri: Uri)

    // Step 6
    fun fetchStaticMap(context: Context)

    // Step 7
    fun createModelFromDraft(context: Context, currency : String)

    fun updateModelFromDraft(context: Context, currency: String)
    fun loadDraftFromProperty(property: Property, section: EditSection, currency: String)

    // Reset
    fun resetState()
}