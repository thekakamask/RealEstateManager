package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiWithPropertiesRelation
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyWithPoiSRelation
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeStaticMapEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePhotoModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePoiModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyPoiCrossModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeStaticMapModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import org.junit.Assert.assertEquals
import org.junit.Test
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toFullModel
import com.dcac.realestatemanager.utils.toModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class MapperModelsAndEntitiesTest {

    private val photoEntity  = FakePhotoEntity.photo1
    private val poiEntity = FakePoiEntity.poi1
    private val propertyEntity = FakePropertyEntity.property1
    private val crossEntity = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val userEntity = FakeUserEntity.user1
    private val staticMapEntity = FakeStaticMapEntity.staticMap1

    private val photoModel = FakePhotoModel.photo1
    private val poiModel = FakePoiModel.poi1
    private val propertyModel = FakePropertyModel.property1
    private val crossModel = FakePropertyPoiCrossModel.cross1
    private val userModel = FakeUserModel.user1
    private val staticMapModel = FakeStaticMapModel.staticMap1

    @Test
    fun toModel_propertyEntity_mapsCorrectlyToPropertyModel() {

        val model = propertyEntity.toModel(
            photos = listOf(photoModel),
            poiS = listOf(poiModel)
        )

        assertEquals(propertyEntity.id, model.universalLocalId)
        assertEquals(propertyEntity.firestoreDocumentId, model.firestoreDocumentId)
        assertEquals(propertyEntity.universalLocalUserId, model.universalLocalUserId)
        assertEquals(propertyEntity.title, model.title)
        assertEquals(propertyEntity.type, model.type)
        assertEquals(propertyEntity.price, model.price)
        assertEquals(propertyEntity.surface, model.surface)
        assertEquals(propertyEntity.rooms, model.rooms)
        assertEquals(propertyEntity.description, model.description)
        assertEquals(propertyEntity.address, model.address)
        assertEquals(propertyEntity.isSold, model.isSold)
        assertEquals(propertyEntity.isSynced, model.isSynced)
        assertEquals(propertyEntity.isDeleted, model.isDeleted)
        assertEquals(propertyEntity.updatedAt, model.updatedAt)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        assertEquals(LocalDate.parse(propertyEntity.entryDate, formatter), model.entryDate)
        assertEquals(propertyEntity.saleDate?.let { LocalDate.parse(it, formatter) }, model.saleDate)

        assertEquals(listOf(photoModel), model.photos)
        assertEquals(listOf(poiModel), model.poiS)
    }

    @Test
    fun toFullModel_propertyEntity_mapsCorrectlyToFullModel() {
        val fullModel = propertyEntity.toFullModel(
            allUsers = listOf(userModel),
            photos = listOf(photoModel),
            crossRefs = listOf(crossModel),
            allPoiS = listOf(poiModel),
            staticMap = staticMapModel
        )

        requireNotNull(fullModel)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        assertEquals(
            LocalDate.parse(propertyEntity.entryDate, formatter),
            fullModel.entryDate
        )
        assertEquals(
            propertyEntity.saleDate?.let { LocalDate.parse(it, formatter) },
            fullModel.saleDate
        )

        assertEquals(propertyEntity.id, fullModel.universalLocalId)
        assertEquals(propertyEntity.firestoreDocumentId, fullModel.firestoreDocumentId)
        assertEquals(propertyEntity.universalLocalUserId, fullModel.universalLocalUserId)
        assertEquals(propertyEntity.title, fullModel.title)
        assertEquals(propertyEntity.type, fullModel.type)
        assertEquals(propertyEntity.price, fullModel.price)
        assertEquals(propertyEntity.surface, fullModel.surface)
        assertEquals(propertyEntity.rooms, fullModel.rooms)
        assertEquals(propertyEntity.description, fullModel.description)
        assertEquals(propertyEntity.address, fullModel.address)
        assertEquals(propertyEntity.isSold, fullModel.isSold)
        assertEquals(propertyEntity.isSynced, fullModel.isSynced)
        assertEquals(propertyEntity.isDeleted, fullModel.isDeleted)
        assertEquals(propertyEntity.updatedAt, fullModel.updatedAt)

        assertEquals(1, fullModel.photos.size)
        assertEquals(photoModel, fullModel.photos.first())
        assertEquals(1, fullModel.poiS.size)
        assertEquals(poiModel, fullModel.poiS.first())
        assertEquals(staticMapModel, fullModel.staticMap)
    }

    @Test
    fun toFullModel_returnsNull_whenUserNotFound() {
        val result = propertyEntity.toFullModel(
            allUsers = emptyList(),
            photos = listOf(photoModel),
            crossRefs = listOf(crossModel),
            allPoiS = listOf(poiModel)
        )

        assertEquals(null, result)
    }

    @Test
    fun toEntity_propertyModel_mapsCorrectlyToPropertyEntity() {
        val entity = propertyModel.toEntity()

        assertEquals(propertyModel.universalLocalId, entity.id)
        assertEquals(propertyModel.firestoreDocumentId, entity.firestoreDocumentId)
        assertEquals(propertyModel.universalLocalUserId, entity.universalLocalUserId)
        assertEquals(propertyModel.title, entity.title)
        assertEquals(propertyModel.type, entity.type)
        assertEquals(propertyModel.price, entity.price)
        assertEquals(propertyModel.surface, entity.surface)
        assertEquals(propertyModel.rooms, entity.rooms)
        assertEquals(propertyModel.description, entity.description)
        assertEquals(propertyModel.address, entity.address)
        assertEquals(propertyModel.isSold, entity.isSold)
        assertEquals(propertyModel.isSynced, entity.isSynced)
        assertEquals(propertyModel.isDeleted, entity.isDeleted)
        assertEquals(propertyModel.updatedAt, entity.updatedAt)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        assertEquals(propertyModel.entryDate.format(formatter), entity.entryDate)
        assertEquals(propertyModel.saleDate?.format(formatter), entity.saleDate)
    }

    @Test
    fun toModel_photoEntity_mapsCorrectlyToPhotoModel() {
        val model = photoEntity.toModel()

        assertEquals(photoEntity.id, model.universalLocalId)
        assertEquals(photoEntity.universalLocalPropertyId, model.universalLocalPropertyId)
        assertEquals(photoEntity.uri, model.uri)
        assertEquals(photoEntity.description, model.description)
        assertEquals(photoEntity.isDeleted, model.isDeleted)
        assertEquals(photoEntity.isSynced, model.isSynced)
        assertEquals(photoEntity.updatedAt, model.updatedAt)
    }


    @Test
    fun  toEntity_photoModel_mapsCorrectlyToPhotoEntity() {
        val entity = photoModel.toEntity()

        assertEquals(photoModel.universalLocalId, entity.id)
        assertEquals(photoModel.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(photoModel.uri, entity.uri)
        assertEquals(photoModel.description, entity.description)
        assertEquals(photoModel.isDeleted, entity.isDeleted)
        assertEquals(photoModel.isSynced, entity.isSynced)
        assertEquals(photoModel.updatedAt, entity.updatedAt)
    }

    @Test
    fun toModel_poiEntity_mapsCorrectlyToPoiModel() {
        val model = poiEntity.toModel()

        assertEquals(poiEntity.id, model.universalLocalId)
        assertEquals(poiEntity.name, model.name)
        assertEquals(poiEntity.type, model.type)
        assertEquals(poiEntity.isSynced, model.isSynced)
        assertEquals(poiEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun toEntity_poiModel_mapsCorrectlyToPoiEntity() {
        val entity = poiModel.toEntity()

        assertEquals(poiModel.universalLocalId, entity.id)
        assertEquals(poiModel.name, entity.name)
        assertEquals(poiModel.type, entity.type)
        assertEquals(poiModel.isSynced, entity.isSynced)
        assertEquals(poiModel.updatedAt, entity.updatedAt)
    }

    @Test
    fun toModel_userEntity_mapsCorrectlyToUserModel() {
        val model = userEntity.toModel()

        assertEquals(userEntity.id, model.universalLocalId)
        assertEquals(userEntity.email, model.email)
        assertEquals(userEntity.agentName, model.agentName)
        assertEquals(userEntity.isSynced, model.isSynced)
        assertEquals(userEntity.firebaseUid, model.firebaseUid)
        assertEquals(userEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun toEntity_userModel_mapsCorrectlyToUserEntity() {
        val entity = userModel.toEntity()

        assertEquals(userModel.universalLocalId, entity.id)
        assertEquals(userModel.email, entity.email)
        assertEquals(userModel.agentName, entity.agentName)
        assertEquals(userModel.isSynced, entity.isSynced)
        assertEquals(userModel.firebaseUid, entity.firebaseUid)
        assertEquals(userModel.updatedAt, entity.updatedAt)
    }

    @Test
    fun toModel_propertyPoiCrossEntity_mapsCorrectlyToModel() {
        val model = crossEntity.toModel()

        assertEquals(crossEntity.universalLocalPropertyId, model.universalLocalPropertyId)
        assertEquals(crossEntity.universalLocalPoiId, model.universalLocalPoiId)
        assertEquals(crossEntity.isSynced, model.isSynced)
        assertEquals(crossEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun toEntity_propertyPoiCrossModel_mapsCorrectlyToEntity() {
        val entity = crossModel.toEntity()

        assertEquals(crossModel.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(crossModel.universalLocalPoiId, entity.universalLocalPoiId)
        assertEquals(crossModel.isSynced, entity.isSynced)
        assertEquals(crossModel.updatedAt, entity.updatedAt)
    }

    @Test
    fun toModel_staticMapEntity_mapsCorrectlyToStaticMapModel() {
        val model = staticMapEntity.toModel()

        assertEquals(staticMapEntity.id, model.universalLocalId)
        assertEquals(staticMapEntity.universalLocalPropertyId, model.universalLocalPropertyId)
        assertEquals(staticMapEntity.uri, model.uri)
        assertEquals(staticMapEntity.isDeleted, model.isDeleted)
        assertEquals(staticMapEntity.isSynced, model.isSynced)
        assertEquals(staticMapEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun toEntity_staticMapModel_mapsCorrectlyToStaticMapEntity() {
        val entity = staticMapModel.toEntity()

        assertEquals(staticMapModel.universalLocalId, entity.id)
        assertEquals(staticMapModel.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(staticMapModel.uri, entity.uri)
        assertEquals(staticMapModel.isDeleted, entity.isDeleted)
        assertEquals(staticMapModel.isSynced, entity.isSynced)
        assertEquals(staticMapModel.updatedAt, entity.updatedAt)
    }

    @Test
    fun toModel_propertyWithPoiSRelation_mapsCorrectly() {
        val relation = PropertyWithPoiSRelation(
            property = propertyEntity,
            poiS = listOf(poiEntity)
        )

        val model = relation.toModel()

        assertEquals(propertyEntity.id, model.property.universalLocalId)
        assertEquals(1, model.poiS.size)
        assertEquals(poiEntity.id, model.poiS.first().universalLocalId)
    }

    @Test
    fun toModel_poiWithPropertiesRelation_mapsCorrectly() {
        val relation = PoiWithPropertiesRelation(
            poi = poiEntity,
            properties = listOf(propertyEntity)
        )

        val model = relation.toModel()

        assertEquals(poiEntity.id, model.poi.universalLocalId)
        assertEquals(1, model.properties.size)
        assertEquals(propertyEntity.id, model.properties.first().universalLocalId)
    }


}
