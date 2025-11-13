package com.dcac.realestatemanager.data.offlineDatabase.property

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity

data class PropertyWithDetails(
    @Embedded
    val property: PropertyEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: UserEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "property_id"
    )
    val photos: List<PhotoEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PropertyPoiCrossEntity::class,
            parentColumn = "property_id",
            entityColumn = "poi_id"
        )
    )
    val pois: List<PoiEntity>
)
