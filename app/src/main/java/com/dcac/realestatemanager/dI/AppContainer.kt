package com.dcac.realestatemanager.dI

import android.app.Application
import androidx.room.Room
import com.dcac.realestatemanager.data.firebaseDatabase.photo.FirebasePhotoOnlineRepository
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import com.dcac.realestatemanager.data.googleMap.OnlineGoogleMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.OfflineStaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
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
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.poi.FirebasePoiOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.FirebasePropertyOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.FirebasePropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.FirebaseStaticMapOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.FirebaseUserOnlineRepository
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.RealEstateManagerDatabase
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoDao
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiDao
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyDao
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossDao
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapDao
import com.dcac.realestatemanager.data.offlineDatabase.user.UserDao
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.sync.globalManager.DownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.globalManager.DownloadManager
import com.dcac.realestatemanager.data.sync.globalManager.UploadInterfaceManager
import com.dcac.realestatemanager.data.sync.globalManager.UploadManager
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.photo.PhotoDownloadManager
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.photo.PhotoUploadManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiDownloadManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.poi.PoiUploadManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyDownloadManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.property.PropertyUploadManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossDownloadManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.propertyPoiCross.PropertyPoiCrossUploadManager
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapDownloadManager
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.staticMap.StaticMapUploadManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserDownloadManager
import com.dcac.realestatemanager.data.sync.user.UserUploadInterfaceManager
import com.dcac.realestatemanager.data.sync.user.UserUploadManager
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.data.userConnection.OnlineAuthRepository
import com.dcac.realestatemanager.network.StaticMapApiService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

// Give AppContainer to non Hilt Class (ex worker)
interface AppContainerProvider {
    //Container for
    val container: AppContainer
}

// Define dependencies expose to all layer
interface AppContainer {
    val propertyRepository: PropertyRepository
    val photoRepository: PhotoRepository
    val poiRepository: PoiRepository
    val propertyPoiCrossRepository: PropertyPoiCrossRepository
    val userRepository: UserRepository
    val staticMapRepository: StaticMapRepository
    val googleMapRepository: GoogleMapRepository
    val authRepository: AuthRepository
    val userOnlineRepository: UserOnlineRepository
    val photoOnlineRepository: PhotoOnlineRepository
    val poiOnlineRepository: PoiOnlineRepository
    val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository
    val propertyOnlineRepository: PropertyOnlineRepository

    val staticMapOnlineRepository: StaticMapOnlineRepository

    val uploadManager: UploadInterfaceManager
    val downloadManager: DownloadInterfaceManager

    val userDownloadManager: UserDownloadInterfaceManager
    val photoDownloadManager: PhotoDownloadInterfaceManager
    val poiDownloadManager: PoiDownloadInterfaceManager
    val propertyDownloadManager: PropertyDownloadInterfaceManager
    val propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadInterfaceManager

    val staticMapDownloadManager: StaticMapDownloadInterfaceManager
    val userUploadManager: UserUploadInterfaceManager
    val photoUploadManager: PhotoUploadInterfaceManager
    val poiUploadManager: PoiUploadInterfaceManager
    val crossSyncManager: PropertyPoiCrossUploadInterfaceManager
    val propertyUploadManager: PropertyUploadInterfaceManager
    val staticMapUploadManager: StaticMapUploadInterfaceManager
    val syncScheduler: SyncScheduler
}

// Manual version for non hilt layers
class AppDataContainer(
    override val propertyRepository: PropertyRepository,
    override val photoRepository: PhotoRepository,
    override val poiRepository: PoiRepository,
    override val propertyPoiCrossRepository: PropertyPoiCrossRepository,
    override val userRepository: UserRepository,
    override val staticMapRepository: StaticMapRepository,
    override val googleMapRepository: GoogleMapRepository,
    override val authRepository: AuthRepository,
    override val userOnlineRepository: UserOnlineRepository,
    override val photoOnlineRepository: PhotoOnlineRepository,
    override val poiOnlineRepository: PoiOnlineRepository,
    override val propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository,
    override val propertyOnlineRepository: PropertyOnlineRepository,
    override val staticMapOnlineRepository: StaticMapOnlineRepository,
    override val uploadManager: UploadInterfaceManager,
    override val downloadManager: DownloadInterfaceManager,
    override val userDownloadManager: UserDownloadInterfaceManager,
    override val photoDownloadManager: PhotoDownloadInterfaceManager,
    override val poiDownloadManager: PoiDownloadInterfaceManager,
    override val propertyDownloadManager: PropertyDownloadInterfaceManager,
    override val propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadInterfaceManager,
    override val staticMapDownloadManager: StaticMapDownloadInterfaceManager,
    override val userUploadManager: UserUploadInterfaceManager,
    override val photoUploadManager: PhotoUploadInterfaceManager,
    override val poiUploadManager: PoiUploadInterfaceManager,
    override val crossSyncManager: PropertyPoiCrossUploadInterfaceManager,
    override val propertyUploadManager: PropertyUploadInterfaceManager,
    override val staticMapUploadManager: StaticMapUploadInterfaceManager,
    override val syncScheduler: SyncScheduler
) : AppContainer

//AppModule will tell to Hilt how to create object needed by the app.
//@Module indicate the this object contains recipes for dependence creation
//InstallIn precise that all dependence will have a global scale (only one time creation and share in all app)
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    //SAY TO HILT HOW TO CREATE APP CONTAINER INSTANCE
    @Provides
    @Singleton
    fun provideAppContainer(
        propertyRepository: PropertyRepository,
        photoRepository: PhotoRepository,
        poiRepository: PoiRepository,
        propertyPoiCrossRepository: PropertyPoiCrossRepository,
        userRepository: UserRepository,
        staticMapRepository: StaticMapRepository,
        googleMapRepository: GoogleMapRepository,
        authRepository: AuthRepository,
        userOnlineRepository: UserOnlineRepository,
        photoOnlineRepository: PhotoOnlineRepository,
        poiOnlineRepository: PoiOnlineRepository,
        propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository,
        propertyOnlineRepository: PropertyOnlineRepository,
        staticMapOnlineRepository: StaticMapOnlineRepository,
        uploadManager: UploadInterfaceManager,
        downloadManager: DownloadInterfaceManager,
        userDownloadManager: UserDownloadInterfaceManager,
        photoDownloadManager: PhotoDownloadInterfaceManager,
        poiDownloadManager: PoiDownloadInterfaceManager,
        propertyDownloadManager: PropertyDownloadInterfaceManager,
        propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadInterfaceManager,
        staticMapDownloadManager: StaticMapDownloadInterfaceManager,
        userUploadManager: UserUploadInterfaceManager,
        photoUploadManager: PhotoUploadInterfaceManager,
        poiUploadManager: PoiUploadInterfaceManager,
        crossSyncManager: PropertyPoiCrossUploadInterfaceManager,
        propertyUploadManager: PropertyUploadInterfaceManager,
        staticMapUploadManager: StaticMapUploadInterfaceManager,
        syncScheduler: SyncScheduler

    ): AppContainer {
        return AppDataContainer(
            propertyRepository,
            photoRepository,
            poiRepository,
            propertyPoiCrossRepository,
            userRepository,
            staticMapRepository,
            googleMapRepository,
            authRepository,
            userOnlineRepository,
            photoOnlineRepository,
            poiOnlineRepository,
            propertyPoiCrossOnlineRepository,
            propertyOnlineRepository,
            staticMapOnlineRepository,
            uploadManager,
            downloadManager,
            userDownloadManager,
            photoDownloadManager,
            poiDownloadManager,
            propertyDownloadManager,
            propertyPoiCrossDownloadManager,
            staticMapDownloadManager,
            userUploadManager,
            photoUploadManager,
            poiUploadManager,
            crossSyncManager,
            propertyUploadManager,
            staticMapUploadManager,
            syncScheduler
        )
    }

    @Provides
    @Singleton
    fun provideDatabase(application: Application): RealEstateManagerDatabase {
        return Room.databaseBuilder(
            application,
            RealEstateManagerDatabase::class.java,
            "real_estate_manager_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(db: RealEstateManagerDatabase) = db.userDao()

    @Provides
    fun providePhotoDao(db: RealEstateManagerDatabase) = db.photoDao()

    @Provides
    fun providePoiDao(db: RealEstateManagerDatabase) = db.poiDao()

    @Provides
    fun providePropertyDao(db: RealEstateManagerDatabase) = db.propertyDao()

    @Provides
    fun providePropertyCrossDao(db: RealEstateManagerDatabase) = db.propertyCrossDao()

    @Provides
    fun provideStaticMapDao(db: RealEstateManagerDatabase) = db.staticMapDao()

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository =
        OfflineUserRepository(userDao)

    @Provides
    @Singleton
    fun providePhotoRepository(photoDao: PhotoDao): PhotoRepository =
        OfflinePhotoRepository(photoDao)

    @Provides
    @Singleton
    fun providePoiRepository(poiDao: PoiDao): PoiRepository =
        OfflinePoiRepository(poiDao)

    @Provides
    @Singleton
    fun providePropertyCrossRepository(crossDao: PropertyPoiCrossDao): PropertyPoiCrossRepository =
        OfflinePropertyPoiCrossRepository(crossDao)

    @Provides
    @Singleton
    fun providePropertyRepository(
        propertyDao: PropertyDao,
        userRepository: UserRepository,
        poiRepository: PoiRepository,
        photoRepository: PhotoRepository,
        propertyPoiCrossRepository: PropertyPoiCrossRepository
    ): PropertyRepository =
        OfflinePropertyRepository(
            propertyDao,
            userRepository,
            poiRepository,
            photoRepository,
            propertyPoiCrossRepository
        )

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideStaticMapApiService(retrofit: Retrofit): StaticMapApiService =
        retrofit.create(StaticMapApiService::class.java)

    @Provides
    @Singleton
    fun provideStaticMapRepository(
        api: StaticMapApiService,
        staticMapDao: StaticMapDao
    ): StaticMapRepository = OfflineStaticMapRepository(api, staticMapDao)

    @Provides
    @Singleton
    fun provideGoogleMapRepository(
        context: Application,
        propertyRepository: PropertyRepository,
        poiRepository: PoiRepository
    ): GoogleMapRepository =
        OnlineGoogleMapRepository(
            context = context,
            propertyRepository = propertyRepository,
            poiRepository = poiRepository
        )

    // --- Repositories Online ---


    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = OnlineAuthRepository()


    @Provides
    @Singleton
    fun provideUserOnlineRepository(firestore: FirebaseFirestore): UserOnlineRepository =
        FirebaseUserOnlineRepository(firestore)

    @Provides
    @Singleton
    fun providePhotoOnlineRepository(firestore: FirebaseFirestore, storage: FirebaseStorage): PhotoOnlineRepository =
        FirebasePhotoOnlineRepository(firestore, storage)

    @Provides
    @Singleton
    fun providePoiOnlineRepository(firestore: FirebaseFirestore): PoiOnlineRepository =
        FirebasePoiOnlineRepository(firestore)

    @Provides
    @Singleton
    fun providePropertyOnlineRepository(firestore: FirebaseFirestore): PropertyOnlineRepository =
        FirebasePropertyOnlineRepository(firestore)

    @Provides
    @Singleton
    fun providePropertyPoiCrossOnlineRepository(firestore: FirebaseFirestore): PropertyPoiCrossOnlineRepository =
        FirebasePropertyPoiCrossOnlineRepository(firestore)

    @Provides
    @Singleton
    fun provideStaticMapOnlineRepository(firestore: FirebaseFirestore, storage: FirebaseStorage): StaticMapOnlineRepository =
        FirebaseStaticMapOnlineRepository(firestore, storage)


    // --- Upload Managers ---

    @Provides
    @Singleton
    fun provideUserUploadManager(
        userRepository: UserRepository,
        userOnlineRepository: UserOnlineRepository
    ): UserUploadInterfaceManager = UserUploadManager(userRepository, userOnlineRepository)

    @Provides
    @Singleton
    fun providePhotoUploadManager(
        photoRepository: PhotoRepository,
        photoOnlineRepository: PhotoOnlineRepository
    ): PhotoUploadInterfaceManager = PhotoUploadManager(photoRepository, photoOnlineRepository)

    @Provides
    @Singleton
    fun providePoiUploadManager(
        poiRepository: PoiRepository,
        poiOnlineRepository: PoiOnlineRepository
    ): PoiUploadInterfaceManager = PoiUploadManager(poiRepository, poiOnlineRepository)

    @Provides
    @Singleton
    fun providePropertyUploadManager(
        propertyRepository: PropertyRepository,
        propertyOnlineRepository: PropertyOnlineRepository
    ): PropertyUploadInterfaceManager = PropertyUploadManager(propertyRepository, propertyOnlineRepository)

    @Provides
    @Singleton
    fun provideCrossSyncManager(
        propertyPoiCrossRepository: PropertyPoiCrossRepository,
        propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository
    ): PropertyPoiCrossUploadInterfaceManager = PropertyPoiCrossUploadManager(propertyPoiCrossRepository, propertyPoiCrossOnlineRepository)

    @Provides
    @Singleton
    fun provideStaticMapUploadManager(
        staticMapRepository: StaticMapRepository,
        staticMapOnlineRepository: StaticMapOnlineRepository
    ): StaticMapUploadInterfaceManager =
        StaticMapUploadManager(staticMapRepository, staticMapOnlineRepository)

    @Provides
    @Singleton
    fun provideUploadManager(
        userUploadManager: UserUploadInterfaceManager,
        photoUploadManager: PhotoUploadInterfaceManager,
        poiUploadManager: PoiUploadInterfaceManager,
        crossSyncManager: PropertyPoiCrossUploadInterfaceManager,
        propertyUploadManager: PropertyUploadInterfaceManager,
        staticMapUploadManager: StaticMapUploadInterfaceManager
    ): UploadInterfaceManager = UploadManager(userUploadManager, photoUploadManager, poiUploadManager, crossSyncManager, propertyUploadManager, staticMapUploadManager)


    // --- Download Managers ---

    @Provides
    @Singleton
    fun provideUserDownloadManager(
        userRepository: UserRepository,
        userOnlineRepository: UserOnlineRepository
    ): UserDownloadInterfaceManager = UserDownloadManager(userRepository, userOnlineRepository)

    @Provides
    @Singleton
    fun providePhotoDownloadManager(
        photoRepository: PhotoRepository,
        photoOnlineRepository: PhotoOnlineRepository
    ): PhotoDownloadInterfaceManager = PhotoDownloadManager(photoRepository, photoOnlineRepository)

    @Provides
    @Singleton
    fun providePoiDownloadManager(
        poiRepository: PoiRepository,
        poiOnlineRepository: PoiOnlineRepository
    ): PoiDownloadInterfaceManager = PoiDownloadManager(poiRepository, poiOnlineRepository)

    @Provides
    @Singleton
    fun providePropertyDownloadManager(
        propertyRepository: PropertyRepository,
        propertyOnlineRepository: PropertyOnlineRepository
    ): PropertyDownloadInterfaceManager = PropertyDownloadManager(propertyRepository, propertyOnlineRepository)

    @Provides
    @Singleton
    fun providePropertyPoiCrossDownloadManager(
        propertyPoiCrossRepository: PropertyPoiCrossRepository,
        propertyPoiCrossOnlineRepository: PropertyPoiCrossOnlineRepository
    ): PropertyPoiCrossDownloadInterfaceManager = PropertyPoiCrossDownloadManager(propertyPoiCrossRepository, propertyPoiCrossOnlineRepository)

    @Provides
    @Singleton
    fun provideStaticMapDownloadManager(
        staticMapRepository: StaticMapRepository,
        staticMapOnlineRepository: StaticMapOnlineRepository
    ): StaticMapDownloadInterfaceManager =
        StaticMapDownloadManager(staticMapRepository, staticMapOnlineRepository)

    @Provides
    @Singleton
    fun provideDownloadManager(
        userDownloadManager: UserDownloadInterfaceManager,
        photoDownloadManager: PhotoDownloadInterfaceManager,
        propertyDownloadManager: PropertyDownloadInterfaceManager,
        poiDownloadManager: PoiDownloadInterfaceManager,
        propertyPoiCrossDownloadManager: PropertyPoiCrossDownloadInterfaceManager,
        staticMapDownloadManager: StaticMapDownloadInterfaceManager
    ): DownloadInterfaceManager = DownloadManager(propertyDownloadManager, photoDownloadManager, poiDownloadManager, userDownloadManager, propertyPoiCrossDownloadManager, staticMapDownloadManager )

    @Provides
    @Singleton
    fun provideSyncScheduler(application: Application): SyncScheduler {
        return SyncScheduler(application)
    }
}