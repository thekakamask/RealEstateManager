package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyPoiCrossDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PropertyPoiCrossEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCrossEntity>)

    @Update
    suspend fun updateCrossRef(propertyPoiCrossEntity: PropertyPoiCrossEntity)

    @Query("DELETE FROM property_poi_cross_ref WHERE propertyId = :propertyId")
    suspend fun deleteCrossRefsForProperty(propertyId: Long)

    @Query("DELETE FROM property_poi_cross_ref WHERE poiId = :poiId")
    suspend fun deleteCrossRefsForPoi(poiId: Long)

    @Query("SELECT * FROM property_poi_cross_ref WHERE propertyId = :propertyId")
    fun getCrossRefsForProperty(propertyId: Long): Flow<List<PropertyPoiCrossEntity>>

    @Query("SELECT poiId FROM property_poi_cross_ref WHERE propertyId = :propertyId")
    fun getPoiIdsForProperty(propertyId: Long): Flow<List<Long>>

    @Query("SELECT propertyId FROM property_poi_cross_ref WHERE poiId = :poiId")
    fun getPropertyIdsForPoi(poiId: Long): Flow<List<Long>>

    @Query("SELECT * FROM property_poi_cross_ref")
    fun getAllCrossRefs(): Flow<List<PropertyPoiCrossEntity>>

    @Query("DELETE FROM property_poi_cross_ref")
    suspend fun clearAllCrossRefs()

    @Query("SELECT * FROM property_poi_cross_ref WHERE is_synced = 0")
    fun getUnSyncedPropertiesPoiSCross(): Flow<List<PropertyPoiCrossEntity>>

    @Query("SELECT * FROM property_poi_cross_ref WHERE propertyId = :propertyId AND poiId = :poiId")
    fun getCrossByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCrossRefFromFirebase(crossRef: PropertyPoiCrossEntity)

}