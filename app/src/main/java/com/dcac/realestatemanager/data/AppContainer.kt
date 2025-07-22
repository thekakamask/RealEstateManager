package com.dcac.realestatemanager.data

import android.content.Context
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import com.dcac.realestatemanager.data.googleMap.OnlineGoogleMapRepository
import com.dcac.realestatemanager.data.offlineStaticMap.OfflineStaticMapRepository
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.RealEstateManagerDatabase
import com.dcac.realestatemanager.data.offlineDatabase.photo.OfflinePhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.OfflinePoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.OfflinePropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.OfflinePropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.userOnlineConnection.AuthRepository
import com.dcac.realestatemanager.data.userOnlineConnection.OnlineAuthRepository
import com.dcac.realestatemanager.network.StaticMapApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

// Dependency container interface for the app.
// Used to expose the different repositories to be injected into ViewModels or other components.
// Promotes a clean separation of concerns and easy testing/mocking.
interface AppContainer{
    val propertyRepository: PropertyRepository
    val photoRepository: PhotoRepository
    val poiRepository: PoiRepository
    val propertyPoiCrossRepository: PropertyPoiCrossRepository
    val userRepository: UserRepository
    val staticMapRepository: StaticMapRepository
    val googleMapRepository: GoogleMapRepository
    val authRepository : AuthRepository
}

//Default implementation of AppContainer.
// Acts as a central place to create and provide all the repositories (in this case, Offline ones).
// Uses lazy initialization to ensure each repository is created only once when first accessed.
class AppDataContainer(private val context: Context) : AppContainer {

    // Base URL for all Google Maps API requests (Static Maps, Directions, etc.)
    // This generic root allows us to reuse the Retrofit instance across multiple Maps-related services.
    private val mapBaseUrl = "https://maps.googleapis.com/maps/api/"

    // Retrofit instance configured with base URL and JSON converter (Kotlinx Serialization).
    // This instance is reused to create services for Maps API endpoints.
    private val mapRetrofit = Retrofit.Builder()
        .baseUrl(mapBaseUrl)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    // Provides access to property data via the OfflinePropertyRepository
    override val propertyRepository: PropertyRepository by lazy {
        OfflinePropertyRepository(
            RealEstateManagerDatabase.getDatabase(context).propertyDao(),
            poiRepository,
            photoRepository,
            propertyPoiCrossRepository
        )
    }

    override val propertyPoiCrossRepository: PropertyPoiCrossRepository by lazy {
        OfflinePropertyPoiCrossRepository(
            RealEstateManagerDatabase.getDatabase(context).propertyCrossDao()
        )
    }

    // Provides access to photo data via the OfflinePhotoRepository
    override val photoRepository: PhotoRepository by lazy {
        OfflinePhotoRepository(RealEstateManagerDatabase.getDatabase(context).photoDao())
    }

    // Provides access to POI data via the OfflinePoiRepository
    override val poiRepository: PoiRepository by lazy {
        OfflinePoiRepository(RealEstateManagerDatabase.getDatabase(context).poiDao())
    }

    override val userRepository: UserRepository by lazy {
        OfflineUserRepository(RealEstateManagerDatabase.getDatabase(context).userDao())
    }

    // Retrofit service interface implementation for the Static Maps API.
    // Built using the shared Retrofit instance (mapRetrofit).
    private val staticMapRetrofitService : StaticMapApiService by lazy {
        mapRetrofit.create(StaticMapApiService::class.java)
    }

    // Repository responsible for requesting static map images from the Maps API.
    // It abstracts away the Retrofit implementation and provides a clean interface for the ViewModel layer.
    override val staticMapRepository : StaticMapRepository by lazy {
        OfflineStaticMapRepository(staticMapRetrofitService)
    }

    override val googleMapRepository: GoogleMapRepository by lazy {
        OnlineGoogleMapRepository(
            context = context,
            propertyRepository = propertyRepository,
            poiRepository = poiRepository
        )
    }

    override val authRepository: AuthRepository by lazy {
        OnlineAuthRepository()
    }
}