package com.dcac.realestatemanager.model


data class PropertyPoiCross(
    val universalLocalPropertyId: String,
    val universalLocalPoiId: String,
    val firestoreDocumentId: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)