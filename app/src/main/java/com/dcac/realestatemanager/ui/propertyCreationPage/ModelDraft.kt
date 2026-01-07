package com.dcac.realestatemanager.ui.propertyCreationPage

import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.model.StaticMap
import org.threeten.bp.LocalDate

data class PropertyDraft(
    var title: String = "",
    var type: String = "",
    var price: Int = 0,
    var surface: Int = 0,
    var rooms: Int = 0,
    var description: String = "",
    var street: String = "",
    var postalCode: String = "",
    var city: String = "",
    var country: String = "",
    val staticMap: StaticMap? = null,
    var isSold: Boolean = false,
    var saleDate: LocalDate? = null,
    var photos: List<Photo> = emptyList(),
    var poiS: List<PoiDraft> = emptyList()
)

data class PoiDraft(
    var name: String = "",
    var type: String = "",
    var street: String ="",
    var postalCode: String = "",
    var city: String = "",
    var country: String= ""
)

data class ParsedAddress(
    val street: String,
    val postalCode: String,
    val city: String,
    val country: String
)

fun parseAddress(fullAddress: String): ParsedAddress {
    val parts = fullAddress.split(",").map { it.trim() }

    val street = parts.getOrNull(0) ?: ""
    val cityAndPostal = parts.getOrNull(1)?.split(" ") ?: listOf()
    val postalCode = cityAndPostal.firstOrNull() ?: ""
    val city = cityAndPostal.drop(1).joinToString(" ")
    val country = parts.getOrNull(2) ?: ""

    return ParsedAddress(
        street = street,
        postalCode = postalCode,
        city = city,
        country = country
    )
}
