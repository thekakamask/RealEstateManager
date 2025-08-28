package com.dcac.realestatemanager.model

import org.threeten.bp.LocalDate

data class Property(
    val id: Long,
    val title: String,
    val type: String,
    val price: Int,
    val surface: Int,
    val rooms: Int,
    val description: String,
    val address: String,
    val isSold: Boolean,
    val entryDate: LocalDate,
    val saleDate: LocalDate?,
    val staticMapPath: String? = null,
    val user: User,
    val photos: List<Photo> = emptyList(),
    val poiS: List<Poi> = emptyList(),
    val isSynced: Boolean = false
)
