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


fun PropertyEntity.toModel(photos: List<Photo> = emptyList(), poiS: List<Poi> = emptyList()): Property {
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
        photos = photos,
        poiS = poiS
    )
}

fun PropertyEntity.toFullModel(
    photos: List<Photo>,
    crossRefs: List<PropertyPoiCross>,
    allPoiS: List<Poi>
): Property {
    val propertyPhotos = photos.filter { it.propertyId == this.id }
    val poiIds = crossRefs.filter { it.propertyId == this.id }.map { it.poiId }
    val propertyPoiS = allPoiS.filter { it.id in poiIds }
    return this.toModel(propertyPhotos, propertyPoiS)
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
        userId = 0L,
        staticMapPath = staticMapPath
    )
}

fun PhotoEntity.toModel(): Photo = Photo(
    id = id,
    propertyId = propertyId,
    uri = uri,
    description = description,
)

fun Photo.toEntity(): PhotoEntity = PhotoEntity(
    id = id,
    propertyId = propertyId,
    uri = uri,
    description = description
)

fun PoiEntity.toModel(): Poi = Poi(
    id = id,
    name = name,
    type = type
)

fun Poi.toEntity(): PoiEntity = PoiEntity(
    id = id,
    name = name,
    type = type
)

fun UserEntity.toModel(): User = User(
    id = id,
    email = email,
    password = password,
    agentName = agentName,
    isSynced = isSynced
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    password = password,
    agentName = agentName,
    isSynced = isSynced
)

fun PropertyPoiCrossEntity.toModel(): PropertyPoiCross = PropertyPoiCross(
    propertyId = propertyId,
    poiId = poiId
)

fun PropertyPoiCross.toEntity(): PropertyPoiCrossEntity = PropertyPoiCrossEntity(
    propertyId = propertyId,
    poiId = poiId
)

fun PoiWithPropertiesRelation.toModel(): PoiWithProperties {
    return PoiWithProperties(
        poi = poi.toModel(),
        properties = properties.map { it.toModel() }
    )
}

fun PropertyWithPoiSRelation.toModel(): PropertyWithPoiS {
    return PropertyWithPoiS(
        property = property.toModel(),
        poiS = poiS.map { it.toModel() }
    )
}

