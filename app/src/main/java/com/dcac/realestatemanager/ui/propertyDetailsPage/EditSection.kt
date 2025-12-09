package com.dcac.realestatemanager.ui.propertyDetailsPage

import androidx.annotation.StringRes
import com.dcac.realestatemanager.R


enum class EditSection(@StringRes val labelRes: Int) {
    TYPE(R.string.edit_section_property_type),
    ADDRESS(R.string.edit_section_property_address),
    DESCRIPTION(R.string.edit_section_property_description),
    PHOTOS(R.string.edit_section_property_photos),
    POIS(R.string.edit_section_property_poi),
}