package com.openclassrooms.realestatemanager.data.poi

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing POIs associated with properties
@Dao
interface PoiDao {

    // Retrieve all POIs linked to a specific property by property ID
    @Query("SELECT * FROM poi WHERE property_id = :propertyId")
    fun getPoiForProperty(propertyId: Long): Flow<List<PoiEntity>>

    // Insert a list of POIs (e.g., during property creation or update)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(poi: List<PoiEntity>)

    // Delete all POIs linked to a specific property (e.g., when updating/removing a property)
    @Query("DELETE FROM poi WHERE property_id = :propertyId")
    suspend fun deletePoiForProperty(propertyId: Long)

    // Delete a single POI if needed (e.g., removing a specific entry manually)
    @Delete
    suspend fun deletePoi(poi: PoiEntity)
}