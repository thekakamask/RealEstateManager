package com.dcac.realestatemanager.data.offlineDatabase.poi

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

// Represents a point of interest (POI) independent from properties
@Entity(
    tableName = "poi",
    indices = [
        Index(value = ["firestore_document_id"], unique = true)
    ])
data class PoiEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // 🔁 UUID
    @ColumnInfo(name = "firestore_document_id")
    val firestoreDocumentId: String? = null,
    val name: String,
    val type: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)