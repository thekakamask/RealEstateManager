# 🏠 **RealEstateManager**
**RealEstateManager** is a modern Android application aimed at helping real estate agents manage exceptional property listings from their mobile device. Built to support a digital transformation for a prestigious New York agency, the app allows agents to browse, add, edit, and geolocate real estate properties even in offline mode.
This project is developed using modern Android architecture principles, with a focus on local data persistence, modular code, and responsive design. It evolves from a legacy intern prototype and will be gradually refactored and extended.


## ✅ **LAST MAJOR UPDATES (see [UPDATES.md](./UPDATES.md) for details)**

   - 🌐 Added NetworkMonitor utility to safely check internet availability on all Android API levels
   - 🔁 Created SyncWorker with WorkManager to automate data sync + introduced AppContainerProvider interface to access DI from background workers


## ❌ **NEXT UPDATES**

   - 🧠 ViewModel Implementation
   - 🔄 State Management with LiveData / StateFlow
   - 💾 Prepopulate sample data
   - 📤 ContentProvider setup (for external data access)
   - 🔔 Notification on property creation
   - 🧰 Improve Utils.java methods
   - 🛡️ Permission & Error Handling Layer.


## 📋 **Features**

   - 🏠 **Property Listings** :

      - 🟩 **IN PROGRESS** Create/edit property listings.
      - 🟩 **IN PROGRESS** View detail and photos of each property.
      - 🟩 **IN PROGRESS** Add status (available / sold).
      - 🟩 **IN PROGRESS** Add real estate agent assigned to each listing.
   
   - 📍 **Geolocation** :

      - ✅ **DONE** Auto-map property using Static Maps API.
      - 🟩 **IN PROGRESS** Display pins of nearby listings on a map.
      - 🟩 **IN PROGRESS** Retrieve and display user current location.

   - 🔐 **User Authentication with Firebase** :

      - ✅ **DONE** Account creation and login exclusively online using Firebase Authentication.
      - ✅ **DONE** User's profile (ID, email, agent name), once authenticated, is cached locally in Room.
      - ✅ **DONE** Offline usage supported (if already logged in).

   - 🔁 **Global Data Sync** :

      - ✅ **DONE** Full sync of all entities: Users, Properties, Photos, POIs, Cross-References.
      - ✅ **DONE** One-way sync from local to Firebase (upload).
      - ✅ **DONE** One-way sync from Firebase to local (download).
      - ✅ **DONE**  Entity-specific managers for modular synchronization logic.
      - ✅ **DONE** Conflict resolution (e.g. field-level merge or overwrite strategies)
      - ✅ **DONE** Background sync using WorkManager + SyncWorker, enabled via AppContainerProvider.

   - 📷 **Media Management** :

      - ❌ **NOT IMPLEMENTED** Take or select photos from gallery.
      - 🟩 **IN PROGRESS** Add multiple images per listing.
      - 🟩 **IN PROGRESS** Downloads images from Firebase Storage and saves them locally on device during sync.

   - 🔍 **Search** :

      - 🟩 **IN PROGRESS** Multi-criteria search (surface, price, type, POI, dates, sold status).
      - 🟩 **IN PROGRESS** Sort and filter property results.

   - 💾 **Offline Mode** :

      - ✅ **DONE** Offline access to all data (users, properties, photos, POIs, links)
      - ✅ **DONE** Full app usability offline (read/write locally, queue for sync).
      - ✅ **DONE** Changes made offline are queued for upload on next connectivity.

   - ☁️ **Online mode with Firebase Firestore**

      - ✅ **DONE** Upload and download of user and property data with Firebase Firestore.
      - ✅ **DONE** SyncManager to orchestrate entity-level sync logic.
      - ✅ **DONE** Upload/download of associated entities: photos, POIs, and cross-links.
      - 🟩 **IN PROGRESS** Real-time Firestore listeners (multi-device live updates).
      - ✅ **DONE** Integrated Firebase Storage for image file handling; only metadata is stored in Firestore while files are uploaded to Storage.

   - 🧠 **Utilities** :

      - ✅ **DONE** Convert dollar to euro.
      - ❌ **NOT IMPLEMENTED** Convert euro to dollar.
      - ✅ **DONE** Get today's date (format to be improved).
      - ✅ **DONE** Reliable internet check using NetworkMonitor (supports API 21+ with fallbacks).
      - ❌ **NOT IMPLEMENTED** Add unit and integration tests.

   - 🧭 **Navigation** :

      - ❌ **NOT IMPLEMENTED** Navigation between list and detail.
      - ❌ **NOT IMPLEMENTED** Two-pane mode support for tablets.

   - 🎨 **Modern and Fluid Interface**:

      - ❌ **NOT IMPLEMENTED** Follows Material Design 3 guidelines.
      - ❌ **NOT IMPLEMENTED** Smooth transitions with Navigation Component.
      - ❌ **NOT IMPLEMENTED** Responsive layout with adaptive UI.

      - **TopBar**:
         - ❌ **NOT IMPLEMENTED** Display application title and possible future actions.

      - **Light/Dark Mode**:
         - ✅ **DONE** Supports light/dark mode.

      - **Custom theme**:
         - ✅ **DONE** Implemented custom colors and shapes.
         - ✅ **DONE** Implemented custom Google Fonts.

   - 🔄 **Real-time status management**:

      - ❌ **NOT IMPLEMENTED** Use of StateFlow for UI state handling.
      - 🟩 **IN PROGRESS** ViewModel for lifecycle-aware logic.
      - 🟩 **IN PROGRESS** Coroutines for async data operations.

   - 🧠 **Architecture & Code Structure**:

      - ✅ **DONE** Refactored legacy Java into clean MVVM structure.
      - ✅ **DONE** Modularized repositories, DAOs, entities, and mappers with separation of concerns.
      - 🟩 **IN PROGRESS** Manual dependency injection via AppContainer.

   - 🚀 **Performance and responsiveness**:
   
      - ❌ **NOT IMPLEMENTED** Optimize UI scrolling and animations.

   - 🧪 **Testing & Quality Assurance** :

      - ❌ **NOT IMPLEMENTED** Unit test for dollar to euro conversion.
      - ❌ **NOT IMPLEMENTED** Unit test for euro to dollar conversion.
      - ❌ **NOT IMPLEMENTED** Unit test for date formatting.
      - ❌ **NOT IMPLEMENTED** Integration test for network availability.
      - ✅ **DONE** DAO tests using instrumented tests for Room database
      - ✅ **DONE** Repository unit tests using fake DAO architecture and model/entity separation


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
   - **AppContainer (Manual DI)** : Centralized dependency management without external DI framework (e.g., Hilt).
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
   

## 🚀 **How to Use**

❌ **THIS SECTION IS NOT AVAILABLE YET**


## 📸 **Screenshots**

❌ **THIS SECTION IS NOT AVAILABLE YET**


## 🤝 **Contributions**
Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.