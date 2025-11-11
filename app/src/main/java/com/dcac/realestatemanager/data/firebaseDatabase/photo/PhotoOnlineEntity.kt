package com.dcac.realestatemanager.data.firebaseDatabase.photo

import androidx.annotation.Keep

/**
 * @Keep ensures that this data class and its fields are not removed or renamed by R8/ProGuard.
 * This is crucial for Firestore deserialization at runtime, which uses reflection based on field names.
 */

@Keep
data class PhotoOnlineEntity(
    val universalLocalId: String = "", // 🔑 UUID from Room
    val universalLocalPropertyId: String = "",  // 🔗 link with property
    val description: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val storageUrl: String = ""
)
