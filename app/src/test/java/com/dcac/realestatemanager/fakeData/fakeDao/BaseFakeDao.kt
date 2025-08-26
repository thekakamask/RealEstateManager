package com.dcac.realestatemanager.fakeData.fakeDao

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Base class for all fake DAO implementations.
 * Provides in-memory storage and reactive flows using StateFlow.
 */
abstract class BaseFakeDao<ID, ENTITY>(
    private val idSelector: (ENTITY) -> ID
) {

    // Internal in-memory map to simulate Room's data storage
    internal val entityMap = ConcurrentHashMap<ID, ENTITY>()

    // Flow that emits all entities whenever a change occurs
    internal val entityFlow = MutableStateFlow<List<ENTITY>>(emptyList())

    /**
     * Adds or updates an entity in the in-memory map and updates the flow
     */
    protected fun upsert(entity: ENTITY) {
        entityMap[idSelector(entity)] = entity
        refreshFlow()
    }

    /**
     * Deletes an entity from the map and refreshes the flow
     */
    protected fun delete(entity: ENTITY) {
        entityMap.remove(idSelector(entity))
        refreshFlow()
    }

    /**
     * Refresh the flow with the current state of the map
     */
    protected fun refreshFlow() {
        entityFlow.value = entityMap.values.toList()
    }

    /**
     * Clears all entities
     */
    fun clear() {
        entityMap.clear()
        refreshFlow()
    }

    /**
     * Replaces the current map with a custom list of entities
     */
    fun seed(data: List<ENTITY>) {
        entityMap.clear()
        data.forEach { entity -> entityMap[idSelector(entity)] = entity }
        refreshFlow()
    }
}