package com.dcac.realestatemanager.model

data class User(
    val id: Long,
    val email: String,
    val agentName: String,
    val firebaseUid: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
