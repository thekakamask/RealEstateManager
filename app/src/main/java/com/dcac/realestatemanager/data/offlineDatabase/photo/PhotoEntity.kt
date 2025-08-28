package com.dcac.realestatemanager.data.offlineDatabase.photo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import androidx.room.Index

//Represents a photo associated with a specific property.
@Entity(tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["property_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [ Index(value = ["property_id"]) ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "property_id")
    val propertyId: Long,  // FK: links to PropertyEntity.id
    val uri: String,
    val description: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)