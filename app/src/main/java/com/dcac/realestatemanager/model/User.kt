package com.dcac.realestatemanager.model

data class User(
    val id: Long,
    val email: String,
    val password: String,
    val agentName: String,
    val isSynced: Boolean = false
)
