package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [JournalEntry::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN writingMode TEXT NOT NULL DEFAULT 'Journal libre'")
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN analysisType TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN updatedAt INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Handle non-null updatedAt modification by making sure it has a valid default if it was null
                db.execSQL("UPDATE journal_entries SET updatedAt = createdAt WHERE updatedAt IS NULL")
                // Cannot easily alter column to NOT NULL in SQLite, but Room will read it depending on how we handle it.
                // Wait, Room will complain if schema doesn't match perfectly. Let's do a table recreation.
                
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `journal_entries_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `content` TEXT NOT NULL, 
                        `mood` TEXT NOT NULL, 
                        `intensity` INTEGER NOT NULL, 
                        `tags` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `aiReflection` TEXT, 
                        `hasReflection` INTEGER NOT NULL, 
                        `writingMode` TEXT NOT NULL, 
                        `analysisType` TEXT, 
                        `isFavorite` INTEGER NOT NULL, 
                        `isPinned` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL DEFAULT 0, 
                        `cloudId` TEXT, 
                        `userId` TEXT, 
                        `pendingSync` INTEGER NOT NULL DEFAULT 0, 
                        `deleted` INTEGER NOT NULL DEFAULT 0, 
                        `syncedAt` INTEGER, 
                        `lastSyncError` TEXT
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO journal_entries_new (
                        id, content, mood, intensity, tags, createdAt, aiReflection, hasReflection, 
                        writingMode, analysisType, isFavorite, isPinned, updatedAt
                    )
                    SELECT 
                        id, content, mood, intensity, tags, createdAt, aiReflection, hasReflection, 
                        writingMode, analysisType, isFavorite, isPinned, 
                        COALESCE(updatedAt, createdAt)
                    FROM journal_entries
                """.trimIndent())

                db.execSQL("DROP TABLE journal_entries")
                db.execSQL("ALTER TABLE journal_entries_new RENAME TO journal_entries")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clarte_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                
                if ((context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    builder.fallbackToDestructiveMigration()
                }
                
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
