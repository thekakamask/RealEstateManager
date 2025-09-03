package com.dcac.realestatemanager.model

data class PropertyPoiCross(
    val propertyId: Long,
    val poiId: Long,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)