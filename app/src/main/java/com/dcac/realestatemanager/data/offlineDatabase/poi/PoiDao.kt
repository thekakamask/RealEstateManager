package com.dcac.realestatemanager.data.offlineDatabase.poi

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing POIs associated with properties
@Dao
interface PoiDao {

    @Query("SELECT * FROM poi")
    fun getAllPoiS(): Flow<List<PoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPoiS(poiS: List<PoiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PoiEntity)

    @Delete
    suspend fun deletePoi(poi: PoiEntity)

    @Transaction
    @Query("SELECT * FROM poi WHERE id = :poiId")
    fun getPoiWithProperties(poiId: Long): Flow<PoiWithPropertiesRelation>
}