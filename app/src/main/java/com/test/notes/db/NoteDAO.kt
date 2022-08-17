package com.test.notes.db

import androidx.room.*
import com.test.notes.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDAO {
    @Insert
    suspend fun addNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM Note ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
}