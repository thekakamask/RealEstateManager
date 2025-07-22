package com.dcac.realestatemanager.model

data class PoiWithProperties(
    val poi: Poi,
    val properties: List<Property> = emptyList()
)