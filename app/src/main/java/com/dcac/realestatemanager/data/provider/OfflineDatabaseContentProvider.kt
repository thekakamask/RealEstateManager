package com.dcac.realestatemanager.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.content.UriMatcher
import com.dcac.realestatemanager.data.offlineDatabase.RealEstateManagerDatabase
import androidx.sqlite.db.SimpleSQLiteQuery

//1️⃣	The ContentProvider is registered in AndroidManifest.xml and bound to a specific authority.
//2️⃣	When another app or component calls ContentResolver.query(), the URI is passed to your ContentProvider.
//3️⃣	The UriMatcher identifies which table is being queried.
//4️⃣	The provider builds a raw SQL query using the projection, selection, etc.
//5️⃣	It delegates the query to the corresponding DAO, which returns a Cursor.
//6️⃣	The Cursor is returned to the client (another app or this app other component(outside the process).
//🔒	Insert/update/delete operations are disabled.


// Constants for table names (used in SQL query building)
private const val TABLE_PROPERTIES = "properties"
private const val TABLE_PHOTOS = "photos"
private const val TABLE_POI = "poi"
private const val TABLE_USERS = "users"
private const val TABLE_CROSS_REF = "property_poi_cross_ref"
private const val TABLE_STATIC_MAP = "static_map"


class OfflineDatabaseContentProvider : ContentProvider() {

    companion object {
        // Content authority used to identify this ContentProvider
        const val AUTHORITY = "com.dcac.realestatemanager.provider"

        // Path segments for each type of data exposed
        const val PATH_PROPERTIES = "properties"
        const val PATH_PHOTOS = "photos"
        const val PATH_POI = "poi"
        const val PATH_USERS = "users"
        const val PATH_CROSS_REF = "property_poi_cross_ref"
        const val PATH_STATIC_MAP = "static_map"

        // Integer codes to match URIs
        const val CODE_PROPERTIES = 1
        const val CODE_PHOTOS = 2
        const val CODE_POI = 3
        const val CODE_USERS = 4
        const val CODE_CROSS_REF = 5
        const val CODE_STATIC_MAP = 6
    }

    // Reference to the Room database (set in onCreate)
    private lateinit var database: RealEstateManagerDatabase

    // UriMatcher maps a URI to its corresponding integer code
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, PATH_PROPERTIES, CODE_PROPERTIES)
        addURI(AUTHORITY, PATH_PHOTOS, CODE_PHOTOS)
        addURI(AUTHORITY, PATH_POI, CODE_POI)
        addURI(AUTHORITY, PATH_USERS, CODE_USERS)
        addURI(AUTHORITY, PATH_CROSS_REF, CODE_CROSS_REF)
        addURI(AUTHORITY, PATH_STATIC_MAP, CODE_STATIC_MAP)
    }

    // Called when the ContentProvider is
    // first created (at app start or when queried externally)
    override fun onCreate(): Boolean {
        context?.let {
            // Retrieve the singleton instance of the Room database
            database = RealEstateManagerDatabase.getDatabase(it.applicationContext)
            return true
        }
        return false
    }

    // Handles query operations (read-only access)
    override fun query(
        uri: Uri,
        projection: Array<out String>?,      // Columns to return
        selection: String?,                 // WHERE clause (without 'WHERE')
        selectionArgs: Array<out String>?, // Arguments for WHERE clause
        sortOrder: String?                // ORDER BY clause
    ): Cursor? {
        // Match the URI to get the corresponding code (e.g., CODE_PROPERTIES)
        val uriCode = uriMatcher.match(uri)

        // Build the actual SQL query (SELECT ... FROM table WHERE ... ORDER BY ...)
        val query = buildQuery(uriCode, projection, selection, selectionArgs, sortOrder) // Construct SQL

        // Call the corresponding DAO method that returns a Cursor for the matched table
        return when (uriCode) {
            CODE_PROPERTIES -> database.propertyDao().getAllPropertiesAsCursor(query)
            CODE_PHOTOS -> database.photoDao().getAllPhotosAsCursor(query)
            CODE_POI -> database.poiDao().getAllPoiSAsCursor(query)
            CODE_USERS -> database.userDao().getAllUsersAsCursor(query)
            CODE_CROSS_REF -> database.propertyCrossDao().getAllCrossRefsAsCursor(query)
            CODE_STATIC_MAP -> database.staticMapDao().getAllStaticMapAsCursor(query)
            else -> null   // If the URI does not match any known paths
        }?.apply {
            // Notifies observers (e.g., content observers) that the URI is being observed
            setNotificationUri(context?.contentResolver, uri)
        }
    }

    // Returns the MIME type of data for the given URI (useful for clients)
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_PROPERTIES -> "vnd.android.cursor.dir/$PATH_PROPERTIES"
            CODE_PHOTOS -> "vnd.android.cursor.dir/$PATH_PHOTOS"
            CODE_POI -> "vnd.android.cursor.dir/$PATH_POI"
            CODE_USERS -> "vnd.android.cursor.dir/$PATH_USERS"
            CODE_CROSS_REF -> "vnd.android.cursor.dir/$PATH_CROSS_REF"
            CODE_STATIC_MAP -> "vnd.android.cursor.dir/$PATH_STATIC_MAP"
            else -> null
        }
    }

    // Insert, update, delete are not supported in your use case (read-only access)
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    // Builds a raw SQL SELECT query dynamically
    private fun buildQuery(
        uriCode: Int,
        projection: Array<out String>?,     // Columns to return
        selection: String?,                 // WHERE clause
        selectionArgs: Array<out String>?,  // Args for the WHERE clause
        sortOrder: String?                  // ORDER BY clause
    ): SimpleSQLiteQuery {
        // Map the URI code to the actual table name
        val table = when (uriCode) {
            CODE_PROPERTIES -> TABLE_PROPERTIES
            CODE_PHOTOS -> TABLE_PHOTOS
            CODE_POI -> TABLE_POI
            CODE_USERS -> TABLE_USERS
            CODE_CROSS_REF -> TABLE_CROSS_REF
            CODE_STATIC_MAP -> TABLE_STATIC_MAP
            else -> throw IllegalArgumentException("Unknown URI code: $uriCode")
        }

        // SELECT clause
        val columns = projection?.joinToString(", ") ?: "*"

        // WHERE clause (if provided)
        val whereClause = if (!selection.isNullOrBlank()) " WHERE $selection" else ""

        // ORDER BY clause (if provided)
        val orderClause = if (!sortOrder.isNullOrBlank()) " ORDER BY $sortOrder" else ""

        // Combine into a full SQL query string
        return SimpleSQLiteQuery("SELECT $columns FROM $table$whereClause$orderClause", selectionArgs)
    }
}
