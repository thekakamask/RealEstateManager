# 🏠 **RealEstateManager**
**RealEstateManager** is a modern Android application aimed at helping real estate agents manage exceptional property listings from their mobile device. Built to support a digital transformation for a prestigious New York agency, the app allows agents to browse, add, edit, and geolocate real estate properties even in offline mode.
This project is developed using modern Android architecture principles, with a focus on local data persistence, modular code, and responsive design. It evolves from a legacy student prototype and will be gradually refactored and extended.


## ✅ **Project Update History**
This file documents key technical updates applied to the RealEstateManager Android application. It serves as a detailed changelog for traceability and developer onboarding.


## ✅ **MAJOR UPDATES**

### 🔹 **Update #1**

- 🛠️ Imported the original student prototype into Android Studio.
- 📁 GitHub repository initialized and structured.
- 📝 Basic README scaffolded with purpose, tech stack, and instructions.
- ✅ Utils.java preserved for currency conversion utility (mandatory requirement).


### 🔹 **Update #2**

- ⚙️ Upgraded Gradle Wrapper to 8.6.
- 🔧 Updated Android Gradle Plugin to 8.4.0.
- ✨ Integrated Kotlin Gradle Plugin 1.9.22 for future Kotlin migration and modern tooling.
- 🧱 Project build system modernized with no sync issues.
- 📦 Installed Android SDK Platform 34 and Build Tools 34.0.0 via SDK Manager.
- 📜 Automatically accepted licenses to ensure CI/CD compatibility.


### 🔹 **Update #3**

- 🧩 **Manifest Fixes** for Android 12+ compliance:
  - Removed obsolete package="..." attribute from AndroidManifest.xml.
  - Added android:exported="true" to MainActivity to support explicit intent filters.

- 🧱 **XML Refactoring**:
  - Migrated ConstraintLayout from legacy android.support.constraint.ConstraintLayout to androidx.constraintlayout.widget.ConstraintLayout.
  - Updated layout namespace references accordingly.

- 🧠 **Runtime Crash Fix - NPE**:
  - Fixed NullPointerException due to wrong TextView ID (R.id.activity_second_activity_text_view_main) causing null view reference.
  - Corrected with proper ID: R.id.activity_main_activity_text_view_main.

- 🔁 **Legacy Import Replacement**:
  - android.support.v7.app.AppCompatActivity ➝ androidx.appcompat.app.AppCompatActivity (in both MainActivity and SecondActivity).
  - Compliant with modern AndroidX libraries.

- ✅ **TextView Bug**:
  - Replaced textView.setText(quantity) with textView.setText(String.valueOf(quantity)) to prevent setText(int) from resolving as string resource ID.


### 🔹 **Update #4**

- 🧪 **Build system migration to Kotlin DSL**:
  - ✅ Renamed build.gradle → build.gradle.kts (project-level).
  - ✅ Renamed settings.gradle → settings.gradle.kts.
  - ✅ Renamed app/build.gradle → app/build.gradle.kts.

- 📦 **Kotlin DSL Integration**:
  - 🔧 Adapted syntax for plugin declarations using Kotlin DSL (plugins {} vs apply plugin).
  - ✅ Applied correct scoping for android {} and dependencies {} blocks.
  - 🧹 Cleaned up legacy Groovy syntax to eliminate warnings.
  - 💡 Type-safe configuration and better IDE support (auto-completion).
  - 📘 Consistent with modern Android/Kotlin best practices.

- 📚 **Version Catalogs (libs.versions.toml)**:
  - ✅ Defined all library and plugin versions in a TOML file for centralized and declarative dependency management.
  - 🔍 Eliminated hardcoded versions across build.gradle.kts files.
  - 🧩 Used aliases (e.g., libs.androidx.appcompat) to reference dependencies for improved maintainability.
  - ⚠️ Fixed configuration error caused by multiple from(...) calls ensured only one is present.
  - 📌 Compatible with Gradle dependencyResolutionManagement and modern best practices.

- 🚀 **Java 11 & Compose build compatibility**:
  - ✅ Set compileOptions and kotlinOptions to Java 11 (sourceCompatibility + jvmTarget).
  - 🧱 Enabled buildFeatures.compose = true for Jetpack Compose support.
  - 🔧 Switched ProGuard config to use proguard-android-optimize.txt in release builds.


### 🔹 **Update #5**

- 🧱 **Project dependency foundation and setup**:
  - ✅ Integrated Jetpack Compose using the official Compose BOM and Material3 libraries.
  - ✅ Added Room (with KTX and compiler) for local database persistence and annotation processing via KSP.
  - ✅ Included DataStore Preferences for key-value data storage (modern replacement of SharedPreferences).
  - ✅ Integrated Location Services and Google Maps SDK for geolocation and static/dynamic map rendering.
  - ✅ Configured KSP (Kotlin Symbol Processing) for Room code generation compatibility with Kotlin.
  - ✅ Added full testing stack with MockK and Truth to complement JUnit and Espresso for unit/integration testing.
  - 📦 All major libraries have been updated to their latest stable versions.

- 🎨 **UI Theming & Fonts**:
  - ✅ Added application logo as a vector asset to align with branding needs and scalable UI rendering.
  - ✅ Created a fully customized Material 3 theme using lightColorScheme() and darkColorScheme() for modern UI appearance.
  - ✅ Defined a color palette including primary, secondary, and background colors to support both light and dark modes.
  - ✅ Integrated Google Fonts via androidx.compose.ui:ui-text-google-fonts, enabling dynamic loading and usage of high-quality typography.
  - 🔤 Applied a clear typographic hierarchy:
    - DM Serif Display for titles, headlines, and large labels to reinforce elegance and impact (ideal for real estate).
    - Inter for body text and UI labels to ensure legibility and a modern feel.
  - 💡 Used Typography() and FontFamily() APIs to inject the font styles into the app MaterialTheme.
  - 🧪 Confirmed Compose preview rendering works with theming and font integration.


### 🔹 **Update #6**

  - 🧹 **Java-to-Kotlin migration & project restructuring**:
    - 🔄 Fully removed legacy Java classes including MainActivity.java, in favor of idiomatic Kotlin components.
    - 🧱 Reorganized source folders to follow MVVM architecture: data, model, ui, utils, workers.
    - 🔧 Updated AndroidManifest.xml with proper Application class and Compose theme reference.
  
  - 🛠️ **Refactored Utils.java to Kotlin**:
    - ✅ Migrated to Utils.kt as an object, following Kotlin best practices.
    - ✅ Preserved the three original methods (convertDollarToEuro, getTodayDate, isInternetAvailable) with improved implementations:
      - ✅ Internet check now supports API 21+ via version-aware logic.
      - ✅ Currency conversion and date formatting are simplified and localized.
    - 📲 Updated method logic to use ConnectivityManager with NetworkCapabilities or NetworkInfo fallback.
    - 🛡️ Removed any direct calls to APIs not available on Android 5.0.


### 🔹 **Update #7**

  - 🧱 **Define core data models and Room entities for real estate properties, photos, and POIs.**

  - 🏠 **PropertyEntity & DAO** : 
    - Defined the PropertyEntity class to represent real estate listings in the Room database. This model includes all key attributes expected from a property:
      - Title, type (e.g. loft, house), price in dollars, surface area, number of rooms, and detailed description.
      - Address information, agent name, listing status (sold/available), entry and optional sale date.
    
    - Also implemented PropertyDao interface to handle database access:
      - Retrieve all properties, sorted either by entry date (descending) or alphabetically by title.
      - Fetch a property by ID.
      - Insert new listings or update existing ones.
      - Mark properties as sold with a timestamp, or delete them individually.
      - A method is also included to clear the database (for development/testing).
      - Introduced a multi-criteria search method using SQL conditions with optional filters (Surface area, price range, type of property, and sold status).
  
  - 🖼️ **PhotoEntity & DAO** :
    - Created the PhotoEntity class to store photos linked to a specific property. Each photo includes:
      - A reference to the associated property (via propertyId).
      - A URI to the image stored locally or remotely.
      - A textual description (e.g. "Living room", "Balcony view").

    - Developed the PhotoDao interface to manage photo data access:
      - Fetch all photos related to a given property by its ID.
      - Insert multiple photos at once (typically during property creation or editing).
      - Remove photos individually or all photos linked to a specific property (e.g. on deletion).

  - 📍 **PoiEntity & DAO** :
    - Implemented the PoiEntity data class to represent nearby points of interest (POIs) linked to a property. Each POI record includes:
      - A reference to the associated property via propertyId.
      - A name (e.g. "Central Park", "Harlem Public School").
      - A type or category (e.g. "Park", "School", "Store").

    - Built the PoiDao interface to manage interaction with POIs:
      - Fetch all POIs related to a given property using its ID.
      - Insert a list of POIs in bulk during property creation or update.
      - Delete all POIs tied to a specific property (e.g. upon property removal).
      - Allow selective deletion of individual POIs if needed (manual clean-up or updates).


### 🔹 **Update #8**

  - 🗃️ **Set up Room database and local data infrastructure.**

    - 🏗️ **RealEstateManagerDatabase**:
      - Created the RoomDatabase implementation that ties together the core Room entities: PropertyEntity, PhotoEntity, and PoiEntity.
      - Defined abstract DAO accessors: propertyDao(), photoDao(), poiDao().
      - Implemented a singleton pattern using @Volatile and synchronized block to ensure thread-safe access to a single database instance.
      - Chose not to use fallbackToDestructiveMigration() to preserve data integrity during future schema updates.
      
    - 🧩 **Repository Layer**:
      - Defined repository interfaces (PropertyRepository, PhotoRepository, PoiRepository) to abstract data access operations.
      - Implemented offline versions (OfflinePropertyRepository, OfflinePhotoRepository, OfflinePoiRepository) using Room DAOs under the hood.
      - This separation enables easier mocking/testing and supports future migration to remote or hybrid data sources.
    
    - 💼 **AppContainer & Dependency Injection**:
      - Introduced the AppContainer interface as a centralized dependency provider.
      - AppDataContainer holds lazy-initialized repository instances, each backed by the RealEstateManagerDatabase.
      - Ensures loose coupling and clean DI (Dependency Injection) throughout the app.

    - 🚀 **Application Initialization**:
      - Created RealEstateManagerApplication which initializes the AppContainer in onCreate().
      - This allows dependency access from any part of the app using context.applicationContext.


### 🔹 **Update #9**

  - 🔧 **Migrated base package name from com.openclassrooms.realestatemanager to com.dcac.realestatemanager.**
    - Used Android Studio's Refactor feature to rename the openclassrooms part of the package to dcac.
    - Updated the package declaration in all .kt source files accordingly.
    - Modified the AndroidManifest.xml to reflect the new package name

  - 🌐 **Integrated Retrofit & Kotlinx Serialization for API access**
    - Added the following libraries to support network requests and JSON parsing:
      - retrofit2 – Core networking library for HTTP API calls.
      - retrofit2-kotlinx-serialization-converter – Allows Retrofit to use kotlinx.serialization for JSON parsing.
      - okhttp3 – HTTP client used by Retrofit.
      - kotlinx-serialization-json – Official Kotlin library for JSON serialization/deserialization.
    - Configured plugin block with:
      - kotlin-serialization – Enables Kotlin’s serialization compiler plugin for handling JSON models.
    - These libraries will support integration with the Google Static Maps API to display static, offline-compatible property maps.

  - 🗺️ **Google Static Maps API Integration**
    - Defined a Retrofit interface targeting the Google Static Maps API /staticmap endpoint.
    - Created a dedicated data class to encapsulate all map request parameters like center, zoom level, size, map type, and marker definitions.
    - Implemented a centralized OfflineStaticMapRepository that orchestrates API calls, handles error logging, and manages image retrieval.
    - The repository abstracts the networking logic, exposing a clean method to get static maps as raw image byte arrays for use in the UI or for offline storage.

  - 💾 **Offline Caching Support for Map Images**
    - Extended the OfflineStaticMapRepository to persist downloaded map images to internal storage.
    - A local save method creates a subfolder in context.filesDir/maps/ to store images with custom filenames.
    - Image file paths are then persisted in the Room database and reused when displaying maps offline, avoiding unnecessary network calls.

  - 🏠 **Room Model Enhancement**
    - Extended the PropertyEntity model to include a new column for storing the path to the static map file.
    - This association allows each property to persist a visual context of its surroundings, improving user experience offline.

  - 🔐 **API Key Management via BuildConfig**
    - Moved the Google Maps Static API key out of source code and into a secure Gradle configuration (local.properties).
    - The key is injected into the app at build time using buildConfigField, allowing safe usage through BuildConfig.
    - This approach avoids exposing sensitive credentials in version control systems.


### 🔹 **Update #10**

  - 📍 **Location Permission Handling**
    - Implemented runtime permission request flow for accessing the user fine location (ACCESS_FINE_LOCATION).
    - Updated AndroidManifest.xml to declare required location permissions (ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION).
    - Added user-friendly permission rationale and error handling to ensure smooth experience when permissions are denied or revoked.
    - This setup prepares the app for integrating live user geolocation features and map display with location-based markers.
  
  - 📦 **Google Play Services & Coroutines Integration**
    - Added dependencies for Google Maps SDK, Location Services, and Kotlin Coroutines support for asynchronous location retrieval.
    - Configured Kotlin Coroutines Play Services to allow suspending functions when accessing the fused location client.

  - 🗺️ **Dynamic Location & Data Integration**
    - Implemented GoogleMapRepository that uses the fused location client to asynchronously obtain the user current location.
    - Repository streams all properties and POIs stored locally via existing Room repositories, enabling live data updates for map display.

  - 💉 **Dependency Injection Enhancements**
    - Extended AppContainer and its implementation to provide an instance of GoogleMapRepository, injecting the necessary context and offline repositories.
    - Centralized repository management to streamline future ViewModel integration and testing.


### 🔹 **Update #11**

  - 🆕 **Core Business Models Creation**
    - Introduced dedicated domain model classes Photo, Poi, and Property to clearly represent the core business entities.
    - Separated business models from database entities to ensure clean domain-driven design.
    - Simplified handling of data relationships and improved code clarity and maintainability.

  - 🔄 **Repository Refactoring for Business Models**
    - Refactored repository interfaces and implementations to expose flows of business models instead of raw database entities for all non-CRUD functions.
    - Improved UI integration by providing ready-to-use domain objects.
    - Facilitated easier mocking and testing of business logic.

  - 🛠️ **ModelUtils and Date Conversion Enhancements**
    - Added utilities for converting database entities into business models.  
    - Implemented robust date parsing and formatting using the ThreeTenABP library for LocalDate.
    - Ensured compatibility with Android API 21+ for consistent date handling.

  - 🧩 **ViewModel and UI State Scaffolding**
    - Created placeholder ViewModel interfaces and their concrete implementations. 
    - Developed sealed interface-based UI States for all main features.
    - Established a modular and testable foundation for reactive UI logic and incremental development.

  - 🔧 **Clean Architecture and Separation of Concerns**
    - Defined clear boundaries between interfaces and implementations.
    - Promoted separation of concerns to enhance scalability and maintainability.
    - Prepared the project for future growth and easier extensions.


## 🤝 **Contributions**
Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.