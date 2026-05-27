package com.example.sync

import com.example.data.JournalEntry
import com.example.security.EncryptionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.crypto.SecretKey

class FirebaseSyncRepository : SyncRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private fun getUserCollection() = auth.currentUser?.uid?.let { 
        firestore.collection("users").document(it).collection("entries") 
    }

    override suspend fun setSyncError(message: String) {
        _syncState.value = SyncState.Error(message)
    }

    override suspend fun syncPendingEntries(
        entries: List<JournalEntry>,
        encryptionKey: SecretKey?
    ): Result<Boolean> {
        val collection = getUserCollection() ?: return Result.failure(Exception("Non connecté"))
        
        _syncState.value = SyncState.Syncing
        return try {
            firestore.runBatch { batch ->
                for (entry in entries) {
                    val uid = entry.cloudId ?: UUID.randomUUID().toString()
                    val docRef = collection.document(uid)
                    
                    val data = mutableMapOf<String, Any?>(
                        "cloudId" to uid,
                        "createdAt" to entry.createdAt,
                        "updatedAt" to entry.updatedAt,
                        "deleted" to entry.deleted,
                        "hasReflection" to entry.hasReflection,
                        "isFavorite" to entry.isFavorite,
                        "isPinned" to entry.isPinned,
                        "schemaVersion" to 2
                    )

                    if (encryptionKey != null) {
                        // Encrypt sensitive fields
                        val json = org.json.JSONObject()
                        json.put("content", entry.content)
                        json.put("mood", entry.mood)
                        json.put("intensity", entry.intensity)
                        json.put("tags", org.json.JSONArray(entry.tags))
                        json.put("writingMode", entry.writingMode)
                        json.put("analysisType", entry.analysisType)
                        json.put("aiReflection", entry.aiReflection)
                        json.put("lastSyncError", entry.lastSyncError)
                        
                        val (encryptedBase64, ivBase64) = EncryptionManager.encryptPayload(json.toString(), encryptionKey)
                        
                        data["encrypted"] = true
                        data["encryptionVersion"] = 1
                        data["encryptedPayload"] = encryptedBase64
                        data["iv"] = ivBase64
                    } else {
                        // Plaintext fallback
                        data["encrypted"] = false
                        data["content"] = entry.content
                        data["mood"] = entry.mood
                        data["intensity"] = entry.intensity
                        data["tags"] = entry.tags
                        data["writingMode"] = entry.writingMode
                        data["analysisType"] = entry.analysisType
                        data["aiReflection"] = entry.aiReflection ?: ""
                        data["lastSyncError"] = entry.lastSyncError
                    }
                    
                    batch.set(docRef, data)
                }
            }.await()
            _syncState.value = SyncState.Success(System.currentTimeMillis())
            Result.success(true)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Erreur de synchro")
            Result.failure(e)
        }
    }

    override suspend fun fetchAllRemoteEntries(encryptionKey: SecretKey?): Result<List<JournalEntry>> {
        val collection = getUserCollection() ?: return Result.failure(Exception("Non connecté"))
        
        _syncState.value = SyncState.Syncing
        return try {
            val snapshot = collection.get().await()
            val remoteEntries = snapshot.documents.mapNotNull { doc ->
                try {
                    val isEncrypted = doc.getBoolean("encrypted") ?: false
                    
                    if (isEncrypted) {
                        if (encryptionKey == null) {
                            throw Exception("Missing decryption key for encrypted backup")
                        }
                        
                        val encryptedPayload = doc.getString("encryptedPayload") ?: throw Exception("Missing payload")
                        val iv = doc.getString("iv") ?: throw Exception("Missing IV")
                        
                        val decryptedJsonString = EncryptionManager.decryptPayload(encryptedPayload, iv, encryptionKey)
                        val json = JSONObject(decryptedJsonString)
                        
                        val tagsArray = json.optJSONArray("tags")
                        val tagsList = mutableListOf<String>()
                        if (tagsArray != null) {
                            for (i in 0 until tagsArray.length()) {
                                tagsList.add(tagsArray.getString(i))
                            }
                        }
                        
                        JournalEntry(
                            cloudId = doc.getString("cloudId") ?: doc.id,
                            content = json.optString("content", ""),
                            mood = json.optString("mood", ""),
                            intensity = json.optInt("intensity", 50),
                            tags = tagsList,
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            aiReflection = json.optString("aiReflection").takeIf { it.isNotEmpty() },
                            hasReflection = doc.getBoolean("hasReflection") ?: false,
                            writingMode = json.optString("writingMode", "Journal libre"),
                            analysisType = json.optString("analysisType").takeIf { it.isNotEmpty() },
                            lastSyncError = json.optString("lastSyncError").takeIf { it.isNotEmpty() },
                            isFavorite = doc.getBoolean("isFavorite") ?: false,
                            isPinned = doc.getBoolean("isPinned") ?: false,
                            updatedAt = doc.getLong("updatedAt") ?: 0L,
                            deleted = doc.getBoolean("deleted") ?: false,
                            pendingSync = false,
                            syncedAt = System.currentTimeMillis()
                        )
                    } else {
                        val tags = doc.get("tags") as? List<String> ?: emptyList()
                        JournalEntry(
                            cloudId = doc.getString("cloudId") ?: doc.id,
                            content = doc.getString("content") ?: "",
                            mood = doc.getString("mood") ?: "",
                            intensity = doc.getLong("intensity")?.toInt() ?: 50,
                            tags = tags,
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            aiReflection = doc.getString("aiReflection").takeIf { !it.isNullOrEmpty() },
                            hasReflection = doc.getBoolean("hasReflection") ?: false,
                            writingMode = doc.getString("writingMode") ?: "Journal libre",
                            analysisType = doc.getString("analysisType").takeIf { !it.isNullOrEmpty() },
                            lastSyncError = doc.getString("lastSyncError").takeIf { !it.isNullOrEmpty() },
                            isFavorite = doc.getBoolean("isFavorite") ?: false,
                            isPinned = doc.getBoolean("isPinned") ?: false,
                            updatedAt = doc.getLong("updatedAt") ?: 0L,
                            deleted = doc.getBoolean("deleted") ?: false,
                            pendingSync = false,
                            syncedAt = System.currentTimeMillis()
                        )
                    }
                } catch(e: Exception) {
                    null
                }
            }
            _syncState.value = SyncState.Success(System.currentTimeMillis())
            Result.success(remoteEntries)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Erreur recuperation")
            Result.failure(e)
        }
    }

    override suspend fun deleteCloudData(): Result<Boolean> {
        val collection = getUserCollection() ?: return Result.failure(Exception("Non connecté"))
        return try {
            val snapshot = collection.get().await()
            firestore.runBatch { batch ->
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
