package com.dcac.realestatemanager.model

data class Photo(
    val id : Long,
    val propertyId: Long,
    val uri : String, // local
    val storageUrl: String = "", // cloud
    val description : String,
    val isSynced : Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
