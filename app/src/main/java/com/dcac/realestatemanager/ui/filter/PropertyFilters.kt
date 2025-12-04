package com.dcac.realestatemanager.ui.filter

data class PropertyFilters(
    val sortOrder: PropertySortOrder = PropertySortOrder.ALPHABETIC,
    val selectedType: String? = null,
    val isSold: Boolean? = null,
    val minSurface: Int? = null,
    val maxSurface: Int? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null
)

fun PropertyFilters.toUiState(): FilterUiState = FilterUiState(
    sortOrder = this.sortOrder,
    selectedType = this.selectedType.orEmpty(),
    isSold = this.isSold,
    minSurface = this.minSurface?.toString().orEmpty(),
    maxSurface = this.maxSurface?.toString().orEmpty(),
    minPrice = this.minPrice?.toString().orEmpty(),
    maxPrice = this.maxPrice?.toString().orEmpty()
)

fun PropertyFilters.isEmpty(): Boolean {
    return minSurface == null &&
            maxSurface == null &&
            minPrice == null &&
            maxPrice == null &&
            selectedType == null &&
            isSold == null
}