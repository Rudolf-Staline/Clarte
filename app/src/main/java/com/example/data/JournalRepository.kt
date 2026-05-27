package com.example.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao) {
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    fun getEntryById(id: Long): Flow<JournalEntry?> = journalDao.getEntryById(id)

    suspend fun getAllEntriesSync(): List<JournalEntry> = journalDao.getAllEntriesSync()
    suspend fun getPendingSyncEntries(): List<JournalEntry> = journalDao.getPendingSyncEntries()
    suspend fun insertEntryDirectly(entry: JournalEntry): Long = journalDao.insertEntry(entry)
    suspend fun updateEntryDirectly(entry: JournalEntry) = journalDao.updateEntry(entry)

    suspend fun insertEntry(entry: JournalEntry): Long {
        val entryWithSync = entry.copy(pendingSync = true, updatedAt = System.currentTimeMillis())
        return journalDao.insertEntry(entryWithSync)
    }

    suspend fun updateEntry(entry: JournalEntry) {
        val entryWithSync = entry.copy(pendingSync = true, updatedAt = System.currentTimeMillis())
        journalDao.updateEntry(entryWithSync)
    }

    suspend fun deleteEntry(entry: JournalEntry) {
        val entryWithSync = entry.copy(deleted = true, pendingSync = true, updatedAt = System.currentTimeMillis())
        journalDao.updateEntry(entryWithSync)
    }

    suspend fun purgeDeletedEntries() {
        // Option to permanently remove deleted later
    }

    suspend fun deleteAllEntries() {
        val all = journalDao.getAllEntriesSync()
        all.forEach {
            val entryWithSync = it.copy(deleted = true, pendingSync = true, updatedAt = System.currentTimeMillis())
            journalDao.updateEntry(entryWithSync)
        }
    }
}
