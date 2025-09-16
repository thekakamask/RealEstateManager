package com.dcac.realestatemanager.data.firebaseDatabase.user

import androidx.annotation.Keep

/**
 * @Keep ensures that this data class and its fields are not removed or renamed by R8/ProGuard.
 * This is crucial for Firestore deserialization at runtime, which uses reflection based on field names.
 */

@Keep
data class UserOnlineEntity(
    val email: String = "",
    val agentName: String = "",
    val firebaseUid: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val roomId : Long = 0L
)
