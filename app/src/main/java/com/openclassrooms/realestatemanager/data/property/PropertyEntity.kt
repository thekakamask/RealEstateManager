package com.openclassrooms.realestatemanager.data.property

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Represents a real estate property stored in the database.
@Entity(tableName = "properties")
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
    @ColumnInfo(name = "agent_name")
    val agentName: String
)