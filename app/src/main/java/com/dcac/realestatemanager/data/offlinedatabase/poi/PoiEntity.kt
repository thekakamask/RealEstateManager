package com.dcac.realestatemanager.data.offlinedatabase.poi

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a point of interest (POI) independent from properties
@Entity(tableName = "poi")
data class PoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String
)