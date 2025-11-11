package com.dcac.realestatemanager.data.firebaseDatabase.property

import androidx.annotation.Keep

/**
 * @Keep ensures that this data class and its fields are not removed or renamed by R8/ProGuard.
 * This is crucial for Firestore deserialization at runtime, which uses reflection based on field names.
 */

@Keep
data class PropertyOnlineEntity(
    val universalLocalId: String = "", // 🔑 UUID from Room
    val universalLocalUserId: String = "",  // 🔗 link with user
    val title: String = "",
    val type: String = "",
    val price: Int = 0,
    val surface: Int = 0,
    val rooms: Int = 0,
    val description: String = "",
    val address: String = "",
    val isSold: Boolean = false,
    val entryDate: String = "",
    val saleDate: String? = null,
    val staticMapPath: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
