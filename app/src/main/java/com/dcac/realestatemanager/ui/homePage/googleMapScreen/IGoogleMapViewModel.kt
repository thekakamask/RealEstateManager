package com.dcac.realestatemanager.ui.homePage.googleMapScreen

interface IGoogleMapViewModel {
    fun loadMapData()
    fun selectProperty(propertyId: Long)
    fun clearSelectedProperty()
}