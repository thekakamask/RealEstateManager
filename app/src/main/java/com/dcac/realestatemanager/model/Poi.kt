package com.dcac.realestatemanager.model

data class Poi(
    val id: Long,
    val name: String,
    val type: String,
    val isSynced : Boolean = false,
    val isDeleted : Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)