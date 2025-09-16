package com.dcac.realestatemanager.utils

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiWithPropertiesRelation
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyWithPoiSRelation
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.PoiWithProperties
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyPoiCross
import com.dcac.realestatemanager.model.PropertyWithPoiS
import com.dcac.realestatemanager.model.User
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

// ------------------ Property ------------------

fun PropertyEntity.toModel(
    user: User,
    photos: List<Photo> = emptyList(),
    poiS: List<Poi> = emptyList()
): Property {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val entryDateLocal = LocalDate.parse(entryDate, formatter)
    val saleDateLocal = saleDate?.let { LocalDate.parse(it, formatter) }

    return Property(
        id = id,
        title = title,
        type = type,
        price = price,
        surface = surface,
        rooms = rooms,
        description = description,
        address = address,
        isSold = isSold,
        entryDate = entryDateLocal,
        saleDate = saleDateLocal,
        staticMapPath = staticMapPath,
        user = user,
        photos = photos,
        poiS = poiS,
        isSynced = isSynced,
        updatedAt = updatedAt
    )
}

fun PropertyEntity.toFullModel(
    allUsers: List<User>,
    photos: List<Photo>,
    crossRefs: List<PropertyPoiCross>,
    allPoiS: List<Poi>
): Property {
    val propertyPhotos = photos.filter { it.propertyId == this.id }
    val poiIds = crossRefs.filter { it.propertyId == this.id }.map { it.poiId }
    val propertyPoiS = allPoiS.filter { it.id in poiIds }
    val user = allUsers.first { it.id == this.userId }
    return this.toModel(user, propertyPhotos, propertyPoiS)
}

fun Property.toEntity(): PropertyEntity {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val entryDateString = entryDate.format(formatter)
    val saleDateString = saleDate?.format(formatter)
    return PropertyEntity(
        id = id,
        title = title,
        type = type,
        price = price,
        surface = surface,
        rooms = rooms,
        description = description,
        address = address,
        isSold = isSold,
        entryDate = entryDateString,
        saleDate = saleDateString,
        userId = user.id,
        staticMapPath = staticMapPath,
        isSynced = isSynced,
        updatedAt = updatedAt
    )
}

// ------------------ Photo ------------------

fun PhotoEntity.toModel(): Photo = Photo(
    id = id,
    propertyId = propertyId,
    uri = uri,
    description = description,
    isSynced = isSynced,
    updatedAt = updatedAt
)

fun Photo.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    propertyId = propertyId,
    uri = uri,
    description = description,
    isSynced = isSynced,
    updatedAt = updatedAt
)

// ------------------ POI ------------------

fun PoiEntity.toModel(): Poi = Poi(
    id = id,
    name = name,
    type = type,
    isSynced = isSynced,
    updatedAt = updatedAt
)

fun Poi.toEntity(): PoiEntity = PoiEntity(
    id = id,
    name = name,
    type = type,
    isSynced = isSynced,
    updatedAt = updatedAt
)

// ------------------ User ------------------

fun UserEntity.toModel(): User = User(
    id = id,
    email = email,
    agentName = agentName,
    isSynced = isSynced,
    firebaseUid = firebaseUid,
    updatedAt = updatedAt
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    agentName = agentName,
    isSynced = isSynced,
    firebaseUid = firebaseUid,
    updatedAt = updatedAt
)

// ------------------ CrossRef ------------------

fun PropertyPoiCrossEntity.toModel(): PropertyPoiCross = PropertyPoiCross(
    propertyId = propertyId,
    poiId = poiId,
    isSynced = isSynced,
    updatedAt = updatedAt
)

fun PropertyPoiCross.toEntity(): PropertyPoiCrossEntity = PropertyPoiCrossEntity(
    propertyId = propertyId,
    poiId = poiId,
    isSynced = isSynced,
    updatedAt = updatedAt
)

// ------------------ Relations ------------------

fun PoiWithPropertiesRelation.toModel(allUsers: List<User>): PoiWithProperties {
    return PoiWithProperties(
        poi = poi.toModel(),
        properties = properties.map { property ->
            val user = allUsers.first { it.id == property.userId }
            property.toModel(user = user)
        }
    )
}

fun PropertyWithPoiSRelation.toModel(allUsers: List<User>): PropertyWithPoiS {
    val user = allUsers.first { it.id == property.userId }
    return PropertyWithPoiS(
        property = property.toModel(user = user),
        poiS = poiS.map { it.toModel() }
    )
}

