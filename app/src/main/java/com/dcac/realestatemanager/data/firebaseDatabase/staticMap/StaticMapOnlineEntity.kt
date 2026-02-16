package com.dcac.realestatemanager.data.firebaseDatabase.staticMap

import com.google.firebase.firestore.PropertyName

data class StaticMapOnlineEntity(
    val ownerUid: String = "",
    val universalLocalId: String = "", // 🔑 UUID from Room
    val universalLocalPropertyId: String = "",  // 🔗 link with property
    val updatedAt: Long = System.currentTimeMillis(),
    val storageUrl: String = "",
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean = false
)
