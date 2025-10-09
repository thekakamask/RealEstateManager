package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePhotoModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePoiModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyPoiCrossModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import org.junit.Assert.assertEquals
import org.junit.Test
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel

class MapperModelsAndEntitiesTest {

    private val photoEntity  = FakePhotoEntity.photo1
    private val poiEntity = FakePoiEntity.poi1
    private val propertyEntity = FakePropertyEntity.property1
    private val crossEntity = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val userEntity = FakeUserEntity.user1

    private val photoModel = FakePhotoModel.photo1
    private val poiModel = FakePoiModel.poi1
    private val propertyModel = FakePropertyModel.property1
    private val crossModel = FakePropertyPoiCrossModel.cross1
    private val userModel = FakeUserModel.user1


// ---------------- PHOTO ----------------

    @Test
    fun toModel_photoEntity_mapsCorrectlyToPhotoModel() {
        val model = photoEntity.toModel()

        assertEquals(photoEntity.id, model.id)
        assertEquals(photoEntity.propertyId, model.propertyId)
        assertEquals(photoEntity.uri, model.uri)
        assertEquals(photoEntity.description, model.description)
        assertEquals(photoEntity.isDeleted, model.isDeleted)
        assertEquals(photoEntity.isSynced, model.isSynced)
        assertEquals(photoEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun  toEntity_photoModel_mapsCorrectlyToPhotoEntity() {
        val entity = photoModel.toEntity()

        assertEquals(photoModel.id, entity.id)
        assertEquals(photoModel.propertyId, entity.propertyId)
        assertEquals(photoModel.uri, entity.uri)
        assertEquals(photoModel.description, entity.description)
        assertEquals(photoModel.isDeleted, entity.isDeleted)
        assertEquals(photoModel.isSynced, entity.isSynced)
        assertEquals(photoModel.updatedAt, entity.updatedAt)
    }

    // ---------------- POI ----------------

    @Test
    fun toModel_poiEntity_mapsCorrectlyToPoiModel() {
        val model = poiEntity.toModel()

        assertEquals(poiEntity.id, model.id)
        assertEquals(poiEntity.name, model.name)
        assertEquals(poiEntity.type, model.type)
        assertEquals(poiEntity.isSynced, model.isSynced)
        assertEquals(poiEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun toEntity_poiModel_mapsCorrectlyToPoiEntity() {
        val entity = poiModel.toEntity()

        assertEquals(poiModel.id, entity.id)
        assertEquals(poiModel.name, entity.name)
        assertEquals(poiModel.type, entity.type)
        assertEquals(poiModel.isSynced, entity.isSynced)
        assertEquals(poiModel.updatedAt, entity.updatedAt)
    }

    // ---------------- USER ----------------

    @Test
    fun toModel_userEntity_mapsCorrectlyToUserModel() {
        val model = userEntity.toModel()

        assertEquals(userEntity.id, model.id)
        assertEquals(userEntity.email, model.email)
        assertEquals(userEntity.agentName, model.agentName)
        assertEquals(userEntity.isSynced, model.isSynced)
        assertEquals(userEntity.firebaseUid, model.firebaseUid)
        assertEquals(userEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun toEntity_userModel_mapsCorrectlyToUserEntity() {
        val entity = userModel.toEntity()

        assertEquals(userModel.id, entity.id)
        assertEquals(userModel.email, entity.email)
        assertEquals(userModel.agentName, entity.agentName)
        assertEquals(userModel.isSynced, entity.isSynced)
        assertEquals(userModel.firebaseUid, entity.firebaseUid)
        assertEquals(userModel.updatedAt, entity.updatedAt)
    }

    // ---------------- PROPERTY POI CROSS ----------------

    @Test
    fun toModel_propertyPoiCrossEntity_mapsCorrectlyToModel() {
        val model = crossEntity.toModel()

        assertEquals(crossEntity.propertyId, model.propertyId)
        assertEquals(crossEntity.poiId, model.poiId)
        assertEquals(crossEntity.isSynced, model.isSynced)
        assertEquals(crossEntity.updatedAt, model.updatedAt)
    }

    @Test
    fun toEntity_propertyPoiCrossModel_mapsCorrectlyToEntity() {
        val entity = crossModel.toEntity()

        assertEquals(crossModel.propertyId, entity.propertyId)
        assertEquals(crossModel.poiId, entity.poiId)
        assertEquals(crossModel.isSynced, entity.isSynced)
        assertEquals(crossModel.updatedAt, entity.updatedAt)
    }

    // ---------------- PROPERTY ----------------

    @Test
    fun toModel_propertyEntityWithUserPhotosPoiS_mapsCorrectlyToPropertyModel() {

        val model = propertyEntity.toModel(userModel, listOf(photoModel), listOf(poiModel))

        assertEquals(propertyEntity.id, model.id)
        assertEquals(propertyEntity.title, model.title)
        assertEquals(propertyEntity.type, model.type)
        assertEquals(propertyEntity.price, model.price)
        assertEquals(propertyEntity.surface, model.surface)
        assertEquals(propertyEntity.rooms, model.rooms)
        assertEquals(propertyEntity.description, model.description)
        assertEquals(propertyEntity.address, model.address)
        assertEquals(propertyEntity.isSold, model.isSold)
        assertEquals(propertyEntity.staticMapPath, model.staticMapPath)
        assertEquals(propertyEntity.isSynced, model.isSynced)
        assertEquals(propertyEntity.updatedAt, model.updatedAt)

        assertEquals(userModel, model.user)
        assertEquals(listOf(photoModel), model.photos)
        assertEquals(listOf(poiModel), model.poiS)
    }

    @Test
    fun toEntity_propertyModel_mapsCorrectlyToPropertyEntity() {
        val entity = propertyModel.toEntity()

        assertEquals(propertyModel.id, entity.id)
        assertEquals(propertyModel.title, entity.title)
        assertEquals(propertyModel.type, entity.type)
        assertEquals(propertyModel.price, entity.price)
        assertEquals(propertyModel.surface, entity.surface)
        assertEquals(propertyModel.rooms, entity.rooms)
        assertEquals(propertyModel.description, entity.description)
        assertEquals(propertyModel.address, entity.address)
        assertEquals(propertyModel.isSold, entity.isSold)
        assertEquals(propertyModel.staticMapPath, entity.staticMapPath)
        assertEquals(propertyModel.isSynced, entity.isSynced)
        assertEquals(propertyModel.updatedAt, entity.updatedAt)

        assertEquals(propertyModel.user.id, entity.userId)
        assertEquals(propertyModel.entryDate.toString(), entity.entryDate)
        assertEquals(propertyModel.saleDate?.toString(), entity.saleDate)
    }
}