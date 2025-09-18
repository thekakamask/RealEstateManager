package com.dcac.realestatemanager.model

data class Photo(
    val id : Long,
    val propertyId: Long,
    val uri : String,
    val description : String? = null,
    val isSynced : Boolean = false,
    val isDeleted : Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
