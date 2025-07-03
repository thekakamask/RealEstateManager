package com.dcac.realestatemanager.data.offlinedatabase.photo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Represents a photo associated with a specific property.
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "property_id")
    val propertyId: Long,  // FK: links to PropertyEntity.id
    val uri: String,
    val description: String
)