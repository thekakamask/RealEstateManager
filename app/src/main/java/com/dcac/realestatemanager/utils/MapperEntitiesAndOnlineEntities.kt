package com.dcac.realestatemanager.utils

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapEntity

fun UserEntity.toOnlineEntity(): UserOnlineEntity {
    return UserOnlineEntity(
        email = this.email,
        agentName = this.agentName,
        updatedAt = this.updatedAt,
        universalLocalId = this.id // ✅ Same UUID
    )
}

fun UserOnlineEntity.toEntity(
    firebaseUid: String
): UserEntity {
    return UserEntity(
        id = this.universalLocalId, // ✅ UUID receive from firestore
        email = this.email,
        agentName = this.agentName,
        isSynced = true,
        firebaseUid = firebaseUid,
        updatedAt = this.updatedAt,
        isDeleted = false
    )
}

fun PhotoEntity.toOnlineEntity(ownerUid: String): PhotoOnlineEntity {
    return PhotoOnlineEntity(
        ownerUid = ownerUid,
        universalLocalId = this.id,
        universalLocalPropertyId = this.universalLocalPropertyId,
        description = this.description,
        updatedAt = this.updatedAt,
        storageUrl = this.uri
    )
}


fun PhotoOnlineEntity.toEntity(
    firestoreId: String
): PhotoEntity {
    return PhotoEntity(
        id = universalLocalId,
        firestoreDocumentId = firestoreId,
        universalLocalPropertyId = universalLocalPropertyId,
        uri = storageUrl,
        description = description,
        isSynced = true,
        updatedAt = updatedAt,
        isDeleted = false
    )
}

fun PoiEntity.toOnlineEntity(ownerUid: String): PoiOnlineEntity {
    return PoiOnlineEntity(
        ownerUid = ownerUid,
        universalLocalId = this.id,
        name = this.name,
        type = this.type,
        address = this.address,
        latitude = this.latitude,
        longitude = this.longitude,
        updatedAt = this.updatedAt
    )
}


fun PoiOnlineEntity.toEntity(
    firestoreId: String?,
): PoiEntity {
    return PoiEntity(
        id = universalLocalId,
        firestoreDocumentId = firestoreId,
        name = this.name,
        type = this.type,
        address= this.address,
        latitude = this.latitude,
        longitude = this.longitude,
        isSynced = true,
        updatedAt = this.updatedAt,
        isDeleted = false,
    )
}


fun PropertyEntity.toOnlineEntity(ownerUid: String): PropertyOnlineEntity {
    return PropertyOnlineEntity(
        ownerUid = ownerUid,
        universalLocalId = this.id,
        universalLocalUserId = this.universalLocalUserId,
        title = this.title,
        type = this.type,
        price = this.price,
        surface = this.surface,
        rooms = this.rooms,
        description = this.description,
        address = this.address,
        isSold = this.isSold,
        entryDate = this.entryDate,
        saleDate = this.saleDate,
        staticMapPath = this.staticMapPath,
        updatedAt = this.updatedAt
    )
}


fun PropertyOnlineEntity.toEntity(
    firestoreId: String?,
): PropertyEntity {
    return PropertyEntity(
        id = universalLocalId,
        firestoreDocumentId = firestoreId,
        universalLocalUserId = universalLocalUserId,
        title = title,
        type = type,
        price = price,
        surface = surface,
        rooms = rooms,
        description = description,
        address = address,
        isSold = isSold,
        entryDate = entryDate,
        saleDate = saleDate,
        staticMapPath = staticMapPath,
        isSynced = true,
        isDeleted = false,
        updatedAt = updatedAt
    )
}

fun PropertyPoiCrossEntity.toOnlineEntity(ownerUid: String): PropertyPoiCrossOnlineEntity {
    return PropertyPoiCrossOnlineEntity(
        ownerUid = ownerUid,
        universalLocalPropertyId = this.universalLocalPropertyId,
        universalLocalPoiId = this.universalLocalPoiId,
        updatedAt = this.updatedAt
    )
}


fun PropertyPoiCrossOnlineEntity.toEntity(
    firestoreId: String?,
): PropertyPoiCrossEntity {
    return PropertyPoiCrossEntity(
        universalLocalPropertyId = universalLocalPropertyId,
        universalLocalPoiId = universalLocalPoiId,
        firestoreDocumentId = firestoreId,
        isSynced = true,
        updatedAt = this.updatedAt,
        isDeleted = false
    )
}

fun StaticMapEntity.toOnlineEntity(ownerUid: String): StaticMapOnlineEntity {
    return StaticMapOnlineEntity(
        ownerUid = ownerUid,
        universalLocalId = this.id,
        universalLocalPropertyId = this.universalLocalPropertyId,
        updatedAt = this.updatedAt,
        storageUrl = this.uri
    )
}

fun StaticMapOnlineEntity.toEntity(
    firestoreId: String?,
): StaticMapEntity {
    return StaticMapEntity(
        id = universalLocalId,
        firestoreDocumentId = firestoreId,
        universalLocalPropertyId = universalLocalPropertyId,
        uri = storageUrl,
        isSynced = true,
        updatedAt = updatedAt,
        isDeleted = false
    )
}