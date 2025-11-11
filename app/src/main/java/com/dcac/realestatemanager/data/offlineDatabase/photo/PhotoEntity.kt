package com.dcac.realestatemanager.data.offlineDatabase.photo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity

//Represents a photo associated with a specific property.
@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["property_id"]),
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
data class PhotoEntity(
    @PrimaryKey
    val id: String, // ✅ Stable UUID for multi device link from Photo model
    @ColumnInfo(name = "firestore_document_id")
    val firestoreDocumentId: String? = null,
    @ColumnInfo(name = "property_id")
    val universalLocalPropertyId: String, // link with UUID of PropertyEntity
    val uri: String = "",
    val description: String? = null,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)