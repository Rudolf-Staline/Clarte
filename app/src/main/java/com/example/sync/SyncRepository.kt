package com.example.sync

import com.example.data.JournalEntry
import kotlinx.coroutines.flow.StateFlow
import javax.crypto.SecretKey

interface SyncRepository {
    val syncState: StateFlow<SyncState>
    
    suspend fun syncPendingEntries(entries: List<JournalEntry>, encryptionKey: SecretKey? = null): Result<Boolean>
    suspend fun setSyncError(message: String)
    suspend fun fetchAllRemoteEntries(encryptionKey: SecretKey? = null): Result<List<JournalEntry>>
    suspend fun deleteCloudData(): Result<Boolean>
}
