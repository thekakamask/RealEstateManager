package com.dcac.realestatemanager.utils

import com.dcac.realestatemanager.data.offlinedatabase.photo.PhotoEntity
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlinedatabase.property.PropertyEntity
import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
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
        agentName = agentName,
        staticMapPath = staticMapPath,
        photos = photos,
        poiS = poiS
    )
}

fun PhotoEntity.toModel(): Photo = Photo(
    id=id,
    propertyId = propertyId,
    uri = uri,
    description = description,
)

fun PoiEntity.toModel(): Poi = Poi(
    id = id,
    propertyId = propertyId,
    name = name,
    type = type
)