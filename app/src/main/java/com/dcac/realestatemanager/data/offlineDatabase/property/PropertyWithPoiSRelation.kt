package com.dcac.realestatemanager.data.offlineDatabase.property

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity

data class PropertyWithPoiSRelation(
    @Embedded val property: PropertyEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PropertyPoiCrossEntity::class,
            parentColumn = "propertyId",
            entityColumn = "poiId"
        )
    )
    val poiS: List<PoiEntity>
)
