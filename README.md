# 🏠 **RealEstateManager**
**RealEstateManager** is a modern Android application aimed at helping real estate agents manage exceptional property listings from their mobile device. Built to support a digital transformation for a prestigious New York agency, the app allows agents to browse, add, edit, and geolocate real estate properties even in offline mode.
This project is developed using modern Android architecture principles, with a focus on local data persistence, modular code, and responsive design. It evolves from a legacy intern prototype and will be gradually refactored and extended.


## 📚 **SUMMARY**
- [✅ LAST MAJOR UPDATES](#-last-major-updates-see-updatesmd-for-details)
- [❌ NEXT UPDATES](#-next-updates)
- [📋 Features](#-features)
- [🛠️ Tech Stack](#️-tech-stack)
- [🚀 How to Use](#-how-to-use)
- [📸 Screenshots](#-screenshots)
- [🤝 Contributions](#-contributions)


## ✅ **LAST MAJOR UPDATES (see [UPDATES.md](./UPDATES.md) for details)**
   
   - 🔐 Updated Firestore security rules for POIs: adjusted permissions to allow shared POI reuse across users while maintaining controlled creation ownership.
   - 🔎 Implemented smart POI matching in repository: added normalized Room-level lookup to prevent duplicates and automatically reuse existing POIs during creation.
   - ✏️ Refactored POI handling in ViewModel (creation & edition): introduced UI-level comparison logic to detect unchanged POIs and recreate cross-references safely when modifications occur.
   - 🔁 Strengthened Property POI relation integrity: migrated CrossRef DAO insert strategy to OnConflictStrategy.REPLACE to correctly reactivate soft-deleted links and prevent duplicate relation issues.
   - 🧪 Refactored fake data layer for unit testing: updated all Fake Room entities, Fake Firestore online entities, and Fake models to fully align with the current synchronization contract (soft delete, isSynced, cross-relations) to ensure deterministic repository and sync testing.
   - 🧩 Rebuilt all Fake DAOs for unit testing: implemented in-memory, Flow-based DAO replicas matching the real Room interfaces, enabling reliable testing of repositories, sync engine, and utility logic without database dependencies.
   - 📱 Updated fake entities for instrumented DAO tests: aligned Android test fakes with the real Room schema to validate DAO behavior (queries, soft delete, conflict strategies, relations) against the production database configuration.


## ❌ **NEXT UPDATES**

   - ⚠️ Implement backend logic for Forgot Password and Contact page.
   - Implemented responsive design for tablet.
   - 🔔 Notification on property creation.
   - 🧪 Testing coverage


## 📋 **Features**

   - 🏠 **Property Listings** :

      - ✅ **DONE** Create/edit property listings.
      - ✅ **DONE** View detail and photos of each property.
      - ✅ **DONE** Add status (available / sold).
      - ✅ **DONE** Add real estate agent assigned to each listing.
   
   - 📍 **Geolocation** :

      - ✅ **DONE** Auto-map property using Static Maps API.
      - ✅ **DONE** Display pins of nearby listings on a map.
      - ✅ **DONE** Retrieve and display user current location.
      - ✅ **DONE** Generate and store static map previews linked to properties.
      - ✅ **DONE** Static map images persist offline and are restored after synchronization.

   - 🔐 **User Authentication with Firebase** :

      - ✅ **DONE** Account creation and login exclusively online using Firebase Authentication.
      - ✅ **DONE** User's profile (ID, email, agent name), once authenticated, is cached locally in Room.
      - ✅ **DONE** Offline usage supported (if already logged in).

   - 🔁 **Global Data Sync** :

      - ✅ **DONE** Full sync of all entities: Users, Properties, Photos, POIs, Cross-References.
      - ✅ **DONE** One-way sync from local to Firebase (upload).
      - ✅ **DONE** One-way sync from Firebase to local (download).
      - ✅ **DONE**  Entity-specific managers for modular synchronization logic.
      - ✅ **DONE** Conflict resolution using timestamp-based last-write-wins strategy (updatedAt).
      - ✅ **DONE** Background sync using WorkManager + SyncWorker, enabled via AppContainerProvider.
      - ✅ **DONE** Full sync of Static Maps (metadata + image files).
      - ✅ **DONE** Offline-first synchronization for static maps using timestamp-based conflict resolution.
      - ✅ **DONE** Deferred hard deletion handled by backend cleanup after successful multi-device synchronization.


   - 📷 **Media Management** :

      - ✅ **DONE** Take or select photos from gallery.
      - ✅ **DONE** Add multiple images per listing.
      - ✅ **DONE** Downloads images from Firebase Storage and saves them locally on device during sync.
      - ✅ **DONE** Static map images are uploaded to and downloaded from Firebase Storage.
      - ✅ **DONE** Local caching of static map images for offline access.
      - ✅ **DONE** Local file lifecycle management (cleanup on delete or replace).

   - 🔍 **Search** :

      - ✅ **DONE** Multi-criteria search (surface, price, type, POI, dates, sold status).
      - ✅ **DONE** Sort and filter property results.

   - 💾 **Offline Mode** :

      - ✅ **DONE** Offline access to all data (users, properties, photos, POIs, links)
      - ✅ **DONE** Full app usability offline (read/write locally, queue for sync).
      - ✅ **DONE** Changes made offline are queued for upload on next connectivity.
      - ✅ **DONE** Static maps fully supported offline (create, update, delete).
      - ✅ **DONE** Static map changes are queued and synchronized when connectivity is restored.

   - ☁️ **Online mode with Firebase Firestore**

      - ✅ **DONE** Upload and download of user and property data with Firebase Firestore.
      - ✅ **DONE** SyncManager to orchestrate entity-level sync logic.
      - ✅ **DONE** Upload/download of associated entities: photos, POIs, and cross-links.
      - ✅ **DONE** Integrated Firebase Storage for image file handling; only metadata is stored in Firestore while files are uploaded to Storage.
      - ✅ **DONE** Static map metadata stored in Firestore with ownership-based security rules.
      - ✅ **DONE** Static map images stored in Firebase Storage and linked via Firestore.
      - ✅ **DONE** Firestore security rules enforcing ownership-based access and forbidding client-side hard deletes.

   - 🧹 **Data Deletion & Lifecycle Management** :

      - ✅ **DONE** Global soft-delete strategy using isDeleted flags.
      - ✅ **DONE** Soft deletion applied consistently across all entities (Room + Firestore).
      - ✅ **DONE** Deletions propagated safely across devices via sync managers.
      - ✅ **DONE** Remote deletion state mirrored locally to prevent zombie data.
      - ✅ **DONE** Conflict resolution for deletions using updatedAt timestamps.
      - ✅ **DONE** Local hard delete performed only after successful sync.
      - ✅ **DONE** Server-side cleanup of deleted data using Firebase Cloud Functions.
      - ✅ **DONE** Weekly scheduled cleanup job permanently removes soft-deleted data older than 30 days.
      - ✅ **DONE** Hard deletion of Firestore documents and associated Firebase Storage files.
      - ✅ **DONE** Cleanup executed exclusively server-side via Firebase Admin SDK.


   - 📡 **Interoperability** :

      - ✅ **DONE** Exposed Room database via a read-only ContentProvider.
      - ✅ **DONE** Supports standard URI-based queries from external components or apps.
      - ✅ **DONE** Grants permission-controlled access (read-only) to data tables: properties, photos, users, POIs, and cross-links.

   - 🧠 **Utilities** :

      - ✅ **DONE** Convert dollar to euro.
      - ✅ **DONE** Convert euro to dollar.
      - ✅ **DONE** Get today's date (format to be improved).
      - ✅ **DONE** Reliable internet check using NetworkMonitor (supports API 21+ with fallbacks).
      - ✅ **DONE** Add unit and integration tests.

   - 🧭 **Navigation** :

      - ✅ **DONE** Navigation between list and detail.
      - ✅ **DONE** Setting up core navigation components:
        - Implementing a central NavHost in the main activity.
        - Defining the NavGraph with all destinations and actions.
        - Using a single NavController to manage navigation events.
      - ✅ **DONE** Two-pane mode support for tablets.

   - 🎨 **Modern and Fluid Interface**:

      - ✅ **DONE** Follows Material Design 3 guidelines.
      - ✅ **DONE** Smooth transitions with Navigation Component.
      - ✅ **DONE** Responsive layout with adaptive UI.

      - **TopBar**:
         - ✅ **DONE** Display application title and possible future actions.

      - **Light/Dark Mode**:
         - ✅ **DONE** Supports light/dark mode.

      - **Custom theme**:
         - ✅ **DONE** Implemented custom colors and shapes.
         - ✅ **DONE** Implemented custom Google Fonts.

   - 🛠️ **Preferences & Settings** :

      - ✅ **DONE** Change application language (English 🇺🇸 / French 🇫🇷).
      - ✅ **DONE** Change display currency (USD 💵 / EUR 💶).
      - ✅ **DONE** Prices are dynamically converted based on selected currency (stored in dollars).
      - ✅ **DONE** Strings and layouts automatically adapt to selected language.
      - ✅ **DONE** Real-time UI updates without app restart when changing language or currency.

   - 🔄 **Real-time status management**:

      - ✅ **DONE** Use of StateFlow for UI state handling.
      - ✅ **DONE** ViewModel for lifecycle-aware logic.
      - ✅ **DONE** Coroutines for async data operations.

   - 🧠 **Architecture & Code Structure**:

      - ✅ **DONE** Refactored legacy Java into clean MVVM structure.
      - ✅ **DONE** Modularized repositories, DAOs, entities, and mappers with separation of concerns.
      - ✅ **DONE** Migrated dependency injection from manual AppContainer to Dagger Hilt.
      - ✅ **DONE** Static Map entity follows the same clean architecture pattern as Photos (Room ↔ Repository ↔ Sync ↔ Firebase).
      - ✅ **DONE** Dedicated upload and download managers for static map synchronization.

   - 🚀 **Performance and responsiveness**:
   
      - 🟩 **IN PROGRESS** Optimize UI scrolling and animations.

   - 🧪 **Testing & Quality Assurance** :

      - ✅ **DONE** Unit test for dollar to euro conversion.
      - ✅ **DONE** Unit test for euro to dollar conversion.
      - ✅ **DONE** Unit test for date formatting.
      - ❌ **NOT IMPLEMENTED** Integration test for network availability.
      - 🟩 **IN PROGRESS** Unit test for Models/Entities/OnlineEntities mappers.
      - 🟩 **IN PROGRESS** DAO tests using instrumented tests for Room database.
      - 🟩 **IN PROGRESS** Repository unit tests using fake DAO architecture and model/entity separation.
      - 🟩 **IN PROGRESS** Firebase Repository unit tests using offline/online entities separation.
      - 🟩 **IN PROGRESS** Unit test for Sync layer (download/upload managers) unit tests between Room and Firebase Repositories.


## 🛠️ **Tech Stack**

   - **Java & Kotlin** : Language (Java legacy + Kotlin migration).
   - **SQLite / Room** : Local persistence.
   - **MVVM** : Architecture separation.
   - **ViewModel** : Lifecycle-aware logic.
   - **LiveData/Flow** : Reactive data Updates.
   - **ContentProvider** : External data access layer.
   - **State Management**: Handle states with MutableStateOf and StateFlow.
   - **Navigation Component**  : Screen transitions.
   - **Coroutines** : Background processing.
   - **Jetpack Compose** : Future UI enhancements.
   - **Material 3**: Modern, accessible user interface.
   - **Retrofit** : Networking client for API calls.
   - **Kotlinx Serialization** : JSON serialization/deserialization with Kotlin.
   - **Google Maps Static API** : Used to render static images of property locations.
   - **AppContainer (Manual DI)** : `DEPRECATED` Centralized dependency management without external DI framework.
   - **Dagger Hilt** : `CURRENT` Dependency injection framework for the entire app.
   - **Internal Storage API** : Used to persist static map images locally for offline access.
   - **BuildConfig / Secure API Key Handling** : Prevent exposing secrets via Gradle-based injection.
   - **Google Play Services (Maps & Location)** : For real-time location tracking and interactive map display.
   - **Kotlinx Coroutines Play Services** : To integrate Google Play Services APIs with Kotlin coroutines for asynchronous tasks.
   - **ThreeTenABP** : Backport of Java Time API (LocalDate, etc.) for Android API 21+, enabling modern date/time handling on older devices.
   - **Firebase Authentication** : Secure user login using email/password and Firebase Identity platform.
   - **Firebase Firestore** : Scalable NoSQL cloud database used for syncing property data online.
   - **Firebase Analytics** : Tracks user engagement and feature usage to inform future improvements.
   - **SHA-256 (MessageDigest)** : Secure local password hashing for authentication.
   - **SyncManager / UserSyncManager** : Synchronization layer for uploading modified Room data to Firestore.
   - **MapperUtils** : Convert between model (User) and storage representations (UserEntity, UserOnlineEntity).
   - **ContentProvider** : External data access layer.
   - **Room Cursor Support** : Custom DAO queries returning Cursor for inter-process access through ContentProvider.
   - **Jetpack DataStore (Preferences)** : Modern, asynchronous key-value storage used for persisting user preferences (language, currency, etc.).
   - **Firebase Cloud Functions (v2)** : Server-side scheduled jobs for data lifecycle management and cleanup.
   - **Firebase Admin SDK** : Privileged backend access to Firestore and Storage (bypasses client security rules).
   - **Cloud Scheduler** : Time-based execution of backend cleanup tasks.
   - **WindowSizeClass & WindowInsets** : Responsive UI adaptation across smartphones and tablets.


## 🚀 **How to Use**

❌ **THIS SECTION IS NOT AVAILABLE YET**


## 📸 **Screenshots**

- **Welcome page**:

   ![Welcome page](screenshots/welcome_page.png)
   
- **Login page**:

   ![Login page](screenshots/login_page.png)

- **Forgot password page**:

   ![Forgot password page](screenshots/forgot_password_page.png)

- **Account creation page**:

   ![Account creation page](screenshots/account_creation_page.png)

- **Help page**:

   ![Help page](screenshots/help_page.png)

- **Email contact page**:

   ![Email contact page](screenshots/email_contact_page.png)

- **Chat contact page**:

   ![Chat contact page](screenshots/chat_contact_page.png)

- **Property creation page**:

   ![Step 1 screen](screenshots/property_creation_step_1_screen.png)
   ![Step 2 screen](screenshots/property_creation_step_2_screen.png)
   ![Step 3 screen](screenshots/property_creation_step_3_screen.png)
   ![Step 4 screen](screenshots/property_creation_step_4_screen.png)
   ![Step 5 screen](screenshots/property_creation_step_5_screen.png)
   ![Step 6 screen](screenshots/property_creation_step_6_screen.png)
   ![Step 7 screen](screenshots/property_creation_step_7_screen.png)
   ![Step 8 screen](screenshots/property_creation_step_8_screen.png)
   ![Step 8 screen 2](screenshots/property_creation_step_8_screen_2.png)

- **HomePage with PropertyListScreen**:

   ![HomePage with PropertyScreen](screenshots/home_page_1.png)
   ![HomePage with PropertyScreen (MenuDrawer open)](screenshots/home_page_2.png)
   ![HomePage with PropertyScreen (FilterScreen open)](screenshots/home_page_3.png)

- **HomePage with GoogleMapScreen**:

   ![HomePage with GoogleMapScreen](screenshots/home_page_4.png)
   ![HomePage with GoogleMapScreen (MenuDrawer open)](screenshots/home_page_5.png)
   ![HomePage with GoogleMapScreen (FilterScreen open)](screenshots/home_page_6.png)

- **UserPropertiesPage**:

   ![UserPropertiesPage](screenshots/user_properties_page.png)

- **DetailPage**:

   ![DetailPage](screenshots/detail_page.png)
   ![DetailPage2](screenshots/detail_page_2.png)

- **SettingsPage**:

   ![SettingsPage](screenshots/settings_page.png)


## 🤝 **Contributions**
Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.