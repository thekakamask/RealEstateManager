package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePhotoOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePoiOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toOnlineEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperEntitiesAndOnlineEntitiesTest {

    private val photoEntity = FakePhotoEntity.photo1
    private val poiEntity = FakePoiEntity.poi1
    private val propertyEntity = FakePropertyEntity.property1
    private val crossEntity = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val userEntity = FakeUserEntity.user1

    private val photoOnlineEntity = FakePhotoOnlineEntity.photoEntity1
    private val poiOnlineEntity = FakePoiOnlineEntity.poiEntity1
    private val propertyOnlineEntity = FakePropertyOnlineEntity.propertyEntity1
    private val crossOnlineEntity = FakePropertyPoiCrossOnlineEntity.cross1
    private val userOnlineEntity = FakeUserOnlineEntity.userOnline1
    private val firebaseUserDocument = FakeUserOnlineEntity.firestoreUserDocument1

    // ---------------- USER ----------------

    @Test
    fun toOnlineEntity_userEntity_mapsCorrectlyToUserOnlineEntity() {
        val online = userEntity.toOnlineEntity()

        assertEquals(userEntity.email, online.email)
        assertEquals(userEntity.agentName, online.agentName)
        assertEquals(userEntity.updatedAt, online.updatedAt)
        assertEquals(userEntity.id, online.roomId)
    }

    @Test
    fun toEntity_userOnlineEntity_mapsCorrectlyToUserEntity() {
        val entity = userOnlineEntity.toEntity(userOnlineEntity.roomId, firebaseUserDocument.id)

        assertEquals(userOnlineEntity.roomId, entity.id)
        assertEquals(userOnlineEntity.email, entity.email)
        assertEquals(userOnlineEntity.agentName, entity.agentName)
        assertEquals(userOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firebaseUserDocument.id, entity.firebaseUid)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }

    // ---------------- PHOTO ----------------

    @Test
    fun toOnlineEntity_photoEntity_mapsCorrectlyToPhotoOnlineEntity() {
        val online = photoEntity.toOnlineEntity()

        assertEquals(photoEntity.propertyId, online.propertyId)
        assertEquals(photoEntity.description, online.description)
        assertEquals(photoEntity.updatedAt, online.updatedAt)
        assertEquals(photoEntity.id, online.roomId)
    }

    @Test
    fun toEntity_photoOnlineEntity_mapsCorrectlyToPhotoEntity() {
        val entity = photoOnlineEntity.toEntity(photoOnlineEntity.roomId)

        assertEquals(photoOnlineEntity.roomId, entity.id)
        assertEquals(photoOnlineEntity.propertyId, entity.propertyId)
        assertEquals(photoOnlineEntity.description, entity.description)
        assertEquals(photoOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }

    // ---------------- POI ----------------

    @Test
    fun toOnlineEntity_poiEntity_mapsCorrectlyToPoiOnlineEntity() {
        val online = poiEntity.toOnlineEntity()

        assertEquals(poiEntity.name, online.name)
        assertEquals(poiEntity.type, online.type)
        assertEquals(poiEntity.updatedAt, online.updatedAt)
        assertEquals(poiEntity.id, online.roomId)
    }

    @Test
    fun toEntity_poiOnlineEntity_mapsCorrectlyToPoiEntity() {
        val entity = poiOnlineEntity.toEntity(poiOnlineEntity.roomId)

        assertEquals(poiOnlineEntity.roomId, entity.id)
        assertEquals(poiOnlineEntity.name, entity.name)
        assertEquals(poiOnlineEntity.type, entity.type)
        assertEquals(poiOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }

    // ---------------- PROPERTY ----------------

    @Test
    fun toOnlineEntity_propertyEntity_mapsCorrectlyToPropertyOnlineEntity() {
        val online = propertyEntity.toOnlineEntity()

        assertEquals(propertyEntity.title, online.title)
        assertEquals(propertyEntity.type, online.type)
        assertEquals(propertyEntity.price, online.price)
        assertEquals(propertyEntity.surface, online.surface)
        assertEquals(propertyEntity.rooms, online.rooms)
        assertEquals(propertyEntity.description, online.description)
        assertEquals(propertyEntity.address, online.address)
        assertEquals(propertyEntity.isSold, online.isSold)
        assertEquals(propertyEntity.entryDate, online.entryDate)
        assertEquals(propertyEntity.saleDate, online.saleDate)
        assertEquals(propertyEntity.userId, online.userId)
        assertEquals(propertyEntity.staticMapPath, online.staticMapPath)
        assertEquals(propertyEntity.updatedAt, online.updatedAt)
        assertEquals(propertyEntity.id, online.roomId)
    }

    @Test
    fun toEntity_propertyOnlineEntity_mapsCorrectlyToPropertyEntity() {
        val entity = propertyOnlineEntity.toEntity(propertyOnlineEntity.roomId)

        assertEquals(propertyOnlineEntity.roomId, entity.id)
        assertEquals(propertyOnlineEntity.title, entity.title)
        assertEquals(propertyOnlineEntity.type, entity.type)
        assertEquals(propertyOnlineEntity.price, entity.price)
        assertEquals(propertyOnlineEntity.surface, entity.surface)
        assertEquals(propertyOnlineEntity.rooms, entity.rooms)
        assertEquals(propertyOnlineEntity.description, entity.description)
        assertEquals(propertyOnlineEntity.address, entity.address)
        assertEquals(propertyOnlineEntity.isSold, entity.isSold)
        assertEquals(propertyOnlineEntity.entryDate, entity.entryDate)
        assertEquals(propertyOnlineEntity.saleDate, entity.saleDate)
        assertEquals(propertyOnlineEntity.userId, entity.userId)
        assertEquals(propertyOnlineEntity.staticMapPath, entity.staticMapPath)
        assertEquals(propertyOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }

    // ---------------- PROPERTY POI CROSS ----------------

    @Test
    fun toOnlineEntity_propertyPoiCrossEntity_mapsCorrectlyToOnlineEntity() {
        val online = crossEntity.toOnlineEntity()

        assertEquals(crossEntity.propertyId, online.propertyId)
        assertEquals(crossEntity.poiId, online.poiId)
        assertEquals(crossEntity.updatedAt, online.updatedAt)
    }

    @Test
    fun toEntity_propertyPoiCrossOnlineEntity_mapsCorrectlyToCrossEntity() {
        val entity = crossOnlineEntity.toEntity()

        assertEquals(crossOnlineEntity.propertyId, entity.propertyId)
        assertEquals(crossOnlineEntity.poiId, entity.poiId)
        assertEquals(crossOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }
}