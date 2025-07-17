package com.dcac.realestatemanager.data.offlinedatabase.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val email: String,
    val password: String,
    @ColumnInfo(name = "agent_name")
    val agentName: String,
)
