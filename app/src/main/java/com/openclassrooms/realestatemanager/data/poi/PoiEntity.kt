package com.openclassrooms.realestatemanager.data.poi

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Represents a point of interest near a property.
@Entity(tableName = "poi")
data class PoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "property_id")
    val propertyId: Long,  // FK: links to PropertyEntity.id
    val name: String,
    val type: String  // e.g. "School", "Park", "Store"
)