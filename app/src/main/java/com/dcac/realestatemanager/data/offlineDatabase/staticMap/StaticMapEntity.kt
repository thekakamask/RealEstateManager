package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity

@Entity(
    tableName = "static_map",
    indices = [
        Index(value = ["property_id"], unique = true),
        Index(value = ["firestore_document_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["property_id"],
            onDelete = ForeignKey.CASCADE // ou SET_NULL, NO_ACTION, etc.
        )
    ]
)
data class StaticMapEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "firestore_document_id")
    val firestoreDocumentId: String? = null,
    @ColumnInfo(name= "property_id")
    val universalLocalPropertyId: String,
    val uri: String = "",
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()

)