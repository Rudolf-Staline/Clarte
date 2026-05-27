package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE deleted = 0 ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id AND deleted = 0 LIMIT 1")
    fun getEntryById(id: Long): Flow<JournalEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    suspend fun getAllEntriesSync(): List<JournalEntry>

    @Query("SELECT * FROM journal_entries WHERE pendingSync = 1")
    suspend fun getPendingSyncEntries(): List<JournalEntry>

    @Update
    suspend fun updateEntry(entry: JournalEntry)


    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()
}
