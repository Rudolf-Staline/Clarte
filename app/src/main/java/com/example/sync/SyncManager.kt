package com.example.sync

import com.example.data.JournalRepository
import com.example.data.JournalEntry
import com.example.data.SettingsStore
import com.example.security.EncryptionManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.crypto.SecretKey

class SyncManager(
    private val syncRepository: SyncRepository,
    private val journalRepository: JournalRepository,
    private val settingsStore: SettingsStore
) {
    val syncState: StateFlow<SyncState> = syncRepository.syncState

    private suspend fun getEncryptionKeyIfEnabled(): SecretKey? {
        val enabled = settingsStore.encryptedBackupEnabledFlow.firstOrNull() ?: false
        if (!enabled) return null
        
        val wrappedKeyBase64 = settingsStore.wrappedEncryptionKeyFlow.firstOrNull()
        val ivBase64 = settingsStore.encryptionKeyIvFlow.firstOrNull()
        
        if (wrappedKeyBase64 != null && ivBase64 != null) {
            return try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    EncryptionManager.unwrapKey(wrappedKeyBase64, ivBase64)
                }
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    suspend fun syncNow() {
        // Resolve encryption key
        val enabled = settingsStore.encryptedBackupEnabledFlow.firstOrNull() ?: false
        val encryptionKey = getEncryptionKeyIfEnabled()
        if (enabled && encryptionKey == null) {
            // Cannot sync, encryption is enabled but key is missing
            syncRepository.setSyncError("Clé de chiffrement introuvable. Renseigne ta phrase de récupération pour synchroniser.")
            return
        }

        // 1. Push local changes
        val pending = journalRepository.getPendingSyncEntries()
        if (pending.isNotEmpty()) {
            val result = syncRepository.syncPendingEntries(pending, encryptionKey)
            
            if (result.isSuccess) {
                val updated = pending.map { 
                    it.copy(
                        pendingSync = false, 
                        syncedAt = System.currentTimeMillis(),
                        cloudId = it.cloudId ?: java.util.UUID.randomUUID().toString()
                    ) 
                }
                updated.forEach { journalRepository.updateEntryDirectly(it) }
            }
        }
        
        // 2. Fetch remote changes
        val remoteResult = syncRepository.fetchAllRemoteEntries(encryptionKey)
        if (remoteResult.isSuccess) {
            val remoteEntries = remoteResult.getOrNull() ?: emptyList()
            val localEntries = journalRepository.getAllEntriesSync()
            
            remoteEntries.forEach { remote ->
                val localMatch = localEntries.find { it.cloudId == remote.cloudId }
                if (localMatch == null) {
                    if (!remote.deleted) {
                        journalRepository.insertEntryDirectly(remote)
                    }
                } else {
                    // Conflict Resolution
                    if (remote.updatedAt > localMatch.updatedAt) {
                        journalRepository.updateEntryDirectly(remote.copy(id = localMatch.id))
                    } else if (localMatch.updatedAt > remote.updatedAt && !localMatch.pendingSync) {
                        // Keep both copies when conflicts happen on the same entry.
                        if (localMatch.content != remote.content) {
                            val conflictedRemote = remote.copy(
                                id = 0, // new local ID
                                cloudId = java.util.UUID.randomUUID().toString(),
                                content = (remote.content ?: "") + "\n\n(Copie restaurée — conflit)",
                                pendingSync = true
                            )
                            journalRepository.insertEntryDirectly(conflictedRemote)
                        }
                    }
                }
            }
        }
    }

    suspend fun testPassphrase(passphrase: String): Boolean {
        return try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return false
            val metadataSnapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("metadata").document("encryption")
                .get().await()
                
            val saltBase64 = metadataSnapshot.getString("salt") ?: return false
            val saltBytes = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
            
            val derivedKey = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.security.EncryptionManager.deriveKey(passphrase, saltBytes)
            }
            
            // Try to fetch 1 entry to test decrypt
            val testResult = syncRepository.fetchAllRemoteEntries(derivedKey)
            if (testResult.isSuccess) {
                true
            } else {
                val error = testResult.exceptionOrNull()
                !(error?.message?.contains("bad record MAC") == true || error?.message?.contains("Missing decryption key") == true)
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restoreFromCloud(passphrase: String? = null): Boolean {
        // First check metadata
        var encryptionKey = getEncryptionKeyIfEnabled()
        
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return false
            val metadataSnapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("metadata").document("encryption")
                .get().await()
                
            val isEncryptedInCloud = metadataSnapshot.getBoolean("encryptionEnabled") ?: false
            if (isEncryptedInCloud && encryptionKey == null) {
                if (passphrase == null) {
                    // Need passphrase to restore
                    throw Exception("PASSPHRASE_REQUIRED")
                } else {
                    val saltBase64 = metadataSnapshot.getString("salt") ?: throw Exception("Invalid cloud encryption metadata")
                    val saltBytes = android.util.Base64.decode(saltBase64, android.util.Base64.NO_WRAP)
                    val derivedKey = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.example.security.EncryptionManager.deriveKey(passphrase, saltBytes)
                    }
                    encryptionKey = derivedKey
                }
            }
        } catch(e: Exception) {
            if (e.message == "PASSPHRASE_REQUIRED") {
                // Signal UI somehow, since we update state
                syncRepository.syncState // Wait, syncRepository doesn't expose a method to set error directly
                return false // We will handle this in UI
            }
        }

        val result = syncRepository.fetchAllRemoteEntries(encryptionKey)
        if (result.isSuccess) {
            val remoteEntries = result.getOrNull() ?: emptyList()
            val localEntries = journalRepository.getAllEntriesSync()
            
            remoteEntries.forEach { remote ->
                if (!remote.deleted) {
                    val localMatch = localEntries.find { it.cloudId == remote.cloudId }
                    if (localMatch == null) {
                        journalRepository.insertEntryDirectly(remote)
                    } else if (remote.updatedAt > localMatch.updatedAt) {
                        journalRepository.updateEntryDirectly(remote.copy(id = localMatch.id))
                    } else if (remote.updatedAt < localMatch.updatedAt) {
                        if (localMatch.content != remote.content) {
                            val conflictedRemote = remote.copy(
                                id = 0,
                                cloudId = java.util.UUID.randomUUID().toString(),
                                content = (remote.content ?: "") + "\n\n(Copie restaurée — conflit)",
                                pendingSync = true
                            )
                            journalRepository.insertEntryDirectly(conflictedRemote)
                        }
                    }
                }
            }
            
            // If we successfully restored using a provided passphrase, save it locally for future syncs!
            if (passphrase != null && encryptionKey != null) {
                val (wrapped, iv) = com.example.security.EncryptionManager.wrapKey(encryptionKey)
                settingsStore.setWrappedEncryptionKey(wrapped, iv)
                settingsStore.setEncryptedBackupEnabled(true)
            }
            return true
        } else {
             val error = result.exceptionOrNull()
             if (error?.message?.contains("bad record MAC") == true || error?.message?.contains("Missing decryption key") == true) {
                 throw Exception("INVALID_PASSPHRASE")
             }
        }
        return false
    }
}
