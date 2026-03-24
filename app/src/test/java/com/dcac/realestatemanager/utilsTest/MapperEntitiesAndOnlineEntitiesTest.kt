package com.dcac.realestatemanager.utilsTest

import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeStaticMapEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePhotoOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePoiOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeStaticMapOnlineEntity
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
    private val staticMapEntity = FakeStaticMapEntity.staticMap1

    private val photoOnlineEntity = FakePhotoOnlineEntity.photoOnline1
    private val poiOnlineEntity = FakePoiOnlineEntity.poiOnline1
    private val propertyOnlineEntity = FakePropertyOnlineEntity.propertyOnline1
    private val crossOnlineEntity = FakePropertyPoiCrossOnlineEntity.crossOnline1
    private val userOnlineEntity = FakeUserOnlineEntity.userOnline1
    private val staticMapOnlineEntity = FakeStaticMapOnlineEntity.staticMapOnline1
    private val firestoreUserDocument = FakeUserOnlineEntity.firestoreUserDocument1
    private val firestorePhotoDocument = FakePhotoOnlineEntity.firestorePhotoDocument1
    private val firestorePoiDocument = FakePoiOnlineEntity.firestorePoiDocument1
    private val firestorePropertyDocument = FakePropertyOnlineEntity.firestorePropertyDocument1
    private val firestoreCrossDocument = FakePropertyPoiCrossOnlineEntity.firestoreCrossDocument1
    private val firestoreStaticMapDocument = FakeStaticMapOnlineEntity.firestoreStaticMapDocument1

    @Test
    fun toOnlineEntity_userEntity_mapsCorrectlyToUserOnlineEntity() {
        val online = userEntity.toOnlineEntity()

        assertEquals(userEntity.email, online.email)
        assertEquals(userEntity.agentName, online.agentName)
        assertEquals(userEntity.updatedAt, online.updatedAt)
        assertEquals(userEntity.id, online.universalLocalId)
        assertEquals(userEntity.isDeleted, online.isDeleted)
    }

    @Test
    fun toEntity_userOnlineEntity_mapsCorrectlyToUserEntity() {
        val entity = userOnlineEntity.toEntity(firestoreUserDocument.firebaseId)

        assertEquals(userOnlineEntity.universalLocalId, entity.id)
        assertEquals(userOnlineEntity.email, entity.email)
        assertEquals(userOnlineEntity.agentName, entity.agentName)
        assertEquals(userOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firestoreUserDocument.firebaseId, entity.firebaseUid)
        assertEquals(userOnlineEntity.isDeleted, entity.isDeleted)
        assertEquals(true, entity.isSynced)
    }

    @Test
    fun toOnlineEntity_photoEntity_mapsCorrectlyToPhotoOnlineEntity() {
        val online = photoEntity.toOnlineEntity(photoOnlineEntity.ownerUid)

        assertEquals(photoOnlineEntity.ownerUid, online.ownerUid)
        assertEquals(photoEntity.id, online.universalLocalId)
        assertEquals(photoEntity.universalLocalPropertyId, online.universalLocalPropertyId)
        assertEquals(photoEntity.description, online.description)
        assertEquals(photoEntity.uri, online.storageUrl)
        assertEquals(photoEntity.updatedAt, online.updatedAt)
        assertEquals(photoEntity.isDeleted, online.isDeleted)
    }

    @Test
    fun toEntity_photoOnlineEntity_mapsCorrectlyToPhotoEntity() {
        val entity = photoOnlineEntity.toEntity(firestorePhotoDocument.firebaseId)

        assertEquals(photoOnlineEntity.universalLocalId, entity.id)
        assertEquals(photoOnlineEntity.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(photoOnlineEntity.description, entity.description)
        assertEquals(photoOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(firestorePhotoDocument.firebaseId, entity.firestoreDocumentId)
        assertEquals(photoOnlineEntity.isDeleted, entity.isDeleted)
        assertEquals("", entity.uri)
        assertEquals(true, entity.isSynced)
    }

    @Test
    fun toOnlineEntity_poiEntity_mapsCorrectlyToPoiOnlineEntity() {
        val online = poiEntity.toOnlineEntity(poiOnlineEntity.ownerUid)

        assertEquals(poiOnlineEntity.ownerUid, online.ownerUid)
        assertEquals(poiEntity.id, online.universalLocalId)
        assertEquals(poiEntity.name, online.name)
        assertEquals(poiEntity.type, online.type)
        assertEquals(poiEntity.updatedAt, online.updatedAt)
        assertEquals(poiEntity.isDeleted, online.isDeleted)
        assertEquals(poiEntity.latitude, online.latitude)
        assertEquals(poiEntity.longitude, online.longitude)
        assertEquals(poiEntity.address, online.address)
    }

    @Test
    fun toEntity_poiOnlineEntity_mapsCorrectlyToPoiEntity() {
        val entity = poiOnlineEntity.toEntity(firestorePoiDocument.firebaseId)

        assertEquals(poiOnlineEntity.universalLocalId, entity.id)
        assertEquals(firestorePoiDocument.firebaseId, entity.firestoreDocumentId)
        assertEquals(poiOnlineEntity.name, entity.name)
        assertEquals(poiOnlineEntity.type, entity.type)
        assertEquals(poiOnlineEntity.address, entity.address)
        assertEquals(poiOnlineEntity.latitude, entity.latitude)
        assertEquals(poiOnlineEntity.longitude, entity.longitude)
        assertEquals(poiOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(true, entity.isSynced)
        assertEquals(poiOnlineEntity.isDeleted, entity.isDeleted)
    }

    @Test
    fun toEntity_poiOnlineEntity_withNullFirestoreId() {
        val entity = poiOnlineEntity.toEntity(null)

        assertEquals(null, entity.firestoreDocumentId)
    }

    @Test
    fun toOnlineEntity_propertyEntity_mapsCorrectlyToPropertyOnlineEntity() {
        val online = propertyEntity.toOnlineEntity(propertyOnlineEntity.ownerUid)

        assertEquals(propertyOnlineEntity.ownerUid, online.ownerUid)
        assertEquals(propertyEntity.id, online.universalLocalId)
        assertEquals(propertyEntity.universalLocalUserId, online.universalLocalUserId)
        assertEquals(propertyEntity.title, online.title)
        assertEquals(propertyEntity.type, online.type)
        assertEquals(propertyEntity.price, online.price)
        assertEquals(propertyEntity.surface, online.surface)
        assertEquals(propertyEntity.rooms, online.rooms)
        assertEquals(propertyEntity.description, online.description)
        assertEquals(propertyEntity.address, online.address)
        assertEquals(propertyEntity.latitude, online.latitude)
        assertEquals(propertyEntity.longitude, online.longitude)
        assertEquals(propertyEntity.isSold, online.isSold)
        assertEquals(propertyEntity.entryDate, online.entryDate)
        assertEquals(propertyEntity.saleDate, online.saleDate)
        assertEquals(propertyEntity.updatedAt, online.updatedAt)
        assertEquals(propertyEntity.isDeleted, online.isDeleted)
    }

    @Test
    fun toEntity_propertyOnlineEntity_mapsCorrectlyToPropertyEntity() {
        val entity = propertyOnlineEntity.toEntity(firestorePropertyDocument.firebaseId)

        assertEquals(propertyOnlineEntity.universalLocalId, entity.id)
        assertEquals(firestorePropertyDocument.firebaseId, entity.firestoreDocumentId)
        assertEquals(propertyOnlineEntity.universalLocalUserId, entity.universalLocalUserId)
        assertEquals(propertyOnlineEntity.title, entity.title)
        assertEquals(propertyOnlineEntity.type, entity.type)
        assertEquals(propertyOnlineEntity.price, entity.price)
        assertEquals(propertyOnlineEntity.surface, entity.surface)
        assertEquals(propertyOnlineEntity.rooms, entity.rooms)
        assertEquals(propertyOnlineEntity.description, entity.description)
        assertEquals(propertyOnlineEntity.address, entity.address)
        assertEquals(propertyOnlineEntity.latitude, entity.latitude)
        assertEquals(propertyOnlineEntity.longitude, entity.longitude)
        assertEquals(propertyOnlineEntity.isSold, entity.isSold)
        assertEquals(propertyOnlineEntity.entryDate, entity.entryDate)
        assertEquals(propertyOnlineEntity.saleDate, entity.saleDate)
        assertEquals(propertyOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(true, entity.isSynced)
        assertEquals(propertyOnlineEntity.isDeleted, entity.isDeleted)
    }

    @Test
    fun toEntity_propertyOnlineEntity_withNullFirestoreId() {
        val entity = propertyOnlineEntity.toEntity(null)

        assertEquals(null, entity.firestoreDocumentId)
    }

    @Test
    fun toOnlineEntity_propertyPoiCrossEntity_mapsCorrectlyToOnlineEntity() {
        val online = crossEntity.toOnlineEntity(crossOnlineEntity.ownerUid)

        assertEquals(crossOnlineEntity.ownerUid, online.ownerUid)
        assertEquals(crossEntity.universalLocalPropertyId, online.universalLocalPropertyId)
        assertEquals(crossEntity.universalLocalPoiId, online.universalLocalPoiId)
        assertEquals(crossEntity.updatedAt, online.updatedAt)
        assertEquals(crossEntity.isDeleted, online.isDeleted)
    }

    @Test
    fun toEntity_propertyPoiCrossOnlineEntity_mapsCorrectlyToCrossEntity() {
        val entity = crossOnlineEntity.toEntity(firestoreCrossDocument.firebaseId)

        assertEquals(crossOnlineEntity.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(crossOnlineEntity.universalLocalPoiId, entity.universalLocalPoiId)
        assertEquals(firestoreCrossDocument.firebaseId, entity.firestoreDocumentId)
        assertEquals(crossOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(true, entity.isSynced)
        assertEquals(crossOnlineEntity.isDeleted, entity.isDeleted)
    }

    @Test
    fun toEntity_crossOnlineEntity_withNullFirestoreId() {
        val entity = crossOnlineEntity.toEntity(null)

        assertEquals(null, entity.firestoreDocumentId)
    }

    @Test
    fun toOnlineEntity_staticMapEntity_mapsCorrectlyToOnlineEntity() {
        val online = staticMapEntity.toOnlineEntity(staticMapOnlineEntity.ownerUid)

        assertEquals(staticMapOnlineEntity.ownerUid, online.ownerUid)
        assertEquals(staticMapEntity.id, online.universalLocalId)
        assertEquals(staticMapEntity.universalLocalPropertyId, online.universalLocalPropertyId)
        assertEquals(staticMapEntity.updatedAt, online.updatedAt)
        assertEquals(staticMapEntity.uri, online.storageUrl)
        assertEquals(staticMapEntity.isDeleted, online.isDeleted)
    }

    @Test
    fun toEntity_staticMapOnlineEntity_mapsCorrectlyToEntity() {
        val entity = staticMapOnlineEntity.toEntity(firestoreStaticMapDocument.firebaseId)

        assertEquals(staticMapOnlineEntity.universalLocalId, entity.id)
        assertEquals(staticMapOnlineEntity.universalLocalPropertyId, entity.universalLocalPropertyId)
        assertEquals(firestoreStaticMapDocument.firebaseId, entity.firestoreDocumentId)
        assertEquals(true, entity.isSynced)
        assertEquals("", entity.uri)
        assertEquals(staticMapOnlineEntity.updatedAt, entity.updatedAt)
        assertEquals(staticMapOnlineEntity.isDeleted, entity.isDeleted)
    }

    @Test
    fun toEntity_staticMapOnlineEntity_withNullFirestoreId() {
        val entity = staticMapOnlineEntity.toEntity(null)

        assertEquals(null, entity.firestoreDocumentId)
    }
}
