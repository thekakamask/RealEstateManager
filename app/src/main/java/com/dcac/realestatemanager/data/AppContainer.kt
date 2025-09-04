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
import com.dcac.realestatemanager.data.onlineDatabase.photo.FirebasePhotoOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.poi.FirebasePoiOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.property.FirebasePropertyOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross.FirebasePropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.user.FirebaseUserOnlineRepository
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.sync.DownloadManager
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadManager
import com.dcac.realestatemanager.data.sync.UploadManager
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadManager
import com.dcac.realestatemanager.data.sync.user.UserUploadManager
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.data.userConnection.OnlineAuthRepository
import com.dcac.realestatemanager.network.StaticMapApiService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainerProvider {
    val container: AppContainer
}

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
    val userOnlineRepository: UserOnlineRepository
    val photoOnlineRepository: PhotoOnlineRepository
    val poiOnlineRepository: PoiOnlineRepository
    val propertyPoiCrossOnlineRepository : PropertyPoiCrossOnlineRepository
    val propertyOnlineRepository: PropertyOnlineRepository
    val userUploadManager: UserUploadManager
    val photoUploadManager : PhotoUploadManager
    val poiUploadManager : PoiUploadManager
    val crossSyncManager : PropertyPoiCrossUploadManager
    val propertyUploadManager: PropertyUploadManager
    val uploadManager: UploadManager
    val downloadManager: DownloadManager
    val userDownloadManager: UserDownloadManager
    val photoDownloadManager: PhotoDownloadManager
    val poiDownloadManager: PoiDownloadManager
    val propertyDownloadManager: PropertyDownloadManager
    val propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadManager
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
            userRepository,
            poiRepository,
            photoRepository,
            propertyPoiCrossRepository,
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
        OfflinePoiRepository(
            RealEstateManagerDatabase.getDatabase(context).poiDao(),
            userRepository)
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

    override val userOnlineRepository: UserOnlineRepository by lazy {
        FirebaseUserOnlineRepository(FirebaseFirestore.getInstance())
    }

    override val userUploadManager: UserUploadManager by lazy {
        UserUploadManager(
            userRepository = userRepository,
            userOnlineRepository = userOnlineRepository
        )
    }

    override val photoOnlineRepository: PhotoOnlineRepository by lazy {
        FirebasePhotoOnlineRepository(
            firestore = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance()
        )
    }

    override val photoUploadManager: PhotoUploadManager by lazy {
        PhotoUploadManager(
            photoRepository = photoRepository,
            photoOnlineRepository = photoOnlineRepository
        )
    }

    override val poiOnlineRepository: PoiOnlineRepository by lazy {
        FirebasePoiOnlineRepository(FirebaseFirestore.getInstance())
    }

    override val poiUploadManager: PoiUploadManager by lazy {
        PoiUploadManager(
            poiRepository = poiRepository,
            poiOnlineRepository = poiOnlineRepository
        )
    }

    override val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository by lazy {
        FirebasePropertyPoiCrossOnlineRepository(FirebaseFirestore.getInstance())
    }

    override val crossSyncManager: PropertyPoiCrossUploadManager by lazy {
        PropertyPoiCrossUploadManager(
            propertyPoiCrossRepository = propertyPoiCrossRepository,
            propertyPoiCrossOnlineRepository = propertyPoiCrossOnlineRepository
        )
    }

    override val propertyOnlineRepository: PropertyOnlineRepository by lazy {
        FirebasePropertyOnlineRepository(FirebaseFirestore.getInstance())
    }

    override val propertyUploadManager: PropertyUploadManager by lazy {
        PropertyUploadManager(
            propertyRepository = propertyRepository,
            propertyOnlineRepository = propertyOnlineRepository
        )
    }

    override val uploadManager: UploadManager by lazy {
        UploadManager(
            userUploadManager = userUploadManager,
            photoUploadManager = photoUploadManager,
            poiUploadManager = poiUploadManager,
            crossSyncManager = crossSyncManager,
            propertyUploadManager = propertyUploadManager
        )
    }

    override val userDownloadManager: UserDownloadManager by lazy {
        UserDownloadManager(
            userRepository = userRepository,
            userOnlineRepository = userOnlineRepository
        )
    }

    override val photoDownloadManager: PhotoDownloadManager by lazy {
        PhotoDownloadManager(
            photoRepository = photoRepository,
            photoOnlineRepository = photoOnlineRepository
        )
    }

    override val poiDownloadManager: PoiDownloadManager by lazy {
        PoiDownloadManager(
            poiRepository = poiRepository,
            poiOnlineRepository = poiOnlineRepository
        )
    }

    override val propertyDownloadManager: PropertyDownloadManager by lazy {
        PropertyDownloadManager(
            propertyRepository = propertyRepository,
            propertyOnlineRepository = propertyOnlineRepository
        )
    }

    override val propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadManager by lazy {
        PropertyPoiCrossDownloadManager(
            propertyPoiCrossRepository = propertyPoiCrossRepository,
            propertyPoiCrossOnlineRepository = propertyPoiCrossOnlineRepository
        )
    }

    override val downloadManager: DownloadManager by lazy {
        DownloadManager(
            userDownloadManager = userDownloadManager,
            photoDownloadManager = photoDownloadManager,
            propertyDownloadManager = propertyDownloadManager,
            poiDownloadManager = poiDownloadManager,
            propertyPoiCrossDownloadManager = propertyPoiCrossDownloadManager,
            userRepository = userRepository
        )
    }
}