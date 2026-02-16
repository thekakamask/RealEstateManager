package com.dcac.realestatemanager

import android.app.Application
import com.dcac.realestatemanager.dI.AppContainer
import com.dcac.realestatemanager.dI.AppContainerProvider
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.jakewharton.threetenabp.AndroidThreeTen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

//Custom Application class for the RealEstateManager app.
// Acts as the global entry point for initializing application-wide dependencies.

//“This application uses Hilt. It will create the dependency graph on startup.”
// Hilt will scan the code for modules (@Module) to figure out how to build the objects.
@HiltAndroidApp
class RealEstateManagerApplication : Application(), AppContainerProvider {

    //  Hilt will automatically inject the container
    // thanks to provideAppContainer from the AppModule
    @Inject
    lateinit var injectedHiltContainer: AppContainer

    // ✅ redefine container from AppDataProvider to provide injectedHiltContainer
    // (from provideAppContainer of AppModule) to Hilt incompatible class
    override val container: AppContainer
        get() = injectedHiltContainer

    @Inject lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        // Global init
        AndroidThreeTen.init(this)
        FirebaseApp.initializeApp(this)
        if (FirebaseAuth.getInstance().currentUser != null) {
            syncScheduler.scheduleSync()
        }

    }
}