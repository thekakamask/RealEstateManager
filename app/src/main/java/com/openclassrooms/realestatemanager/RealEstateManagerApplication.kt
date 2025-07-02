package com.openclassrooms.realestatemanager

import android.app.Application
import com.openclassrooms.realestatemanager.data.AppContainer
import com.openclassrooms.realestatemanager.data.AppDataContainer

//Custom Application class for the RealEstateManager app.
// Acts as the global entry point for initializing application-wide dependencies.
// Used to hold a singleton instance of the AppContainer (dependency container).
class RealEstateManagerApplication : Application() {

    // Declares the AppContainer instance that will provide access to repositories.
    // `lateinit` means this variable will be initialized *later* (in onCreate),and NOT at the time of declaration.
    //  This avoids needing to make it nullable, and tells the compiler:  “trust me, I’ll initialize it before using it.”
    //  If accessed before being initialized, a runtime exception will occur.
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Initialize the dependency container (repositories) once for the entire app lifecycle.
        container = AppDataContainer(this)

    }
}