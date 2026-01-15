package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity

@Entity(
    tableName = "property_poi_cross_ref",
    primaryKeys = ["property_id", "poi_id"],
    indices = [
        Index(value = ["property_id"]),
        Index(value = ["poi_id"]),
        Index(value = ["is_deleted"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["property_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PoiEntity::class,
            parentColumns = ["id"],
            childColumns = ["poi_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PropertyPoiCrossEntity(
    @ColumnInfo(name = "property_id")
    val universalLocalPropertyId: String, // 🔁 UUID unique link between multi device
    @ColumnInfo(name = "poi_id")
    val universalLocalPoiId: String,      // 🔁 UUID unique link between multi device
    @ColumnInfo(name = "firestore_document_id")
    val firestoreDocumentId: String? = null,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)