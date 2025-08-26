package com.dcac.realestatemanager.data.offlineDatabase.poi

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

    // Returns a flow of all POI entities stored in the database.
    override fun getAllPoiS(): Flow<List<Poi>> =
        poiDao.getAllPoiS().map { list -> list.map { it.toModel() } }

    // Inserts single POI
    override suspend fun insertPoi(poi: Poi) = poiDao.insertPoi(poi.toEntity())

    // Inserts a list of POI entities into the database.
    override suspend fun insertAllPoiS(poiS: List<Poi>)=
        poiDao.insertAllPoiS(poiS.map { it.toEntity() })

    // Deletes a specific POI entity from the database.
    override suspend fun deletePoi(poi: Poi) = poiDao.deletePoi(poi.toEntity())

    override fun getPoiWithProperties(poiId: Long): Flow<PoiWithProperties> {
        val relationFlow = poiDao.getPoiWithProperties(poiId)
        val usersFlow = userRepository.getAllUsers()

        return kotlinx.coroutines.flow.combine(relationFlow, usersFlow) { relation, users ->
            relation.toModel(allUsers = users)
        }
    }
}