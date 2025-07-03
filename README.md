# 🏠 **RealEstateManager**
**RealEstateManager** is a modern Android application aimed at helping real estate agents manage exceptional property listings from their mobile device. Built to support a digital transformation for a prestigious New York agency, the app allows agents to browse, add, edit, and geolocate real estate properties even in offline mode.
This project is developed using modern Android architecture principles, with a focus on local data persistence, modular code, and responsive design. It evolves from a legacy intern prototype and will be gradually refactored and extended.


## ✅ **LAST MAJOR UPDATES (see [UPDATES.md](./UPDATES.md) for details)**

   - 📍 Added runtime location permission request to enable user geolocation features.
   - 📦 Added Google Play Services dependencies for Maps and Location with coroutine support.
   - 🗺️ Integrated dynamic retrieval of user location, properties, and POIs using Google Maps SDK and Location Services APIs.
   - 🔄 Implemented GoogleMapRepository to manage geolocation data and property/POI streams from Room.
   - 💉 Extended AppContainer to inject GoogleMapRepository with offline repositories and location client dependencies.


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

   - 📷 **Media Management** :

      - ❌ **NOT IMPLEMENTED** Take or select photos from gallery.
      - ❌ **NOT IMPLEMENTED** Add multiple images per listing.

   - 🔍 **Search** :

      - ❌ **NOT IMPLEMENTED** Multi-criteria search (surface, price, type, POI, dates, sold status).
      - ❌ **NOT IMPLEMENTED** Sort and filter property results.

   - 💾 **Offline Mode** :

      - ✅ **DONE** Local data persistence via Room (SQLite).
      - 🟩 **IN PROGRESS** Full offline functionality for all screens.

   - 🧠 **Utilities** :
      - ✅ **DONE** Convert dollar to euro.
      - ❌ **NOT IMPLEMENTED** Convert euro to dollar.
      - ✅ **DONE** Get today's date (format to be improved).
      - ❌ **NOT IMPLEMENTED** Better network availability check.
      - ❌ **NOT IMPLEMENTED** Add unit and integration tests.

   - 🧭 **Navigation** :

      - ❌ **NOT IMPLEMENTED** Navigation between list and detail.
      - ❌ **NOT IMPLEMENTED** Two-pane mode support for tablets.

   - 🎨 Modern and Fluid Interface:

      - ❌ **NOT IMPLEMENTED** Follows Material Design 3 guidelines.
      - ❌ **NOT IMPLEMENTED** Smooth transitions with Navigation Component.
      - ❌ **NOT IMPLEMENTED** Responsive layout with adaptive UI.

      - TopBar:
         - ❌ **NOT IMPLEMENTED** Display application title and possible future actions.

      - Light/Dark Mode:
         - ✅ **DONE** Supports light/dark mode.

      - Custom theme:
         - ✅ **DONE** Implemented custom colors and shapes.
         - ✅ **DONE** Implemented custom Google Fonts.

   - 🔄 Real-time status management:

      - ❌ **NOT IMPLEMENTED** Use of StateFlow for UI state handling.
      - ❌ **NOT IMPLEMENTED** ViewModel for lifecycle-aware logic.
      - 🟩 **IN PROGRESS** Coroutines for async data operations.

   - 🧠 Architecture & Code Structure:

      - ✅ **DONE** Refactor legacy Java to MVVM.
      - 🟩 **IN PROGRESS** Introduce ViewModel, LiveData / StateFlow, DAO.

   - 🚀 Performance and responsiveness:
   
      - ❌ **NOT IMPLEMENTED** Optimize UI scrolling and animations.

   - 🧪 **Testing & Quality Assurance** :

      - ❌ **NOT IMPLEMENTED** Unit test for dollar to euro conversion.
      - ❌ **NOT IMPLEMENTED** Unit test for euro to dollar conversion.
      - ❌ **NOT IMPLEMENTED** Unit test for date formatting.
      - ❌ **NOT IMPLEMENTED** Integration test for network availability.
      - ❌ **NOT IMPLEMENTED** DAO and Repository unit tests.


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
   

## 🚀 **How to Use**

❌ **THIS SECTION IS NOT AVAILABLE YET**


## 📸 **Screenshots**

❌ **THIS SECTION IS NOT AVAILABLE YET**


## 🤝 **Contributions**
Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.