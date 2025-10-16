package com.dcac.realestatemanager.ui.homePage.propertiesListScreen


interface IPropertiesListViewModel {

    fun loadProperties()

    fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    )
    fun sortProperties(order: PropertySortOrder)
    fun clearSort()
    fun resetFilters()
    fun resetState()
}