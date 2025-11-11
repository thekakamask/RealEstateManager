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

    private val photoOnlineEntity = FakePhotoOnlineEntity.photoOnline1
    private val poiOnlineEntity = FakePoiOnlineEntity.poiOnline1
    private val propertyOnlineEntity = FakePropertyOnlineEntity.propertyOnline1
    private val crossOnlineEntity = FakePropertyPoiCrossOnlineEntity.crossOnline1
    private val userOnlineEntity = FakeUserOnlineEntity.userOnline1
    private val firestoreUserDocument = FakeUserOnlineEntity.firestoreUserDocument1
    private val firestorePhotoDocument = FakePhotoOnlineEntity.firestorePhotoDocument1
    private val firestorePoiDocument = FakePoiOnlineEntity.firestorePoiDocument1
    private val firestorePropertyDocument = FakePropertyOnlineEntity.firestorePropertyDocument1
    private val firestoreCrossDocument = FakePropertyPoiCrossOnlineEntity.firestoreCrossDocument1

    // ---------------- USER ----------------

    @Test
    fun toOnlineEntity_userEntity_mapsCorrectlyToUserOnlineEntity() {
        val online = userEntity.toOnlineEntity()

        assertEquals(userEntity.email, online.email)
        assertEquals(userEntity.agentName, online.agentName)
        assertEquals(userEntity.updatedAt, online.updatedAt)
        assertEquals(userEntity.id, online.universalLocalId)
    }

    @Test
    fun toEntity_userOnlineEntity_mapsCorrectlyToUserEntity() {
        val entity = userOnlineEntity.toEntity(firestoreUserDocument.id)

        assertEquals(userOnlineEntity.universalLocalId, entity.id)
        assertEquals(userOnlineEntity.email, entity.email)
        assertEquals(userOnlineEntity.agentName, entity.agentName)
        assertEquals(userOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firestoreUserDocument.id, entity.firebaseUid)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }

    // ---------------- PHOTO ----------------

    @Test
    fun toOnlineEntity_photoEntity_mapsCorrectlyToPhotoOnlineEntity() {
        val online = photoEntity.toOnlineEntity()

        assertEquals(photoEntity.id, online.universalLocalId)
        assertEquals(photoEntity.universalLocalPropertyId, online.universalLocalPropertyId)
        assertEquals(photoEntity.description, online.description)
        assertEquals(photoEntity.uri, online.storageUrl)
        assertEquals(photoEntity.updatedAt, online.updatedAt)
    }

    @Test
    fun toEntity_photoOnlineEntity_mapsCorrectlyToPhotoEntity() {
        val entity = photoOnlineEntity.toEntity(firestorePhotoDocument.id)

        assertEquals(photoOnlineEntity.universalLocalId, entity.id)
        assertEquals(photoOnlineEntity.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(photoOnlineEntity.description, entity.description)
        assertEquals(photoOnlineEntity.storageUrl, entity.uri)
        assertEquals(photoOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firestorePhotoDocument.id, entity.firestoreDocumentId)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }

    // ---------------- POI ----------------

    @Test
    fun toOnlineEntity_poiEntity_mapsCorrectlyToPoiOnlineEntity() {
        val online = poiEntity.toOnlineEntity()

        assertEquals(poiEntity.id, online.universalLocalId)
        assertEquals(poiEntity.name, online.name)
        assertEquals(poiEntity.type, online.type)
        assertEquals(poiEntity.updatedAt, online.updatedAt)
    }

    @Test
    fun toEntity_poiOnlineEntity_mapsCorrectlyToPoiEntity() {
        val entity = poiOnlineEntity.toEntity(firestorePoiDocument.id)

        assertEquals(poiOnlineEntity.universalLocalId, entity.id)
        assertEquals(poiOnlineEntity.name, entity.name)
        assertEquals(poiOnlineEntity.type, entity.type)
        assertEquals(poiOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firestorePoiDocument.id, entity.firestoreDocumentId)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }

    // ---------------- PROPERTY ----------------

    @Test
    fun toOnlineEntity_propertyEntity_mapsCorrectlyToPropertyOnlineEntity() {
        val online = propertyEntity.toOnlineEntity()

        assertEquals(propertyEntity.id, online.universalLocalId)
        assertEquals(propertyEntity.universalLocalUserId, online.universalLocalUserId)
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
        assertEquals(propertyEntity.staticMapPath, online.staticMapPath)
        assertEquals(propertyEntity.updatedAt, online.updatedAt)
    }

    @Test
    fun toEntity_propertyOnlineEntity_mapsCorrectlyToPropertyEntity() {
        val entity = propertyOnlineEntity.toEntity(firestorePropertyDocument.id)

        assertEquals(propertyOnlineEntity.universalLocalId, entity.id)
        assertEquals(propertyOnlineEntity.universalLocalUserId, entity.universalLocalUserId)
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
        assertEquals(propertyOnlineEntity.staticMapPath, entity.staticMapPath)
        assertEquals(propertyOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firestorePropertyDocument.id, entity.firestoreDocumentId)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }


    // ---------------- PROPERTY POI CROSS ----------------

    @Test
    fun toOnlineEntity_propertyPoiCrossEntity_mapsCorrectlyToOnlineEntity() {
        val online = crossEntity.toOnlineEntity()

        assertEquals(crossEntity.universalLocalPropertyId, online.universalLocalPropertyId)
        assertEquals(crossEntity.universalLocalPoiId, online.universalLocalPoiId)
        assertEquals(crossEntity.updatedAt, online.updatedAt)
    }

    @Test
    fun toEntity_propertyPoiCrossOnlineEntity_mapsCorrectlyToCrossEntity() {
        val entity = crossOnlineEntity.toEntity(firestoreCrossDocument.id)

        assertEquals(crossOnlineEntity.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(crossOnlineEntity.universalLocalPoiId, entity.universalLocalPoiId)
        assertEquals(crossOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firestoreCrossDocument.id, entity.firestoreDocumentId)
        assertEquals(true, entity.isSynced)
        assertEquals(false, entity.isDeleted)
    }
}