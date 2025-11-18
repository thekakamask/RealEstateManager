package com.dcac.realestatemanager.ui.propertyCreationPage

sealed class PropertyCreationStep {
    data object Intro : PropertyCreationStep()
    data object PropertyType : PropertyCreationStep()
    data object Address : PropertyCreationStep()
    data object PoiS : PropertyCreationStep()
    data object Description : PropertyCreationStep()
    data object Photos : PropertyCreationStep()
    data object StaticMap : PropertyCreationStep()
    data object Confirmation : PropertyCreationStep()
}