package com.dcac.realestatemanager.model

import java.util.UUID

data class StaticMap(
    val universalLocalId: String = UUID.randomUUID().toString(),
    val firestoreDocumentId: String? = null,
    val universalLocalPropertyId: String,
    val uri: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
