package com.dcac.realestatemanager.data.offlineDatabase.property

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity

// Represents a real estate property stored in the database
@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["firestore_document_id"], unique = true)
              ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE // ou SET_NULL, NO_ACTION, etc.
        )
    ]
)
data class PropertyEntity(
    @PrimaryKey
    val id: String, // ✅ Stable UUID for multi device link from Property model
    @ColumnInfo(name = "firestore_document_id")
    val firestoreDocumentId: String? = null,
    @ColumnInfo(name = "user_id")
    val universalLocalUserId: String, // link with UUID of UserEntity
    val title: String,
    val type: String,
    val price: Int,
    val surface: Int,
    val rooms: Int,
    val description: String,
    val address: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    @ColumnInfo(name = "is_sold")
    val isSold: Boolean,
    @ColumnInfo(name = "entry_date")
    val entryDate: String,
    @ColumnInfo(name = "sale_date")
    val saleDate: String?,
   @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)