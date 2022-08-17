package com.test.notes.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.test.notes.db.NoteDB
import com.test.notes.model.Note
import com.test.notes.utils.OperationStatus
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class NoteRepository @Inject constructor(private val noteDB: NoteDB) {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    private val _status = MutableLiveData<OperationStatus<Boolean>>()
    val status: LiveData<OperationStatus<Boolean>> get() = _status

    suspend fun createNote(note: Note) {
        _status.postValue(OperationStatus.LOADING())
        noteDB.noteDao().addNote(note)
        _status.postValue(OperationStatus.SUCCESS(true))
    }

    suspend fun updateNote(note: Note) {
        _status.postValue(OperationStatus.LOADING())
        noteDB.noteDao().updateNote(note)
        _status.postValue(OperationStatus.SUCCESS(true))
    }

    suspend fun deleteNote(note: Note) {
        _status.postValue(OperationStatus.LOADING())
        noteDB.noteDao().deleteNote(note)
        _status.postValue(OperationStatus.SUCCESS(true))
    }

    suspend fun getNotes() {
        noteDB.noteDao().getAllNotes().collect {
            _notes.postValue(it)
        }
    }

}