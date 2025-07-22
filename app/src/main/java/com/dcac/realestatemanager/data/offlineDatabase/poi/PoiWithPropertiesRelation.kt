package com.dcac.realestatemanager.data.offlineDatabase.poi

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity

data class PoiWithPropertiesRelation(
    @Embedded val poi: PoiEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PropertyPoiCrossEntity::class,
            parentColumn= "poiId",
            entityColumn= "propertyId"
        )
    )
    val properties: List<PropertyEntity>
)
