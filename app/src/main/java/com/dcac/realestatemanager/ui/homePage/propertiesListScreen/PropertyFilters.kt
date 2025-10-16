package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

data class PropertyFilters(
    val minSurface: Int? = null,
    val maxSurface: Int? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val type: String? = null,
    val isSold: Boolean? = null
)
