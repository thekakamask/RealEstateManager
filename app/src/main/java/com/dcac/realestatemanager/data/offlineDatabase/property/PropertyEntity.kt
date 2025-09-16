package com.dcac.realestatemanager.data.offlineDatabase.property

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import androidx.room.Index

// Represents a real estate property stored in the database
@Entity(
    tableName = "properties",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val type: String,
    val price: Int,
    val surface: Int,
    val rooms: Int,
    val description: String,
    val address: String,
    @ColumnInfo(name = "is_sold")
    val isSold: Boolean,
    @ColumnInfo(name = "entry_date")
    val entryDate: String,
    @ColumnInfo(name = "sale_date")
    val saleDate: String?, // Nullable if not yet sold
    @ColumnInfo(name = "user_id")
    val userId: Long, // FK: links to UserEntity.id
    @ColumnInfo(name = "static_map_path")
    val staticMapPath: String? = null,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()

)