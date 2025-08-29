package com.dcac.realestatemanager.data.onlineDatabase.property

import androidx.annotation.Keep

/**
 * @Keep ensures that this data class and its fields are not removed or renamed by R8/ProGuard.
 * This is crucial for Firestore deserialization at runtime, which uses reflection based on field names.
 */

@Keep
data class PropertyOnlineEntity(
    val title: String = "",
    val type: String = "",
    val price: Int = 0,
    val surface: Int = 0,
    val rooms: Int = 0,
    val description: String = "",
    val address: String = "",
    val isSold: Boolean = false,
    val entryDate: String = "",       // format "yyyy-MM-dd"
    val saleDate: String? = null,     // nullable
    val userId: Long = 0L,            // same id than Room
    val staticMapPath: String? = null
)
