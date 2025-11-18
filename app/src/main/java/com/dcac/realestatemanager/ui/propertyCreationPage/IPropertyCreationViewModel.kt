package com.dcac.realestatemanager.ui.propertyCreationPage

import android.net.Uri
import android.content.Context

interface IPropertyCreationViewModel {

    fun goToNext()
    fun goToPrevious()
    val isNextEnabled: Boolean
    fun updateType(type: String)
    fun updateStreet(value: String)
    fun updateCity(value: String)
    fun updatePostalCode(value: String)
    fun updateCountry(value: String)
    fun updatePoiType(index: Int, type: String)
    fun updatePoiName(index: Int, name: String)
    fun updatePoiStreet(index: Int, street: String)
    fun updatePoiCity(index: Int, city: String)
    fun updatePoiPostalCode(index: Int, postalCode: String)
    fun updatePoiCountry(index: Int, country: String)
    fun updatePrice(value : Int)
    fun updateSurface(value : Int)
    fun updateRooms(value : Int)
    fun updateDescription(value : String)
    fun updatePhotoAt(index: Int, uri: Uri, description: String? = null)
    fun onPhotoCellClicked(index: Int, launchPicker: () -> Unit)
    fun handlePhotoPicked(context: Context, uri: Uri)
    fun removePhotoAt(index: Int)
    fun fetchStaticMap(context: Context)
    fun createModelFromDraft()
    fun resetState()
}