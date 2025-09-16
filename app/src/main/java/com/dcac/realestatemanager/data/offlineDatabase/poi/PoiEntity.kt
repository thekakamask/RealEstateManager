package com.dcac.realestatemanager.data.offlineDatabase.poi

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a point of interest (POI) independent from properties
@Entity(tableName = "poi")
data class PoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)