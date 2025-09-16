package com.dcac.realestatemanager.data.offlineDatabase.poi

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.PoiWithProperties
import com.dcac.realestatemanager.utils.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dcac.realestatemanager.utils.toModel

class OfflinePoiRepository(
    private val poiDao: PoiDao,
    private val userRepository: UserRepository
): PoiRepository {

    // FOR UI

    override fun getAllPoiS(): Flow<List<Poi>> =
        poiDao.getAllPoiS().map { list -> list.map { it.toModel() } }

    override fun getPoiById(id: Long): Flow<Poi?> =
        poiDao.getPoiById(id).map { it?.toModel() }

    // Inserts single POI
    override suspend fun insertPoi(poi: Poi) = poiDao.insertPoi(poi.toEntity())

    // Inserts a list of POI entities into the database.
    override suspend fun insertAllPoiS(poiS: List<Poi>)=
        poiDao.insertAllPoiS(poiS.map { it.toEntity() })

    override suspend fun updatePoi(poi: Poi) {
        poiDao.updatePoi(poi.toEntity())
    }

    override suspend fun markPoiAsDeleted(poi: Poi) {
        poiDao.markPoiAsDeleted(poi.id, System.currentTimeMillis())
    }

    override fun getPoiWithProperties(poiId: Long): Flow<PoiWithProperties> {
        val relationFlow = poiDao.getPoiWithProperties(poiId)
        val usersFlow = userRepository.getAllUsers()

        return kotlinx.coroutines.flow.combine(relationFlow, usersFlow) { relation, users ->
            relation.toModel(allUsers = users)
        }
    }


    //FOR FIREBASE SYNC

    override fun getPoiEntityById(id: Long): Flow<PoiEntity?> =
        poiDao.getPoiById(id)

    override suspend fun deletePoi(poi: PoiEntity) =
        poiDao.deletePoi(poi)

    override fun uploadUnSyncedPoiSToFirebase(): Flow<List<PoiEntity>> =
        poiDao.uploadUnSyncedPoiSToFirebase()

    override suspend fun downloadPoiFromFirebase(poi: PoiOnlineEntity) {
        poiDao.savePoiFromFirebase(poi.toEntity(poiId = poi.roomId))
    }
}