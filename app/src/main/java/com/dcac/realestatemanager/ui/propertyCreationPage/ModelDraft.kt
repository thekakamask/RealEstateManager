package com.dcac.realestatemanager.ui.propertyCreationPage

import com.dcac.realestatemanager.model.Photo

data class PropertyDraft(
    //var universalLocalUserId: String = "",
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
    val staticMapPath: String? = null,
    //var isSold: Boolean = false,
    //var entryDate: LocalDate = LocalDate.now(),
    //var saleDate: LocalDate? = null,
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
