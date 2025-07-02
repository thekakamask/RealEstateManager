package com.openclassrooms.realestatemanager.data

import android.content.Context
import com.openclassrooms.realestatemanager.data.photo.OfflinePhotoRepository
import com.openclassrooms.realestatemanager.data.photo.PhotoRepository
import com.openclassrooms.realestatemanager.data.poi.OfflinePoiRepository
import com.openclassrooms.realestatemanager.data.poi.PoiRepository
import com.openclassrooms.realestatemanager.data.property.OfflinePropertyRepository
import com.openclassrooms.realestatemanager.data.property.PropertyRepository

// Dependency container interface for the app.
// Used to expose the different repositories to be injected into ViewModels or other components.
// Promotes a clean separation of concerns and easy testing/mocking.
interface AppContainer{
    val propertyRepository: PropertyRepository
    val photoRepository: PhotoRepository
    val poiRepository: PoiRepository
}

//Default implementation of AppContainer.
// Acts as a central place to create and provide all the repositories (in this case, Offline ones).
// Uses lazy initialization to ensure each repository is created only once when first accessed.
class AppDataContainer(private val context: Context) : AppContainer {

    // Provides access to property data via the OfflinePropertyRepository
    override val propertyRepository: PropertyRepository by lazy {
        OfflinePropertyRepository(RealEstateManagerDatabase.getDatabase(context).propertyDao())
    }

    // Provides access to photo data via the OfflinePhotoRepository
    override val photoRepository: PhotoRepository by lazy {
        OfflinePhotoRepository(RealEstateManagerDatabase.getDatabase(context).photoDao())
    }

    // Provides access to POI data via the OfflinePoiRepository
    override val poiRepository: PoiRepository by lazy {
        OfflinePoiRepository(RealEstateManagerDatabase.getDatabase(context).poiDao())
    }
}