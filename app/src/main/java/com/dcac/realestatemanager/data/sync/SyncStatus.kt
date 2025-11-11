package com.dcac.realestatemanager.data.sync

sealed class SyncStatus {
    data class Success(val message: String) : SyncStatus()
    data class Failure(val label: String, val error: Throwable) : SyncStatus()
}