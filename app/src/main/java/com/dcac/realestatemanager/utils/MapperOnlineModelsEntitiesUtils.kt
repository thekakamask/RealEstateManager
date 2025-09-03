package com.dcac.realestatemanager.utils

import android.util.Log
import com.dcac.realestatemanager.data.onlineDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.onlineDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.onlineDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.onlineDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyPoiCross
import com.dcac.realestatemanager.model.User
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

fun User.toOnlineEntity(): UserOnlineEntity {
    return UserOnlineEntity(
        email = this.email,
        agentName = this.agentName,
        updatedAt = updatedAt
    )
}

fun UserOnlineEntity.toModel(firebaseUid: String, userId: Long = 0L): User {
    Log.d("Mapping", "Deserialized UserOnlineEntity: $this")
    return User(
        id = userId,
        email = this.email,
        agentName = this.agentName,
        isSynced = true,
        firebaseUid = firebaseUid,
        updatedAt = updatedAt
    )
}

fun Photo.toOnlineEntity(): PhotoOnlineEntity {
    return PhotoOnlineEntity(
        uri = "",
        description = this.description,
        propertyId = this.propertyId,
        updatedAt = this.updatedAt,
        storageUrl = this.storageUrl
    )
}


fun PhotoOnlineEntity.toModel(photoId : Long): Photo {
    Log.d("Mapping", "Deserialized PhotoOnlineEntity: $this")
    return Photo(
        id = photoId,
        propertyId = this.propertyId,
        uri = "",
        storageUrl = this.storageUrl,
        description = this.description,
        isSynced = true,
        updatedAt = this.updatedAt
    )
}


fun PoiOnlineEntity.toModel(poiId : Long): Poi {
    Log.d("Mapping", "Deserialized PoiOnlineEntity: $this")
    return Poi(
        id = poiId,
        name = this.name,
        type = this.type,
        isSynced = true,
        updatedAt = updatedAt
    )
}

fun Poi.toOnlineEntity(): PoiOnlineEntity {
    return PoiOnlineEntity(
        name = this.name,
        type = this.type,
        updatedAt = updatedAt
    )
}

fun PropertyOnlineEntity.toModel(propertyId: Long, user: User): Property {
    Log.d("Mapping", "Deserialized PropertyOnlineEntity: $this")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return Property(
        id = propertyId,
        title = title,
        type = type,
        price = price,
        surface = surface,
        rooms = rooms,
        description = description,
        address = address,
        isSold = isSold,
        entryDate = LocalDate.parse(entryDate, formatter),
        saleDate = saleDate?.let { LocalDate.parse(it, formatter) },
        staticMapPath = staticMapPath,
        user = user,
        photos = emptyList(),
        poiS = emptyList(),
        isSynced = true,
        updatedAt = updatedAt
    )
}

fun Property.toOnlineEntity(): PropertyOnlineEntity {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return PropertyOnlineEntity(
        title = title,
        type = type,
        price = price,
        surface = surface,
        rooms = rooms,
        description = description,
        address = address,
        isSold = isSold,
        entryDate = entryDate.format(formatter),
        saleDate = saleDate?.format(formatter),
        userId = user.id,
        staticMapPath = staticMapPath,
        updatedAt = updatedAt
    )
}

fun PropertyPoiCross.toOnlineEntity(): PropertyPoiCrossOnlineEntity {
    return PropertyPoiCrossOnlineEntity(
        propertyId = this.propertyId,
        poiId = this.poiId,
        updatedAt = updatedAt
    )
}

fun PropertyPoiCrossOnlineEntity.toModel(): PropertyPoiCross {
    Log.d("Mapping", "Deserialized PropertyPoiCrossOnlineEntity: $this")
    return PropertyPoiCross(
        propertyId = this.propertyId,
        poiId = this.poiId,
        isSynced = true,
        updatedAt = updatedAt
    )
}