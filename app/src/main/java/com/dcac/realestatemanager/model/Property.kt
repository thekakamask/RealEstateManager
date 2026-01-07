package com.dcac.realestatemanager.model

import org.threeten.bp.LocalDate
import java.util.UUID

data class Property(
    // 🔁 generate unique UUID for multi device
    val universalLocalId: String = UUID.randomUUID().toString(),
    val firestoreDocumentId: String? = null,
    val universalLocalUserId: String,
    val title: String,
    val type: String,
    val price: Int,
    val surface: Int,
    val rooms: Int,
    val description: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSold: Boolean,
    val entryDate: LocalDate,
    val saleDate: LocalDate?,
    val staticMap: StaticMap?,
    val photos: List<Photo> = emptyList(),
    val poiS: List<Poi> = emptyList(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
