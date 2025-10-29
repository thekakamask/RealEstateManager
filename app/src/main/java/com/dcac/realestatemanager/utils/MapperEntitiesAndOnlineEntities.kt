package com.dcac.realestatemanager.utils

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity

fun UserEntity.toOnlineEntity(): UserOnlineEntity {
    return UserOnlineEntity(
        email = this.email,
        agentName = this.agentName,
        updatedAt = updatedAt,
        roomId = this.id,
    )
}

fun UserOnlineEntity.toEntity(userId: Long, firebaseUid: String): UserEntity {
    return UserEntity(
        id = userId,
        email = this.email,
        agentName = this.agentName,
        isSynced = true,
        firebaseUid = firebaseUid,
        updatedAt = this.updatedAt,
        isDeleted = false
    )
}

fun PhotoEntity.toOnlineEntity(): PhotoOnlineEntity {
    return PhotoOnlineEntity(
        description = this.description,
        propertyId = this.propertyId,
        updatedAt = this.updatedAt,
        roomId = this.id
    )
}

fun PhotoOnlineEntity.toEntity(photoId : Long): PhotoEntity {
    return PhotoEntity(
        id = photoId,
        propertyId = this.propertyId,
        description = this.description,
        isSynced = true,
        updatedAt = this.updatedAt,
        isDeleted = false
    )
}

fun PoiEntity.toOnlineEntity(): PoiOnlineEntity {
    return PoiOnlineEntity(
        name = this.name,
        type = this.type,
        updatedAt = this.updatedAt,
        roomId = this.id
    )
}

fun PoiOnlineEntity.toEntity(poiId : Long): PoiEntity {
    return PoiEntity(
        id = poiId,
        name = this.name,
        type = this.type,
        isSynced = true,
        updatedAt = this.updatedAt,
        isDeleted = false,
    )
}


fun PropertyEntity.toOnlineEntity(): PropertyOnlineEntity {
    return PropertyOnlineEntity(
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
        userId = this.userId,
        staticMapPath = this.staticMapPath,
        updatedAt = this.updatedAt,
        roomId = this.id
    )
}

fun PropertyOnlineEntity.toEntity(propertyId: Long): PropertyEntity {
    return PropertyEntity(
        id = propertyId,
        title = this.title,
        type = this.type,
        price = this.price,
        surface = this.surface,
        rooms = this.rooms,
        description = this.description,
        address = this.address,
        isSold = this.isSold,
        entryDate = this.entryDate, // déjà String "yyyy-MM-dd"
        saleDate = this.saleDate,
        userId = this.userId,
        staticMapPath = this.staticMapPath,
        isSynced = true,
        updatedAt = this.updatedAt,
        isDeleted = false
    )
}

fun PropertyPoiCrossEntity.toOnlineEntity(): PropertyPoiCrossOnlineEntity {
    return PropertyPoiCrossOnlineEntity(
        propertyId = this.propertyId,
        poiId = this.poiId,
        updatedAt = this.updatedAt,
    )
}

fun PropertyPoiCrossOnlineEntity.toEntity(): PropertyPoiCrossEntity {
    return PropertyPoiCrossEntity(
        propertyId = this.propertyId,
        poiId = this.poiId,
        isSynced = true,
        updatedAt = this.updatedAt,
        isDeleted = false
    )
}