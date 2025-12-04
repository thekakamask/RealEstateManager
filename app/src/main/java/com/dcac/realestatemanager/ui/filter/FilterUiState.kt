package com.dcac.realestatemanager.ui.filter

import androidx.compose.runtime.Immutable

@Immutable
data class FilterUiState(
    val sortOrder: PropertySortOrder,
    val selectedType: String,
    val isSold: Boolean?,
    val minSurface: String,
    val maxSurface: String,
    val minPrice: String,
    val maxPrice: String
)