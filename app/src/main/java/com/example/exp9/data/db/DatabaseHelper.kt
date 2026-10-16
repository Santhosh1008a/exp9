package com.example.exp9.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.exp9.data.model.Note
import com.example.exp9.data.model.NoteStats
import com.example.exp9.data.pref.PreferencesManager

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_NOTES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_CONTENT TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_UPDATED_AT INTEGER NOT NULL,
                $COL_IS_FAVORITE INTEGER NOT NULL DEFAULT 0,
                $COL_IS_PINNED INTEGER NOT NULL DEFAULT 0,
                $COL_IS_ARCHIVED INTEGER NOT NULL DEFAULT 0,
                $COL_IS_COMPLETED INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safe upgrade logic if database schema changes in future versions
        if (oldVersion < 2) {
            // Example migration steps when incrementing DATABASE_VERSION
        }
    }

    fun insertNote(note: Note): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, note.title)
            put(COL_CONTENT, note.content)
            put(COL_CATEGORY, note.category)
            put(COL_CREATED_AT, note.createdAt)
            put(COL_UPDATED_AT, note.updatedAt)
            put(COL_IS_FAVORITE, if (note.isFavorite) 1 else 0)
            put(COL_IS_PINNED, if (note.isPinned) 1 else 0)
            put(COL_IS_ARCHIVED, if (note.isArchived) 1 else 0)
            put(COL_IS_COMPLETED, if (note.isCompleted) 1 else 0)
        }
        return db.insert(TABLE_NOTES, null, values)
    }

    fun updateNote(note: Note): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, note.title)
            put(COL_CONTENT, note.content)
            put(COL_CATEGORY, note.category)
            put(COL_UPDATED_AT, System.currentTimeMillis())
            put(COL_IS_FAVORITE, if (note.isFavorite) 1 else 0)
            put(COL_IS_PINNED, if (note.isPinned) 1 else 0)
            put(COL_IS_ARCHIVED, if (note.isArchived) 1 else 0)
            put(COL_IS_COMPLETED, if (note.isCompleted) 1 else 0)
        }
        return db.update(
            TABLE_NOTES,
            values,
            "$COL_ID = ?",
            arrayOf(note.id.toString())
        )
    }

    fun deleteNote(noteId: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTES, "$COL_ID = ?", arrayOf(noteId.toString()))
    }

    fun deleteAllNotes(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTES, null, null)
    }

    fun getNoteById(noteId: Long): Note? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            "$COL_ID = ?",
            arrayOf(noteId.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToNote(it) else null
        }
    }

    fun getAllNotes(
        searchQuery: String? = null,
        category: String? = null,
        sortOrder: String = PreferencesManager.SORT_NEWEST,
        showArchived: Boolean = false,
        showFavoritesOnly: Boolean = false
    ): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase

        val selectionList = mutableListOf<String>()
        val selectionArgsList = mutableListOf<String>()

        // Archive filter
        selectionList.add("$COL_IS_ARCHIVED = ?")
        selectionArgsList.add(if (showArchived) "1" else "0")

        // Favorites filter
        if (showFavoritesOnly) {
            selectionList.add("$COL_IS_FAVORITE = ?")
            selectionArgsList.add("1")
        }

        // Category filter
        if (!category.isNullOrEmpty() && category != "All") {
            selectionList.add("$COL_CATEGORY = ?")
            selectionArgsList.add(category)
        }

        // Search query filter (title or content)
        if (!searchQuery.isNullOrBlank()) {
            selectionList.add("($COL_TITLE LIKE ? OR $COL_CONTENT LIKE ?)")
            val term = "%${searchQuery.trim()}%"
            selectionArgsList.add(term)
            selectionArgsList.add(term)
        }

        val selection = selectionList.joinToString(" AND ")
        val selectionArgs = selectionArgsList.toTypedArray()

        // Pinned notes always appear first, followed by selected sort order
        val orderByClause = when (sortOrder) {
            PreferencesManager.SORT_OLDEST -> "$COL_IS_PINNED DESC, $COL_CREATED_AT ASC"
            PreferencesManager.SORT_TITLE -> "$COL_IS_PINNED DESC, $COL_TITLE ASC"
            PreferencesManager.SORT_UPDATED -> "$COL_IS_PINNED DESC, $COL_UPDATED_AT DESC"
            else -> "$COL_IS_PINNED DESC, $COL_CREATED_AT DESC" // SORT_NEWEST
        }

        val cursor = db.query(
            TABLE_NOTES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderByClause
        )

        cursor.use {
            while (it.moveToNext()) {
                notes.add(cursorToNote(it))
            }
        }
        return notes
    }

    fun getStats(): NoteStats {
        val db = readableDatabase
        var total = 0
        var favorites = 0
        var completed = 0

        val query = """
            SELECT 
                COUNT(*) AS total_count,
                SUM(CASE WHEN $COL_IS_FAVORITE = 1 THEN 1 ELSE 0 END) AS fav_count,
                SUM(CASE WHEN $COL_IS_COMPLETED = 1 THEN 1 ELSE 0 END) AS comp_count
            FROM $TABLE_NOTES
            WHERE $COL_IS_ARCHIVED = 0
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                total = it.getInt(0)
                favorites = it.getInt(1)
                completed = it.getInt(2)
            }
        }
        return NoteStats(totalCount = total, favoriteCount = favorites, completedCount = completed)
    }

    fun toggleFavorite(noteId: Long, isFavorite: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_FAVORITE, if (isFavorite) 1 else 0)
        }
        return db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(noteId.toString())) > 0
    }

    fun togglePin(noteId: Long, isPinned: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_PINNED, if (isPinned) 1 else 0)
        }
        return db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(noteId.toString())) > 0
    }

    fun toggleArchive(noteId: Long, isArchived: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_ARCHIVED, if (isArchived) 1 else 0)
        }
        return db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(noteId.toString())) > 0
    }

    fun toggleCompleted(noteId: Long, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(noteId.toString())) > 0
    }

    fun insertSampleNotes() {
        val samples = listOf(
            Note(
                title = "Welcome to MemoFlow!",
                content = "MemoFlow demonstrates dual Android persistence:\n\n1. SQLite Database ($TABLE_NOTES table) stores your notes, categories, and status states.\n2. SharedPreferences manages app theme, layout density, and default sorting.\n\nTry creating, editing, pinning, or favoriting this note!",
                category = "Ideas",
                isPinned = true,
                isFavorite = true
            ),
            Note(
                title = "Android Mobile Development Practical",
                content = "Prepare experiment 9 writeup covering SharedPreferences and SQLiteOpenHelper implementation. Review CRUD query execution and cursor management.",
                category = "Study",
                isPinned = false,
                isFavorite = true
            ),
            Note(
                title = "Weekly Work & Project Sync",
                content = "Discuss UI updates, database indexing, and performance optimizations with the design team.",
                category = "Work",
                isCompleted = true
            ),
            Note(
                title = "Grocery List & Household Items",
                content = "• Fresh milk & almond milk\n• Whole grain bread\n• Organic eggs\n• Dark roast coffee beans",
                category = "Personal"
            )
        )

        for (sample in samples) {
            insertNote(sample)
        }
    }

    private fun cursorToNote(cursor: Cursor): Note {
        return Note(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
            content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT)),
            isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_FAVORITE)) == 1,
            isPinned = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_PINNED)) == 1,
            isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ARCHIVED)) == 1,
            isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED)) == 1
        )
    }

    companion object {
        private const val DATABASE_NAME = "memoflow.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NOTES = "notes"
        const val COL_ID = "_id"
        const val COL_TITLE = "title"
        const val COL_CONTENT = "content"
        const val COL_CATEGORY = "category"
        const val COL_CREATED_AT = "created_at"
        const val COL_UPDATED_AT = "updated_at"
        const val COL_IS_FAVORITE = "is_favorite"
        const val COL_IS_PINNED = "is_pinned"
        const val COL_IS_ARCHIVED = "is_archived"
        const val COL_IS_COMPLETED = "is_completed"
    }
}