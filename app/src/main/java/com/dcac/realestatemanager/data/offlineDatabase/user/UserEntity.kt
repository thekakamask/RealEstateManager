package com.dcac.realestatemanager.data.offlineDatabase.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["firebase_uid"], unique = true),
        Index(value = ["is_deleted"])
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String, // ✅ Stable UUID for multi device link from User model
    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String,   // ✅ for Firebase Auth mapping
    val email: String,
    @ColumnInfo(name = "agent_name")
    val agentName: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
