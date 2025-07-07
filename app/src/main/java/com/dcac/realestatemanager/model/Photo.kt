package com.dcac.realestatemanager.model

data class Photo(
    val id : Long,
    val propertyId: Long,
    val uri : String,
    val description : String
)
