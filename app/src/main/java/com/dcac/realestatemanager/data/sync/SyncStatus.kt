package com.dcac.realestatemanager.data.sync

sealed class SyncStatus {
    data class Success(val email: String) : SyncStatus()
    data class Failure(val email: String, val error: Throwable) : SyncStatus()
}