package com.dcac.realestatemanager.databaseTest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.dcac.realestatemanager.data.offlineDatabase.RealEstateManagerDatabase
import org.junit.After
import org.junit.Before
import java.io.IOException
import kotlin.jvm.Throws

abstract class DatabaseSetup {
    protected lateinit var db: RealEstateManagerDatabase

    @Before
    fun initDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RealEstateManagerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}