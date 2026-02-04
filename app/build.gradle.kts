import java.io.FileInputStream
import java.util.Properties

// Apply plugins using aliases defined in the version catalog (libs.versions.toml)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt.gradle)
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
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

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


    // ✅ JVM unit test configuration
    // By default, unit tests do not have access to Android resources (layouts, drawables, strings.xml, etc.).
    // With “isIncludeAndroidResources = true,” we allow access to APK resources
    // so that Android classes (e.g., android.net.Uri, Resources, R.string...) work
    // even in “pure JVM” unit tests (without instrumentation).

    // ⚠️ The “testOptions” API is marked @Incubating by the AGP team.
    // This means that its DSL is not yet considered stable
    // and may change in future versions of the Android Gradle plugin.
    // 👉 It's fine to use it in production: it's the only official way
    // to configure test options (here: including Android resources
    // in JVM unit tests).
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
    //For tablet responsive UI
    implementation(libs.androidx.material3.window.size.class1)
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
    ksp(libs.hilt.compiler)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

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
    //provides backport support for Java 8’s java.time API on Android devices with API level < 26
    implementation(libs.threetenabp)

    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)

    // Allows you to use CoroutineWorker, WorkerParameters, and deferred task management.
    implementation(libs.work.runtime.ktx)

    implementation(libs.coil.compose)

    implementation(libs.maps.compose)
    implementation(libs.play.services.maps.v1820)

    // Unit testing framework
    testImplementation(libs.junit)
    // Testing
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    // Android instrumentation testing libraries
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.kotlinx.coroutines.test)
    // For mocking Firebase Storage await() extensions + tasks
    testImplementation (libs.firebase.storage.ktx)
    testImplementation (libs.kotlinx.coroutines.play.services)

}

// 🔧 Display println() into unitary tests
tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
        events("passed", "failed", "skipped", "standardOut", "standardError")
    }
}