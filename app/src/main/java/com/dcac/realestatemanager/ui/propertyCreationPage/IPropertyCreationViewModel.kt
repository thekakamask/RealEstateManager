package com.dcac.realestatemanager.ui.propertyCreationPage

import com.dcac.realestatemanager.model.Property

interface IPropertyCreationViewModel {

    fun createProperty(property: Property)
    fun resetState()
    fun loadPropertyForEditing(propertyId: Long)
    fun updateProperty(property: Property)
}