package com.dcac.realestatemanager.data.sync.property

import android.util.Log
import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineRepository
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first

class PropertyUploadManager(
    private val propertyRepository: PropertyRepository,
    private val propertyOnlineRepository: PropertyOnlineRepository
) : PropertyUploadInterfaceManager {

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to sync data")

    override suspend fun syncUnSyncedProperties(): List<SyncStatus> {
        val results = mutableListOf<SyncStatus>()
        val propertyToSync = propertyRepository.uploadUnSyncedPropertiesToFirebase().first()

        for (propertyEntity in propertyToSync) {
            val firebaseId = propertyEntity.firestoreDocumentId
            val localId = propertyEntity.id
            try {
                if (propertyEntity.isDeleted) {
                    if (firebaseId != null) {
                        propertyOnlineRepository.deleteProperty(firebaseId)
                    }
                    propertyRepository.deleteProperty(propertyEntity)
                    results.add(SyncStatus.Success("Property $localId deleted from Firebase & Room"))
                } else {
                    Log.e("PropertyUpload", "Uploading property ${propertyEntity.id}")
                    val finalId = firebaseId?: generateFirestoreId()
                    val uploadProperty = propertyOnlineRepository.uploadProperty(
                        property = propertyEntity.toOnlineEntity(currentUserUid),
                        firebasePropertyId = finalId
                    )
                    propertyRepository.updatePropertyFromFirebase(
                        property = uploadProperty,
                        firebaseDocumentId = finalId
                    )

                    results.add(SyncStatus.Success("Property $localId uploaded to Firebase"))
                }
            } catch (e: Exception) {
                results.add(SyncStatus.Failure("Property $localId", e))
            }
        }

        return results
    }

    private fun generateFirestoreId(): String {
        return FirebaseFirestore.getInstance()
            .collection(FirestoreCollections.PROPERTIES)
            .document()
            .id
    }
}
