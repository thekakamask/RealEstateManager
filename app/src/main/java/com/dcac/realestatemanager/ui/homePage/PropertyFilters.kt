package com.dcac.realestatemanager.ui.homePage

data class PropertyFilters(
    val sortOrder: PropertySortOrder = PropertySortOrder.ALPHABETIC,
    val selectedType: String? = null,
    val isSold: Boolean? = null,
    val minSurface: Int? = null,
    val maxSurface: Int? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null
)

fun PropertyFilters.isEmpty(): Boolean {
    return minSurface == null &&
            maxSurface == null &&
            minPrice == null &&
            maxPrice == null &&
            selectedType.isNullOrBlank() &&
            isSold == null
}