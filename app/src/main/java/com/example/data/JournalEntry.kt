package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val mood: String,
    val intensity: Int,
    val tags: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
    val aiReflection: String? = null,
    val hasReflection: Boolean = false,
    val writingMode: String = "Journal libre",
    val analysisType: String? = null,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val cloudId: String? = null,
    val userId: String? = null,
    val pendingSync: Boolean = false,
    val deleted: Boolean = false,
    val syncedAt: Long? = null,
    val lastSyncError: String? = null
)

class Converters {
    @TypeConverter
    fun fromTagsList(tags: List<String>?): String {
        return tags?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toTagsList(tagsStr: String?): List<String> {
        if (tagsStr.isNullOrEmpty()) return emptyList()
        return tagsStr.split(",").filter { it.isNotEmpty() }
    }
}
