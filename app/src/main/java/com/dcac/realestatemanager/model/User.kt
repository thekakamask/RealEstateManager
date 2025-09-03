package com.dcac.realestatemanager.model

data class User(
    val id: Long,
    val email: String,
    val agentName: String,
    val isSynced: Boolean = false,
    val firebaseUid: String,
    val updatedAt: Long = System.currentTimeMillis()
)
