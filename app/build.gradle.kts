import java.io.FileInputStream
import java.util.Properties

// Apply plugins using aliases defined in the version catalog (libs.versions.toml)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Load the mapsApiKe from the local.properties file (not committed to VCS)
// This approach ensures the API key stays secure and out of version control
val localProperties = Properties().apply {
    load(FileInputStream(File(rootDir, "local.properties")))
}
val mapsApiKey = localProperties.getProperty("MAPS_API_KEY")
    ?: throw GradleException("MAPS_API_KEY not found in local.properties")

// Android-specific build settings for the app module
// Includes SDK versioning, build types, and default configuration
android {
    namespace = "com.dcac.realestatemanager" // Kotlin package namespace for generated code
    compileSdk = 35 // Android SDK version to compile against

    // Configuration that applies to all build variants (debug/release)
    defaultConfig {
        applicationId = "com.dcac.realestatemanager" // Unique app identifier
        minSdk = 21  // Minimum Android version supported
        targetSdk = 35  // Targeted Android version for compatibility testing
        versionCode = 1 // Internal version number
        versionName = "1.0" // User-visible version string

        // Runner used for instrumentation tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject the map API key into BuildConfig
        // This allows referencing BuildConfig.MAPS_API_KEY directly in the code
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    // Release build configuration
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Set Java source and target compatibility to Java 11
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Set JVM target for Kotlin to Java 11
    kotlinOptions {
        jvmTarget = "11"
    }

    // Enable Jetpack Compose support and BuildConfig generation
    // 'buildConfig = true' is required when using buildConfigField in defaultConfig
    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {
    // App dependencies resolved via Version Catalog aliases
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    // Jetpack Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    //Makes it easy to integrate Google Fonts directly into Jetpack Compose.
    implementation(libs.androidx.ui.text.google.fonts)
    //Provides additional Material Design icons for use in the user interface.
    implementation(libs.androidx.material.icons.extended)
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    // DataStore
    implementation(libs.datastore.preferences)
    // Location & Maps
    implementation(libs.location.services)
    implementation(libs.maps)
    // Coroutine support for Google Play Services APIs (e.g., FusedLocationProviderClient.await()).
    implementation(libs.kotlinx.coroutines.play.services)
    // Unit testing framework
    testImplementation(libs.junit)
    // Testing
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    // Android instrumentation testing libraries
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
