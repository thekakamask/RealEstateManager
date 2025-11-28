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


### 🔹 **Update #12**

  - 🔐 **Firebase Authentication & Firestore Integration**
    - Added Firebase BoM (firebase-bom) to manage consistent Firebase library versions centrally.
    - Integrated Firebase Authentication (firebase-auth) to support secure user sign-in functionality.
    - Connected Firebase Firestore (firebase-firestore) as a scalable cloud NoSQL database to complement the local Room database.
    - Firebase Analytics (firebase-analytics) added for tracking in-app user behavior and engagement.
    - com.google.gms.google-services plugin configured via Gradle Version Catalog for automatic initialization from google-services.json`.
    - Updated build.gradle.kts and libs.versions.toml to declare all Firebase dependencies using best practices (via BOM and centralized plugin declarations).
    - Ensured secure handling of API credentials by relying on Gradle's BuildConfig, local.properties and google-services.JSON (no secrets committed to VCS).
    - This integration sets the stage for future bi-directional sync between local Room database and Firestore, enabling full offline-online data resilience.

  - 🧩 **Firebase Initialization in Application class**
    - Updated RealEstateManagerApplication to initialize Firebase using FirebaseApp.initializeApp(this).
    - Ensures Firebase services are available globally and initialized at app startup.

  - 🔐 **Authentication Abstraction Layer**
    - Introduced AuthRepository interface to define contract for user authentication logic.
    - Created OnlineAuthRepository as a Firebase-based implementation using FirebaseAuth.
    - Exposed currentUser state and observeAuthState() as a Flow of FirebaseUser to observe changes in authentication status reactively.
    - Added coroutine-based signInWithEmail() and signUpWithEmail() functions with Result wrappers for robust error handling.

  - 🛡️ **Secure API Key & Secrets Handling**
    - Placed google-services.json in the app/ module.
    - Updated .gitignore to exclude google-services.json from version control, ensuring Firebase credentials are not exposed.


### 🔹 **Update #13**

  - 🧩 **Firebase Authentification Repository Injection Setup**
    - Injected OnlineAuthRepository as AuthRepository implementation within AppContainer for centralized dependency management.
    - Ensured consistent and easy access to authentication functionality across the app via dependency injection.

  - 👥 **User Data Model and Repository Implementation**
    - Introduced UserEntity Room entity with fields for email, password, and agent name.
    - Developed UserDao with standard CRUD operations and Flow support.
    - Created UserRepository interface and OfflineUserRepository implementation encapsulating data access.
    - Added domain model User and mapping extension functions for seamless entity-model conversion.

  - 🔄 **Property-POI Many-to-Many Relationship Handling**
    - Created cross-reference entity PropertyPoiCrossEntity to manage many-to-many associations between properties and POIs.
    - Updated Room schema: removed direct foreign key in PoiEntity to property; handled relations via junction table.
    - Enhanced DAO interfaces with join queries to retrieve POIs linked to properties.
    - Added composite data class PropertyWithPois using Room @Relation with @Junction to fetch relational data efficiently.

  - 🧩 **Repository and Dependency Injection Updates**
    - Developed repository implementation managing property-poi links and user data repositories with clean architecture principles.
    - Ensured repositories provide domain models enriched with relational data to upper layers.
    - Extended DI container to provide these repositories application-wide.
    - Designed data layer with clean separation (Entity vs Domain Model) to facilitate future synchronization with remote data sources.

  - 🛠️ **Model Mappers and Utility Extensions**
    - Extended utils file to add toEntity() functions for all domain models: Property, Poi, Photo, User, and PropertyPoiCrossRef.
    - Continued to leverage ThreeTenABP for reliable LocalDate conversion between strings and domain models.
    - Facilitated future offline-online synchronization and consistent UI binding through robust mapping layers.

  - 🗃️ **Database Consistency and Optimization**
    - Applied foreign keys with ON DELETE CASCADE to ensure dependent entities are removed automatically, preventing orphan data.
    - Added indices on foreign key columns (user_id, propertyId, poiId) for improved query performance and scalability.


### 🔹 **Update #14 (correction)**

  - 🔐 **Online-Only Account Creation**
    - User accounts must now be created online using Firebase Authentication (createUserWithEmailAndPassword).
    - The password is not stored in plain text anywhere — only the hashed version (SHA-256) is retained locally for offline authentication.
    - Upon successful signup, the user's profile (email + agent name + uid) is uploaded to Firestore under the path: users/{uid}.
    - Once both Firebase Auth and Firestore steps succeed, the account is marked as isSynced = true and saved in the local Room database for offline access.

  - 💾 **Offline Authentication (Room)**
    - Introduced a secure hashing utility using SHA-256 to hash passwords before storing them in Room.
    - The app supports offline login by comparing the hash of the entered password with the one stored locally.
    - Implemented Room DAO queries to check user existence (emailExists) and retrieve users by email.
    - Enhanced the OfflineUserRepository to transparently hash passwords during cacheUserFromFirebase().

  - 🔄 **Sync-Only Logic for User Modifications**
    - Account creation is now excluded from the sync logic.
    - UserSyncManager is responsible only for syncing modified users (e.g., email/agent name changes) from Room to Firestore.
    - Users with isSynced = false are automatically detected and synchronized when the network is available.
    - The Firebase UID is stored locally to maintain reference consistency between Firestore and Room.
    - Robust error handling is in place: failed syncs do not mark the user as synced and retry later.
    - Firestore security rules enforce write access only to authenticated users modifying their own document.

  - 🧩 **Architecture & Extensibility**
    - Clear separation of concerns:
      - ✅ FirebaseAuth handles account creation and sign-in
      - ✅ Firestore stores public user profile info
      - ✅ Room caches the user account for offline usage
      - ✅ Sync system (SyncManager & UserSyncManager) pushes changes, not creation
    - The model User and entity UserEntity include isSynced and firebaseUid to track sync state and cross-reference.
    - This setup lays the groundwork for synchronizing other entities (e.g., properties, photos, POIs) using the same principles.

  - ✅ **TL;DR**  
    - User accounts are now created online-only, authenticated offline, and synchronized intelligently with Firebase when needed. This architecture ensures security, offline resilience, and cloud consistency.


### 🔹 **Update #15**

  - 🧪 **Complete Fake Data Setup**
    - Introduced comprehensive FakeEntity structure with dummy data for:
      - UserEntity, PropertyEntity, PhotoEntity, PoiEntity, PropertyPoiCrossEntity
      - Cross-relational data for PropertyWithPoiSRelation and PoiWithPropertiesRelation
    - Mirrored all data into the FakeModel domain layer for test independence.
    - Created bidirectional relation models: FakePropertyWithPoiModel, FakePoiWithPropertyModel, and FakePropertyPoiCrossModel.

  - ✏️ **Domain Model Updates**
    - Property now embeds a User object directly (instead of userId).
    - Utility methods and Room mappers adapted to support this new structure, enabling full object graph restoration from Room.
    - Dates (entryDate, saleDate) are now stored as LocalDate.

  - 🔄 **Model Conversion + Repository Updates**
    - All toModel() and toEntity() functions now handle nested conversion:
      - Full PropertyEntity → Property conversion including User, Photos, and PoiS.
      - .toFullModel(...) aggregates cross-refs, user, POIs, and photo data.
    - Repositories (OfflinePropertyRepository, OfflinePoiRepository) now inject UserRepository to resolve User dependencies during mapping.


### 🔹 **Update #16**

  - ✅ **DAO Unit Tests Added**
    - 🧪 PoiDaoTest
      - Insert single and multiple POIs
      - Delete POI and associated cross-ref
      - Retrieve all POIs
      - Test relation resolution with getPoiWithProperties()
    - 🧪 PropertyPoiCrossDaoTest (Junction Table)
      - Insert/delete single and multiple cross refs
      - Get POI IDs by Property and vice versa
      - Clean-up test with clearAllCrossRefs()
      - Test relational queries and mapping logic
    - 🧪 PropertyDaoTest
      - Insert, update, delete, and mark property as sold
      - Filtered search with surface, price, type, and sale status
      - Sorted fetch by date and alphabetic order
      - getPropertyWithPoiS() tests complete relation resolution
    - 🧪 UserDaoTest
      - Insert multiple users from Firebase
      - Retrieve users by ID
      - Clear all users
    - 🧪 PhotoDaoTest
      - Insert single and multiple photos
      - Retrieve photos for a given property ID
      - Delete photos individually or by property
      - Full photo list fetch and clean-up

  - 🧩 **Test Infrastructure Improvements**
    - All DAO tests use a shared DatabaseSetup base class with in-memory Room instance
    - Coroutines (runBlocking) and Flow.first() ensure deterministic and synchronous test assertions
    - Every test runs in isolation with a clean database state to ensure accuracy and repeatability


### 🔹 **Update #17 & #18**

  - 🏗️ **FakeDAO Infrastructure**
    - Implemented BaseFakeDao with in-memory map + StateFlow
    - Specialized FakeDAOs for each domain:
      - FakeUserDao (users, auth, sync flags)
      - FakePhotoDao (pre-seeded photos by propertyId)
      - FakePropertyDao (properties + local poiStore + propertyToPoi links)
      - FakePoiDao (POIs + local propertyStore + poiToProperty links)
      - FakePropertyPoiCrossDao (propertyId ↔ poiId cross-refs)
    - Each FakeDAO mirrors the real DAO interface → repositories tested without Room

  - 🗂️ **FakeEntity / FakeModel Datasets**
    - FakeEntity = predefined DB-like records (UserEntity, PhotoEntity, PropertyEntity, PoiEntity, CrossRefEntity)
    - FakeModel = domain objects (User, Photo, Property, Poi) with relations (photos + poiS + user)
    - Entities = DB state, Models = expected test values
    - Ensures entity ↔ model conversion correctness

  - 🧪 **UserRepository Test**
    - Full coverage with FakeUserDao:
      - getUserById(), getUserByEmail(), authenticateUser()
      - cacheUserFromFirebase(), updateUser(), deleteUser()
      - emailExists(), getAllUsers(), getUnSyncedUsers()

  - 🧪 **PhotoRepository Test**
    - Full coverage with FakePhotoDao:
      - getPhotoById(), getPhotosByPropertyId(), getAllPhotos()
      - insertPhoto(s), deletePhoto(s)

  - 🧪 **PropertyRepository Test**
    - Full coverage with FakePropertyDao, FakePhotoDao, FakeUserDao, FakePoiDao, FakePropertyPoiCrossDao
    - Validated:
      - Sorted retrieval (by date, by title)
      - Filtering (surface, price, type, sold)
      - CRUD + business logic (insert, update, delete, clearAll, markPropertyAsSold)
      - Relation queries (getPropertyWithPoiS → cross-refs + user + photos resolved)
    - DAO-level assertions (entityMap, propertyToPoi) + Model-level assertions (FakePropertyModel)

  - 🧪 **PoiRepository Test**
    - Full coverage with FakePoiDao + FakeUserDao
    - Validated:
    - Retrieval, insert, batch insert, update, delete
    - Relation queries (getPoiWithProperties) + unlink scenario
    - DAO-level validation (entityMap`, poiToProperty) + model-level assertions

  - 🧩 **Shared Test Patterns**
    - Explicit expected = ... values
    - Dual checks: entity-level (DAO map) + model-level (flow result)
    - Deterministic datasets → no flaky tests
    - Coroutine isolation with runTest
    - Reusable pattern for any future Repository


### 🔹 **Update #19**

  - 🧪 **PropertyPoiCrossRepository Test**
    - Full coverage with FakePropertyPoiCrossDao
    - Validated:
      - Retrieval (getAllCrossRefs)
      - Insertions (single, batch)
      - Deletions (by property, by POI) + global clear
      - Queries (getPoiIdsForProperty, getPropertyIdsForPoi)
    - DAO-level checks (entityMap) + model-level Flow assertions
    - Sorted list comparison ensures deterministic results

  - 🏗️ **User Stack Refactor**
    - UserEntity / User model: password field removed, only firebaseUid kept as identity anchor
    - UserDao: authenticate() query removed (delegated to FirebaseAuth), simplified API
    - OfflineUserRepository: updated accordingly, no more local hashing
    - FakeUserDao / FakeUserEntity / FakeUserModel: aligned with new schema
    - ✅ UserRepositoryTest (and also UserDaoTest) fully rewritten:
      - Retrieval (getUserById, getUserByEmail, getAllUsers)
      - Insert (cacheUserFromFirebase)
      - Update, delete, email existence
      - Sync logic (getUnSyncedUsers)
    - 🔄 Unified usage of userEntityList / userModelList in tests for consistency and maintainability

  - 🌐 **Online User Stack**
    - Added UserOnlineEntity, UserOnlineRepository interface, and FirebaseUserOnlineRepository implementation
    - uploadUser(User) → maps to UserOnlineEntity, stores in Firestore, returns domain User (marked isSynced = true)
    - getUser(uid) → fetches from Firestore, maps back to domain User
    - Centralized mapping (User ↔ UserOnlineEntity) handled in utils
    
  - 🔄 **isSynced Propagation**
    - Added isSynced field across all Room entities/models: Property, Photo, Poi, and PropertyPoiCross
    - Updated:
      - Entities (new column is_synced)
      - DAOs (added getUnSynced...() queries for each type)
      - Repositories (flows and save/update methods respect sync state)
      - Mappers (toEntity, toModel) extended to propagate flag
      - Tests (Dao + Repository tests updated with synced/unsynced scenarios)
    - ✅ Enables granular sync tracking: each record can now be individually flagged and synchronized with Firestore


### 🔹 **Update #20**

  - 🔥 Firestore integration completed for all major domain types (User, Property, Photo, Poi, PropertyPoiCross)  
    - Full support in:
      - FirebaseUserOnlineRepository
      - FirebasePropertyOnlineRepository
      - FirebasePhotoOnlineRepository
      - FirebasePoiOnlineRepository
      - FirebasePropertyPoiCrossOnlineRepository
    - Each repository provides:
      - upload<Entity>()
      - get<Entity>() and/or get<Entity>ByPropertyId()
      - delete<Entity>() and deleteAllBy<Criteria>() where relevant
      - Bidirectional mapping via toOnlineEntity() / toModel()
      - Returns domain model with isSynced = true
    - Centralized collection paths via FirestoreCollections.kt to avoid naming errors

  - 🛡️ **ProGuard & R8 Compatibility**
    - Applied @Keep annotation to all OnlineEntity classes (used via Firebase reflection)
    - Updated proguard-rules.pro:
      - Keeps Firestore model classes + annotations
      - Prevents obfuscation of Firestore-bound classes
      - Ensures runtime parsing via toObject() works correctly in release builds

  - 🔁 **Full Domain ↔ OnlineEntity Mapping**
    - Implemented bidirectional mappers toOnlineEntity() and toModel() for all synced types: User, Property, Photo, Poi, PropertyPoiCross
    - Ensures reliable conversion between rich domain models and Firestore-friendly DTOs during upload/download
    - Centralized in the utils package for maintainability and reuse
    - Added Log.d("Mapping", ...) statements in all .toModel() functions to trace deserialization and quickly identify malformed Firestore data
      - Example: Deserialized PropertyOnlineEntity: $this


### 🔹 **Update #21**

  - 🔄 **Offline ➡️ Online Synchronization for All Domain Types**
    - Implemented synchronization logic for User, Property, Photo, Poi, and PropertyPoiCross entities
    - Each entity now supports upload to Firestore from Room when isSynced = false
    - Centralized control via a dedicated SyncManager class that delegates tasks to each specific SyncManager

  - ⬇️ **Online ➡️ Offline Synchronization for All Domain Types**
    - Implemented Firestore-to-Room download logic for all major entities:
      - User, Property, Photo, Poi, and PropertyPoiCross
    - Entities are inserted into Room if missing, or updated only if remote changes are detected
    - Uses new DownloadManager to coordinate entity-specific downloads

  - 🧩 **Entity-Specific Download/UploadManager Classes**
    - Created modular sync handlers:
      - UserDownloadManager/UserUploadManager
      - PropertyDownloadManager/PropertyUploadManager
      - PhotoDownloadManager/PhotoUploadManager
      - PoiDownloadManager/PoiUploadManager
      - PropertyPoiCrossDownloadManager/PropertyPoiCrossUploadManager
    - Each class encapsulates logic to detect, upload, download and update Room entities based on sync status

  - 🗃️ **Upload Methods in Room DAO & Repository Layers**
    - Added update<Entity>() methods in all relevant DAOs to mark items as isSynced = true after upload
    - Repositories now expose update<Entity>() to allow consistent state updates after Firestore interactions
    - Enables reliable post-upload state management to prevent redundant sync attempts

  - 🗃️ **Download Methods in Room DAO & Repository Layers**
    - Added cache<Entity>() methods to safely persist Firestore-fetched data into Room
    - Local data is only overwritten if the remote entity differs (based on field comparison) 
    - Prevents unnecessary writes and preserves local I/O performance

  - ✅ **Comprehensive Test Coverage of Room Sync Logic**
    - Offline ➡️ Online logic (Upload)
      - Added update<Entity>() methods in DAOs and Repositories to mark entities as synced after upload
      - Verified in DAO and Repository tests that isSynced flag is correctly updated
    - Online ➡️ Offline logic (Download)
      - Added cache<Entity>() methods in DAOs and Repositories to store Firestore-fetched data


### 🔹 **Update #22**

  - 🛠️ **Added updatedAt Timestamps for Conflict Resolution**
    - Introduced updatedAt: Long field across all major data layers:
      - Room entity classes (e.g. PropertyEntity)
      - Domain model classes (e.g. Property)
      - Firestore document structure
    - Purpose: ensure reliable conflict resolution between offline and online states
      - During sync, remote data is only written locally if it is more recent than the local version
      - Prevents overwriting local edits with outdated server data and vice versa
      
  - ♻️ **Improved Sync Logic with updatedAt Comparison**
    - Refactored all DownloadManager and UploadManager classes for User, Property, Photo, Poi, and PropertyPoiCross entities
    - Each DownloadManager now checks if the incoming Firestore document has a more recent updatedAt than the local version before updating Room
    - Avoids redundant Room writes and protects recent local edits from being overwritten by older cloud data
    - Makes synchronization behavior more deterministic, efficient, and safe for concurrent data changes across devices

  - ☁️ **Integrated Firebase Storage for Image Synchronization**
    - Replaced Firestore-only photo storage logic with a hybrid approach using Firebase Storage for file handling
    - Photo upload process now:
      - Uploads image files to Firebase Storage using putFile(uri)
      - Retrieves the public downloadUrl
      - Saves image metadata and storageUrl to Firestore (via PhotoOnlineEntity)
    - During download:
      - Firestore provides the storageUrl, and the app downloads the image locally
      - The local uri is regenerated via a temp file for Room storage


### 🔹 **Update #23**

  - 🌐 **Network Monitor Utility**
    - Introduced a reusable NetworkMonitor class to verify internet availability across all Android API levels.
    - Used internally before triggering upload/download operations to avoid failed sync attempts while offline.
    - Compatible with API 21+:
      - Uses NetworkCapabilities on Android M (API 23) and above.
      - Falls back to deprecated but functional activeNetworkInfo on lower versions.
    - Will serve as a foundation for future connectivity-aware features (e.g. observing real-time connection changes).

  - 🔁 **SyncWorker & Background Dependency Injection**
    - Implemented a new SyncWorker based on CoroutineWorker for automatic bidirectional sync between Room and Firebase (upload + download).
    - The worker:
      - Uploads local unsynced entities (users, properties, photos, POIs, cross-links).
      - Downloads and updates local database with cloud data when newer.
      - Ensures network availability using the existing NetworkMonitor utility before launching any sync.
    - Introduced an AppContainerProvider interface:
      - Exposes the application-wide AppContainer (dependency container) from Context.
      - Allows dependency access in SyncWorker, which is not tied to any activity/fragment lifecycle.
      - The application class RealEstateManagerApplication now implements this provider.


### 🔹 **Update #24**

  - 📤 **Read-only ContentProvider for Room database**
    - Added OfflineDatabaseContentProvider to expose internal Room tables (properties, photos, POIs, users, and cross-references) to other apps or components via standard Android ContentProvider mechanism.
    - Registered in the AndroidManifest.xml with proper authority and read permission.
    - Supports only read access (query()), no insert/update/delete, ensuring data integrity and safety.
    - Each DAO now includes a getAllAsCursor() method (e.g. getAllPhotosAsCursor()) to support low-level cursor access from the provider.
    - Useful for:
      - Widgets or services running in separate processes.
      - External apps accessing real estate data through contract-based URIs.
    - Fully tested with new instrumentation tests for each DAO cursor method.

  - 🧠 **Utils Enhancements**
    - Added convertEuroToDollar() utility method to complement the existing convertDollarToEuro() for full bidirectional currency conversion.
    - Updated getTodayDate() to return the date in dd/MM/yyyy format (e.g., "15/09/2025") for improved readability.
    - Deprecated Utils.isInternetAvailable() in favor of the more robust NetworkMonitor.isConnected(), which uses NetworkCapabilities to check for true internet access.
    - All new methods are now covered by unit tests.

  - 🗄️ **Entity ↔ OnlineEntity Mappers Refactor**
    - Replaced old Model ↔ OnlineEntity mappers with direct Entity ↔ OnlineEntity conversions.
    - Each entity (User, Property, Photo, Poi, CrossRef) now has its dedicated bidirectional mapping function.
    - UI continues to consume Models only, while Firebase sync relies exclusively on Entities.

  - ❌ **Soft & Hard Delete Implementation**
    - Introduced is_deleted flag in Room entities.
    - Soft delete: marks an entity as deleted locally, pending Firebase sync.
    - Hard delete: permanent removal from Room after confirmation.
    - DAOs updated with new markAsDeleted queries for each entity type.

  - 🔗 **DAO, Repository & SyncManager Refactor**
    - Room repositories now expose:
      - Classic CRUD operations on Models for the UI layer.
      - New methods dedicated to synchronization: uploadUnSynced…() and download…FromFirebase() → these methods use Room Entities directly, without going through Models.
    - Firebase repositories:
      - Work only with OnlineEntities, linked to Firestore.
      - Mapping to Room Entities is done via toEntity() (and vice versa with toOnlineEntity()).
      - The roomId identifier is systematically used to preserve the Room ↔ Firestore correspondence.
    - SyncManagers (UploadManager / DownloadManager):
      - Centralize the unidirectional synchronization logic (upload or download) for each entity type.
      - No longer go through Models, but only use:
        - RoomEntity ↔ OnlineEntity
        - updatedAt to decide if resynchronization is necessary
        - isDeleted to manage soft deletions
      - Each SyncManager follows a clear pattern:
        - Read unsynchronized data from Room
        - Compare with Firestore (or upload directly)
        - Rewrite to Room with isSynced = true if successful
    - ✅ This guarantees:
      - Clear separation of layers:
      - UI ↔ Models / Room ↔ Entities / Firebase ↔ OnlineEntities
      - Isolated, testable, and reusable synchronization logic
      - Reduction of unnecessary conversions and improved data consistency


### 🔹 **Update #25**

  - 🧪 **DAO Instrumented Test Refactor**
    - All DAO tests (PhotoDao, PropertyDao, PoiDao, PropertyPoiCrossDao, UserDao) were rewritten and expanded to match the new DAO refactoring.
    - Each DAO is now tested with:
      - CRUD operations (insert, update, query, delete).
      - Soft delete (mark…AsDeleted) ensuring entities are hidden logically but preserved in DB.
      - Hard delete (delete…, clearAllDeleted) ensuring soft-deleted rows are physically removed.
      - Synchronization queries (uploadUnSynced…(), downloadFromFirebase…()) validating offline-first behavior.
      - ContentProvider support (getAll…AsCursor) to ensure compatibility with external access.


### 🔹 **Update #26**

  - 🧪 **Room Unit Test Refactor**
    - All unit tests for Room-based repositories (PhotoRepository, PropertyRepository, PoiRepository, PropertyPoiCrossRepository, UserRepository) have been fully rewritten and expanded following the latest repository refactor.
    - Each repository is now thoroughly tested for:
      - CRUD operations: insert, update, query, delete logic validation.
      - Soft deletes: mark…AsDeleted ensures logical deletion without data loss.
      - Hard deletes: delete… and clearAllDeleted remove data physically from local storage.
      - Synchronization logic: uploadUnSynced…() and downloadFromFirebase…() ensure offline-first consistency with Firebase.


### 🔹 **Update #27**

  - 🧪 **Firebase Unit Test Coverage**
    - All Firebase repositories (PhotoRepository, PropertyRepository, PoiRepository, PropertyPoiCrossRepository, UserRepository) now have full unit test coverage.
    - Tests cover:
      - Successful Firestore operations (upload, get, getAll, delete, deleteAllFor…).
      - Failure scenarios, ensuring proper exception wrapping (Firebase…UploadException, Firebase…DownloadException, Firebase…DeleteException).
      - Synchronization consistency between Room entities and their Firebase equivalents, ensuring bidirectional sync via the new UploadManager/DownloadManager layers.


### 🔹 **Update #28**

  - 🧩 **Model & Room/Firebase Entity Mapping Tests**
    - Comprehensive unit tests added for all mapping extensions between:
      - Room Entity ↔ Domain Model
      - Room Entity ↔ Firebase OnlineEntity
    - This includes all major models:
      - Property, Photo, POI, User, PropertyPoiCross
    - Tests verify:
      - Field-by-field conversion accuracy.
      - Default sync states (e.g. isSynced = true, isDeleted = false) during deserialization from Firebase.
      - Compatibility with existing fake data for isolated testing.
  
  - 🔄 **Synchronization layer Unit Testing (Offline ↔ Online)**
    - UploadManager & DownloadManager (Each specific manager is tested individually):
      - UserManager
      - PhotoManager
      - POIManager
      - PropertyManager
      - PropertyPoiCrossRefManager
    - Covered test cases for each manager:
      - Full success (returns SyncStatus.Success)
      - Partial failure (some items return SyncStatus.Failure)
      - Total failure (all items fail)
      - Empty sync (no unsynced items)
    - SyncWorker
      - WorkManager background integration tested with:
        - No internet → returns Result.retry()
        - Successful upload & download → returns Result.success()
        - Exception thrown during sync → returns Result.retry()


### 🔹 **Update #29**

  - 🏛️ **Unified Dependency Injection Architecture**
    - Refactored the entire DI system to establish a single source of truth managed exclusively by Hilt, solving potential "split-brain" issues between Hilt and non-Hilt components. The new implementation is based on three core pillars within the dI package:
      - AppModule as the Centralized Provider:
        - This Hilt module is now the sole authority for creating all application dependencies (Repositories, Managers, DAOs, etc.).
        - It contains all the @Provides functions, acting as a complete recipe book for Hilt.
      - AppContainer as the Abstraction:
        - An interface that defines the complete set of dependencies available to the application.
        - Consumers (like WorkManager) depend on this interface, not on a concrete class, which improves decoupling and testability.
      - AppDataContainer as the Concrete Holder:
        - A data class that implements the AppContainer interface.
        - It has no creation logic; its only role is to hold the dependency instances it receives via its constructor.
    - Hilt now assembles the AppDataContainer by injecting the same @Singleton instances it provides to the rest of the app, guaranteeing a single, consistent dependency graph.

  - 🧩 **Complete ViewModel Architecture**
    - All feature screens now have:
      - A ViewModel class implementing an IViewModel interface (for separation of concerns and easier mocking).
      - A dedicated UiState sealed interface/class to clearly represent screen states: Loading, Success, Error, etc.
    - Improves maintainability, readability, and decouples the UI from internal logic/state.

  - ⚙️ **SharedPreferences with Hilt**
    - Created a UserPreferencesRepository using DataStore to manage:
      - Theme mode (light/dark)
      - Language
    - Repository is injected with Hilt via a dedicated module (UserPreferencesModule).
    - Used in ParametersViewModel.

  - 🧠 **Hilt Integration for ViewModels and Preferences Repository**
    - Migrated all ViewModels to use @HiltViewModel and constructor injection.
    - Hilt modules provide the necessary bindings for repositories (notably PreferencesRepository).
    - Reduces boilerplate and improves testability with proper lifecycle-scoped DI.

  - 🔍 **getUserByFirebaseUid() added to UserRepository**
    - Allows fetching a user from local Room DB using their Firebase UID.
    - Simplifies logic for onboarding, login, or syncing flows.
    - Unit tests cover:
      - Existing UID
      - Missing UID
      - DB empty state

  - 🧭 **Navigation Architecture using NavHost + Sealed Destinations**
    - Introduced a centralized navigation system based on:
      - A NavDestination interface (for route + title abstraction).
      - A RealEstateDestination sealed class that defines all navigation routes with arguments and string resources.
    - This approach improves:
      - Type safety (e.g. createRoute(propertyId) for dynamic paths).
      - Scalability when adding new screens.
      - Consistency and reusability across the app.
    - The NavGraph and NavHost will be implemented based on this foundation to complete navigation flow.


### 🔹 **Update #30**

  - 🧭 **Complete Graph Navigation**
    - Implementation of a centralized NavGraph using NavHost and NavController.
    - Each screen now has a route defined via a sealed class (RealEstateDestination), ensuring better maintainability and type safety.
      - The user flow starts on the Welcome page if the user is not logged in, otherwise on the Home page.
    - Transitions are handled cleanly with popBackStack() and popUpTo() to avoid residual screens in the backstack.
    - This foundation makes it easy to add new screens or modify user paths without breaking existing navigation.
  
  - 🚀 **Welcome Screen with integrated navigation**
    - Added a complete welcome screen with a background image, a login button, a sign-up button, and a help button.
    - The screen acts as the entry point to the application and redirects to the various feeds.
    - The design was created using Jetpack Compose and complies with the application's overall visual style guide.

  - 🔐 **Login screen**
    - The login screen includes an email field, a password field with controlled visibility, and a link to the “Forgot your password” page.
    - A login button redirects to the Home page after validation.
    - The actual authentication is not yet connected, but navigation is fully operational.

  - 🆕 **Account creation screen**
    - Addition of a complete form: email, agent name, password, and confirmation.
    - Form validity check on the UI side (password matching, non-empty fields).
    - Redirection to the Home page after success (simulated).
    - The registration backend will be connected in a future iteration.

  - 📨 **Forgot password screen**
    - Implementation of an email field with validation.
    - Action button ready to be connected to a password reset logic.
    - Integrated back navigation via a Back button.

  - ☎️ **Contact screens**
    - Addition of a Contact Info screen allowing users to choose between contacting by email or chat.
    - The EmailContact screen has a complete form (email, subject, message).
    - The ChatContact screen displays an information message (placeholder for future real-time messaging).

  - 🆘 **Global help button**
    - Integrated into several key screens (Welcome, Login), it provides quick access to the contact screen.
    - Ensures a smooth and consistent UX for user support.

  - 🧭 **Smooth post-authentication navigation**
    - After a simulated login or registration, the user is automatically redirected to the Home page.
    - The popUpTo clears the backstack to prevent any unintended return to the authentication screens.

  - 🖼️ **Complete UI in Jetpack Compose**
    - All screens are now built with Compose: forms, buttons, icons, and navigation.
    - The design is consistent, responsive, and follows Material 3 principles.
    - Placeholders and interactions are ready to be connected to business logic.

  - ⚠️ **Business logic coming soon**
    - The UI is functional, but no persistence or backend logic is connected yet.
    - The integration of authentication, account registration, and email/chat messaging will be part of future updates.


### 🔹 **Update #31**

  - ✅ **Complete implementation of login and account creation logic with Firebase integration and local Room syncing**
    - The login and sign-up screens are now fully connected to Firebase Authentication.
    - On success, users are created both remotely (Firestore) and locally (Room).
    - ViewModels emit LoginUiState for loading, success, and error states, handled via StateFlow.
    - Error messages are contextualized using localized strings and reflect Firebase-specific exceptions.
    - Includes validation for email format, password matching, and minimum password length.
    - Navigation on success uses popUpTo() to ensure clean back stack behavior.

  - 🧱 **Refactored User data layer (Firebase + Room) including mappers for safer sync and validation**
    - UserOnlineEntity has been simplified: firebaseUiD is now excluded from the document body and used as Firestore document ID only.
    - Room entity UserEntity enforces uniqueness on both email and firebase_uid via @Index.
    - Mappers were updated accordingly:
      - UserEntity.toOnlineEntity() sends roomId to Firestore.
      - UserOnlineEntity.toEntity(userId, firebaseUid) ensures data is reconstructed with the correct Firestore UID.

- 🔄 **Improved user sync logic between Room and Firestore with firebaseUid mapping and conflict handling**
  - The UserUploadManager now uploads local users using their firebaseUid as document ID.
  - Conflicts are prevented by checking if email or roomId already exist in Firestore before insertion.
  - The UserDownloadManager uses FirestoreUserDocument to retrieve both the Firestore UID and document content.
  - The sync managers now log detailed SyncStatus per user (Success or Failure), making debugging easier and more transparent.

- 🧪 **Refactored all user-related unit and instrumentation tests to align with new data & sync logic**
  - UserDaoTest updated to reflect new insert strategies and unique constraints.
  - OfflineUserRepositoryTest extended to verify proper handling of firebaseUid, soft deletes, and sync flags.
  - FirebaseUserOnlineRepositoryTest includes new test cases for duplicate checks and exception handling.
  - UserUploadManagerTest and UserDownloadManagerTest now test real sync flows and validate UID-to-roomId mapping logic.


### 🔹 **Update #32**

  - 🔐 **Optimized sign-in flow to use local data when available and fallback to Firestore when necessary**
    - The LoginViewModel signIn() logic now checks whether the user already exists in the local Room database before re-creating or uploading them.
    - If the user is not found locally, it falls back to Firebase Firestore using UserOnlineRepository.getUser(uid) to fetch the UserOnlineEntity, then reconstructs the full domain User.
    - The fallback user is saved to Room to ensure consistency for future offline-first behavior.
    - This avoids unnecessary writes to Firestore and protects against inconsistent data overwrites.
    - A new mapping logic using remoteUser.user.toEntity(...).toModel() is used to convert Firestore user documents into domain models.

  - 🧱 **Complete refactor of data model architecture for sync-safe, scalable offline-first structure**
    - Introduced universalLocalId: UUID in all models to uniquely identify entities across devices and offline sessions.
    - Added optional firestoreDocumentId: String? in all models/entities to track remote Firestore references.
    - All Room Entities (UserEntity, PropertyEntity, PhotoEntity, PoiEntity, PropertyPoiCrossEntity) now mirror the domain structure and enforce integrity via indices and foreign keys.
    - Mappers between Model, Room, and Online entities now follow a clean architecture approach with:
      - Strict ownership of ID generation
      - Controlled sync state flags (isSynced, isDeleted)
      - Full offline reconstruction support
    - Adapted new relation classes and converters:
      - PropertyWithPoiS, PoiWithProperties (Model)
      - PropertyWithPoiSRelation, PoiWithPropertiesRelation (Room)
    - Updated toFullModel() logic for PropertyEntity to dynamically rebuild properties with photos and POIs using UUID-based linking.
  
  - 📦 **Refactored Room DAOs for all entities to support offline sync and consistency**
    - Migrated all Room DAOs to use String based UUID identifiers instead of Long.
    - Standardized isSynced and isDeleted logic across all DAO operations:
      - Inserts from UI set isSynced = false, inserts from Firestore set isSynced = true.
      - Soft deletes mark isDeleted = true with updated_at tracking and deferred sync.
      - Hard deletes physically remove entities only after confirmation from Firestore.
    - Split insert/update/delete logic by data source (UI vs Firebase) for deterministic behavior.
    - All DAOs now expose complete test and debug visibility (e.g. IncludeDeleted() queries).

  - 🧩 **Updated all repository interfaces and implementations to reflect the new architecture**
    - All repositories (User, Property, Photo, POI, CrossRef) have been refactored to operate on UUID-based models with firestoreDocumentId support.
    - Interface contracts now enforce separation between local (Room) and remote (Firestore) operations, with clear mapping rules.
    - This enables:
      - Predictable and testable repository logic
      - Full offline-first behavior without redundancy
      - Consistent sync strategies across all entities

  - 🔄 **Refactored the entire Sync layer to support per-entity upload/download management with full conflict resolution**
    - Refactored dedicated UploadManager and DownloadManager classes for each entity type (User, Property, Photo, POI, CrossRef) to encapsulate synchronization logic.
    - Sync operations now use updatedAt timestamps to resolve conflicts between local and remote data, ensuring deterministic updates.
    - Added unified handling of soft deletion: entities marked as isDeleted = true are propagated to Firestore and hard-deleted from Room only after successful remote confirmation.
    - All sync flows emit standardized SyncStatus objects (Success/Failure), providing consistent feedback and traceability during synchronization cycles.
    - These managers integrate seamlessly with the updated repository and DAO layers, enabling fully modular, testable, and scalable sync infrastructure.

  - 🎯 **Refactored LoginViewModel to align with the offline-first architecture and new repository model**
    - The signIn() flow now queries Room for the user by firebaseUid; if not found, it falls back to Firestore and reconstructs the domain model from the remote UserOnlineEntity.
    - The fallback path ensures the user is inserted into Room via insertUserInsertFromFirebase() to enable full offline support immediately after login.
    - The signUp() logic creates a full User domain model locally and inserts it into Room using firstUserInsert(), followed by uploading the user to Firestore via uploadUser() using the stable UUID.
    - Mapping from domain to Firestore is performed using toEntity().toOnlineEntity(), guaranteeing ID stability and sync consistency.


### 🔹 **Update #33**

  - 🏗️ **Step-by-step property creation form (8 steps)**
    - A new PropertyCreationPage was introduced with the following screens:
      - Intro – Simple welcome page
      - Property Type – Select type with icon picker
      - Address – Inputs for street, city, postal code, country
      - POIs – Add up to 5 POIs (School, Grocery, etc.) with full address and type
      - Description – Enter price, surface, rooms and textual description
      - Photos – Add photos via file picker, with live preview and deletion
      - Static Map – Fetch a clean static map centered on the address with POI markers
      - Confirmation – Show full summary of all data before creation

  - 📸 **Photo handling**
    - Uses ActivityResultContracts.GetContent()
    - Saves image to local app storage
    - Allows inline deletion before submission

  - 🗺️ **Static map rendering**
    - Uses StaticMapRepository.getStaticMapImage(config)
    - Markers include property and POIs with labels
    - Cleans up default Google POIs and visual clutter using map style rules
    - Map image is persisted to draft and reused in confirmation

  - 🧠 **Draft logic with PropertyDraft model**
    - All intermediate input is stored in a PropertyDraft
    - Values are updated step-by-step
    - Reused in confirmation step and final property creation logic
    - Supports POI modeling with PoiDraft
    - Supports saving temporary photos with local file URIs

  - 🧪 **Confirmation screen and submission**
    - Displays formatted sections: address, POIs, description, photos, map
    - Calls createModelFromDraft() which builds a full Property with relations
    - Inserts Property, Poi, Photo, and PropertyPoiCross into Room
    - Sync-ready, and offline-first compatible


### 🔹 **Update #34**

  - 🧠 **Simplification & centralization of state management (UI State)**
    - The ViewModel for property creation has been refactored to rely solely on a StateFlow<PropertyCreationUiState>, removing the currentStep, propertyDraft, isNextEnabled, etc. fields as independent variables.
    - A single source of truth is used: PropertyCreationUiState.StepState, which contains:
      - currentStep
      - draft
      - isNextEnabled
      - error
      - isLoadingMap
      - staticMapImageBytes
    - All UI screens now consume this uiState declaratively (collectAsState()).

  - 🏠 **PropertyListScreen and state driven loading**
    - Implemented the full PropertiesListScreen connected to PropertiesListViewModel.
    - Properties are dynamically loaded based on the presence (or absence) of filters.
    - The UI reacts to PropertiesListUiState (Idle, Loading, Success, Error) for a declarative display.

  - 🔍 **Advanced filtering system**
    - Introduced a bottom sheet for applying filters (type, status, price range, surface range).
    - Filters are stored centrally in HomeUiState, then passed to the list screen.
    - Type filtering uses a new icon-based selector UI, replacing the dropdown.
    - Filtering logic is performed via repository calls inside PropertiesListViewModel.

  - 🎨 **Theming overhaul**
    - Updated the application’s color palette using MaterialTheme.colorScheme across components.
    - Applied consistent colors for icons, backgrounds, text, outlines, and surfaces.
    - Improved contrast and primary accent color usage to enhance visual clarity and UX.


### 🔹 **Update #35**

  - 🗺️ **Google Maps integration**
    - Integrated Google Maps into the app's Home screen via GoogleMapScreen.
    - Properties and POIs are displayed as interactive markers on the map.
    - Each marker is styled with a custom icon.

  - 📍 **Accurate user location handling**
    - Implemented real-time user tracking with FusedLocationProviderClient.
    - The camera centers on the user's current position when permission is granted.
    - If location is unavailable, a proper fallback message is shown.

  - 📌 **Fixed-time geocoding**
    - Properties and POIs are geocoded only once when they are created.
    - Their latitude and longitude are stored in the local Room database and synced to Firebase.
    - This eliminates the need to geocode again when displaying the map.

  - 🧱 **Enriched data models (lat/lng fields)**
    - Added latitude and longitude fields to the following layers:
      - Property and Poi model classes.
      - Room entities and Firebase DTOs (OnlinePropertyEntity, etc.).
      - All mappers and converters (to/from Firebase and Room).
    - Ensures that geolocation data persists across devices and survives offline mode.
    
  - 🌐 **Network awareness**
    - The map screen detects whether an internet connection is available.
    - If offline, a fallback message is shown, but map markers still render using saved data.
    - Reinforces the offline-first design philosophy of the app.

  - 🚀 **Optimized map performance**
    - The map is now displayed as soon as the user's location is available, without waiting for properties or POIs to load.
    - This change significantly reduces perceived loading time, especially on slower networks or large datasets.
    - Markers are added incrementally after the map is visible, improving user feedback and responsiveness.

  - 🧩 **Separated UI loading states**
    - Introduced a new intermediate UI state: OnlyUserLocation, representing when the user’s location is known but marker data isn’t yet ready.
    - This separation allows the map to initialize with just the user position, then update once properties and POIs are fetched.
    - Enhances user experience by avoiding full-screen spinners during partial data loading.

  - ✅ **Removed runtime geocoding at map display**
    - No geocoding calls are made during map screen usage.
    - All coordinates (lat/lng) are computed once at creation time and reused from the local database.
    - Improves performance, avoids crashes in offline mode, and removes dependency on the Geocoder service.


### 🔹 **Update #36**

  - 🎯 **Map-based dynamic filtering**
    - GoogleMapScreen now supports advanced property filtering, synchronized with the filter system used in PropertiesListScreen.
    - Filters (type, price range, surface range, status) are passed from HomeUiState to both list and map views.
    - GoogleMapViewModel applies filters using PropertyRepository and updates the UI state accordingly.
    - The GoogleMapUiState.Success state now includes isFiltered and activeFilters fields for better UI control.
    - The map updates markers in real-time based on the selected filters, offering a consistent and unified UX.

  - 🔄 **ViewModel state parity**
    - The filtering logic is now fully shared between the list and map screens, ensuring consistency.
    - Both screens react to the same PropertyFilters object and load filtered or full datasets accordingly.
    - Code duplication has been reduced by centralizing filtering in the repository layer.

  - 💡 **Idle UI state for GoogleMap**
    - Introduced a new Idle state in googleMapUiState, enabling the map to reset its display when no data is available.
    - Helps manage UX during state transitions (e.g., after filter reset or screen changes).

  - 🖼️ **Improved static map generation and persistence**
    - Fixed a critical issue where static maps generated during property creation in the StepScreen were always overwriting the same file. Each property now generates a static map image file using a unique name based on its ID, preventing any loss of data or map duplication.
    - The StepScreen now correctly saves and persists static map files for each property independently, even when multiple properties are created or modified consecutively. This change ensures full compatibility with offline usage and improves reliability of map rendering across the app.

  - 📄 **Added Property Details screen**
    - The PropertyDetailsPage was Added and display complete view of the property, integrating a static map display at the bottom, agent information, associated POIs with icons, and dynamic image carousels.
    - Static maps now appear seamlessly in the layout with full width and no border styling, offering a cleaner and more consistent visual integration. Conditional logic was added to handle null fields (like sale date), and visual separators (dividers) improve content readability.


## 🤝 **Contributions**
Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.