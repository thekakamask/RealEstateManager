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

//PROPERTY
fun PropertyEntity.toModel(
    photos: List<Photo> = emptyList(),
    poiS: List<Poi> = emptyList()
): Property {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val entryDateLocal = LocalDate.parse(entryDate, formatter)
    val saleDateLocal = saleDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it, formatter) }

    return Property(
        universalLocalId = id,
        firestoreDocumentId = firestoreDocumentId,
        universalLocalUserId = universalLocalUserId,
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
        poiS = poiS,
        isSynced = isSynced,
        isDeleted = isDeleted,
        updatedAt = updatedAt
    )
}

//FULL MODEL
fun PropertyEntity.toFullModel(
    allUsers: List<User>,
    photos: List<Photo>,
    crossRefs: List<PropertyPoiCross>,
    allPoiS: List<Poi>
): Property? {
    val userExists = allUsers.any { it.universalLocalId == this.universalLocalUserId }
    if (!userExists) return null

    val propertyPhotos = photos.filter { it.universalLocalPropertyId == this.id }
    val poiIds = crossRefs.filter { it.universalLocalPropertyId == this.id }.map { it.universalLocalPoiId }
    val propertyPois = allPoiS.filter { it.universalLocalId in poiIds }

    return this.toModel(propertyPhotos, propertyPois)
}

fun Property.toEntity(): PropertyEntity {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return PropertyEntity(
        id = universalLocalId,
        firestoreDocumentId = firestoreDocumentId,
        universalLocalUserId = universalLocalUserId,
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
        staticMapPath = staticMapPath,
        isSynced = isSynced,
        isDeleted = isDeleted,
        updatedAt = updatedAt
    )
}

//PHOTO

fun PhotoEntity.toModel(): Photo = Photo(
    universalLocalId = id,
    firestoreDocumentId = firestoreDocumentId,
    universalLocalPropertyId = universalLocalPropertyId,
    uri = uri,
    description = description,
    isDeleted = isDeleted,
    isSynced = isSynced,
    updatedAt = updatedAt
)

fun Photo.toEntity(): PhotoEntity = PhotoEntity(
    id = universalLocalId,
    firestoreDocumentId = firestoreDocumentId,
    universalLocalPropertyId = universalLocalPropertyId,
    uri = uri,
    description = description,
    isDeleted = isDeleted,
    isSynced = isSynced,
    updatedAt = updatedAt
)

//POI

fun PoiEntity.toModel(): Poi = Poi(
    universalLocalId = id,
    firestoreDocumentId = firestoreDocumentId,
    name = name,
    type = type,
    address = address,
    isSynced = isSynced,
    updatedAt = updatedAt
)

fun Poi.toEntity(): PoiEntity = PoiEntity(
    id = universalLocalId,
    firestoreDocumentId = firestoreDocumentId,
    name = name,
    type = type,
    address = address,
    isSynced = isSynced,
    updatedAt = updatedAt
)

//USER

fun UserEntity.toModel(): User {
    return User(
        universalLocalId = this.id, // ✅ no UUID generation here
        email = this.email,
        agentName = this.agentName,
        firebaseUid = this.firebaseUid,
        isSynced = this.isSynced,
        isDeleted = this.isDeleted,
        updatedAt = this.updatedAt
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = this.universalLocalId, // ✅ stable UUID
        email = this.email,
        agentName = this.agentName,
        isSynced = this.isSynced,
        firebaseUid = this.firebaseUid,
        isDeleted = this.isDeleted,
        updatedAt = this.updatedAt
    )
}

//CROSSREFS

fun PropertyPoiCrossEntity.toModel(): PropertyPoiCross = PropertyPoiCross(
    universalLocalPropertyId = universalLocalPropertyId,
    universalLocalPoiId = universalLocalPoiId,
    isSynced = isSynced,
    updatedAt = updatedAt
)

fun PropertyPoiCross.toEntity(): PropertyPoiCrossEntity = PropertyPoiCrossEntity(
    universalLocalPropertyId = universalLocalPropertyId,
    universalLocalPoiId = universalLocalPoiId,
    isSynced = isSynced,
    updatedAt = updatedAt
)

//RELATIONS

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
