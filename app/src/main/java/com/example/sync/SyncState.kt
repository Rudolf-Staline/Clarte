package com.example.sync

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Error(val message: String) : SyncState()
    data class Success(val syncTime: Long) : SyncState()
}
