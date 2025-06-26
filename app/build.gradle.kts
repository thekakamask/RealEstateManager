// Apply plugins using aliases defined in the version catalog (libs.versions.toml)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Android-specific build settings for the app module
// Includes SDK versioning, build types, and default configuration
android {
    namespace = "com.openclassrooms.realestatemanager" // Kotlin package namespace for generated code
    compileSdk = 34 // Android SDK version to compile against

    // Configuration that applies to all build variants (debug/release)
    defaultConfig {
        applicationId = "com.openclassrooms.realestatemanager" // Unique app identifier
        minSdk = 21  // Minimum Android version supported
        targetSdk = 34  // Targeted Android version for compatibility testing
        versionCode = 1 // Internal version number
        versionName = "1.0" // User-visible version string

        // Runner used for instrumentation tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // Enable Jetpack Compose build support
    buildFeatures {
        compose = true
    }
}

dependencies {
    // App dependencies resolved via Version Catalog aliases
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    // Unit testing framework
    testImplementation(libs.junit)
    // Android instrumentation testing libraries
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
