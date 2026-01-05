package com.dcac.realestatemanager.data.offlineDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoDao
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiDao
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyDao
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossDao
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapDao
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserDao
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity

// Room database definition for the application.
// - Declares entities used by the database: Property, Photo, POI.
// - Declares DAO accessors to interact with the database.
@Database(entities = [PropertyEntity::class, PhotoEntity::class, PoiEntity::class, UserEntity::class, PropertyPoiCrossEntity::class, StaticMapEntity::class],
    version = 1, // Version number of the schema. Increment it if the schema (entities, fields, relations) changes.
    exportSchema = false) // If true, Room will export the schema to a folder (used for schema versioning/testing). Disabled here.
abstract class RealEstateManagerDatabase : RoomDatabase() {
    // DAO accessors for Room to implement at compile time
    abstract fun propertyDao(): PropertyDao
    abstract fun photoDao(): PhotoDao
    abstract fun poiDao(): PoiDao
    abstract fun userDao(): UserDao
    abstract fun propertyCrossDao(): PropertyPoiCrossDao
    abstract fun staticMapDao(): StaticMapDao

    // Singleton instance to avoid multiple database instances at runtime.
    companion object {

        // Holds the singleton instance of the Room database.
        // Annotated with @Volatile to ensure proper synchronization:
        // - Guarantees that any thread accessing this variable sees the most up-to-date value.
        // - Prevents threads from using a cached version of the variable.
        // - Ensures visibility of writes to other threads (important for the double-checked locking pattern used in getDatabase()).
        @Volatile
        private var Instance: RealEstateManagerDatabase?= null

        //Builds or returns the existing Room database instance.
        //`synchronized` ensures only one thread can initialize the DB at a time, preventing race conditions during instance creation.
        //`Room.databaseBuilder()` creates a RoomDatabase with the provided context and DB class, Here we specify the DB name as "real_estate_manager_database".
        //`.also { Instance = it }` stores the built instance for future reuse.
        fun getDatabase(context : Context): RealEstateManagerDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    RealEstateManagerDatabase::class.java,
                    "real_estate_manager_database")  // Name of the SQLite DB file.
                    .fallbackToDestructiveMigration()
                    .build() // Create Room DB instance. No fallback strategy configured (migrations must be handled manually).
                    .also { Instance = it } // Save the instance for reuse.
            }
        }
    }
}