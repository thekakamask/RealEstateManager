package com.dcac.realestatemanager.model

import java.util.UUID

data class Photo(
    val universalLocalId: String = UUID.randomUUID().toString(),    // 🔁 generate unique UUID for multi device
    val firestoreDocumentId: String? = null,
    val universalLocalPropertyId: String,
    val uri : String,
    val description : String? = null,
    val isSynced : Boolean = false,
    val isDeleted : Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
