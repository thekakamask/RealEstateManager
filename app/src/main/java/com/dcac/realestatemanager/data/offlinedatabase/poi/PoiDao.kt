package com.dcac.realestatemanager.data.offlinedatabase.poi

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing POIs associated with properties
@Dao
interface PoiDao {

    @Query("SELECT * FROM poi WHERE property_id = :propertyId")
    fun getPoiSForProperty(propertyId: Long): Flow<List<PoiEntity>>

    @Query("SELECT * FROM poi")
    fun getAllPoiS(): Flow<List<PoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPoiS(poiS: List<PoiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PoiEntity)

    @Query("DELETE FROM poi WHERE property_id = :propertyId")
    suspend fun deletePoiSForProperty(propertyId: Long)

    @Delete
    suspend fun deletePoi(poi: PoiEntity)
}