package com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

/**
 * @Keep ensures that this data class and its fields are not removed or renamed by R8/ProGuard.
 * This is crucial for Firestore deserialization at runtime, which uses reflection based on field names.
 */

@Keep
data class PropertyPoiCrossOnlineEntity(
    val ownerUid: String = "",
    val universalLocalPropertyId: String = "", // 🔗 UUID of the property
    val universalLocalPoiId: String = "",      // 🔗 UUID of the poi
    val updatedAt: Long = System.currentTimeMillis(),
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean = false
)
