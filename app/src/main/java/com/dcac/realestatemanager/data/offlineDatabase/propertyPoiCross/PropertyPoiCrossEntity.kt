package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity

@Entity(
    tableName = "property_poi_cross_ref",
    primaryKeys = ["propertyId", "poiId"],
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PoiEntity::class,
            parentColumns = ["id"],
            childColumns = ["poiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["propertyId"]), Index(value = ["poiId"])]
)
data class PropertyPoiCrossEntity(
    val propertyId: Long,
    val poiId: Long
)