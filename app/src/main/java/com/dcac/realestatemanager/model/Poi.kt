package com.dcac.realestatemanager.model

import java.util.UUID

data class Poi(
    // 🔁 generate unique UUID for multi device
    val universalLocalId: String = UUID.randomUUID().toString(),
    val firestoreDocumentId: String? = null,
    val name: String,
    val type: String,
    val isSynced : Boolean = false,
    val isDeleted : Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)