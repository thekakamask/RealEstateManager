package com.dcac.realestatemanager.data.firebaseDatabase.staticMap

data class StaticMapOnlineEntity(
    val ownerUid: String = "",
    val universalLocalId: String = "", // 🔑 UUID from Room
    val universalLocalPropertyId: String = "",  // 🔗 link with property
    val updatedAt: Long = System.currentTimeMillis(),
    val storageUrl: String = "",
    val isDeleted: Boolean = false
)
