package com.dcac.realestatemanager.data.onlineDatabase.photo

import androidx.annotation.Keep

/**
 * @Keep ensures that this data class and its fields are not removed or renamed by R8/ProGuard.
 * This is crucial for Firestore deserialization at runtime, which uses reflection based on field names.
 */

@Keep
data class PhotoOnlineEntity(
    val uri: String = "",
    val description: String = "",
    val propertyId: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis(),
    val storageUrl: String = "" // ✅ link to firestore

)