package com.dcac.realestatemanager.data.offlineDatabase.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val email: String,
    @ColumnInfo(name = "agent_name")
    val agentName: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
