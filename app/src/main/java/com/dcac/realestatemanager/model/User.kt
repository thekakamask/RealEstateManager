package com.dcac.realestatemanager.model

import java.util.UUID

data class User(
    val universalLocalId: String = UUID.randomUUID().toString(),    // 🔁 generate unique UUID for multi device
    val firebaseUid: String,
    val email: String,
    val agentName: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
