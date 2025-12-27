package com.readboost.id.data.local.dao

import androidx.room.*
import com.readboost.id.data.model.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes WHERE articleId = :articleId ORDER BY date DESC")
    fun getNotesByArticle(articleId: Int): Flow<List<Notes>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Notes?

    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Notes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Notes): Long

    @Update
    suspend fun updateNote(note: Notes)

    @Delete
    suspend fun deleteNote(note: Notes)

    @Query("DELETE FROM notes WHERE articleId = :articleId")
    suspend fun deleteNotesByArticle(articleId: Int)
}

