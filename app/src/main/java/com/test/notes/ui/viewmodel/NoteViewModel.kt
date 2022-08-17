package com.test.notes.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.notes.model.Note
import com.test.notes.repository.NoteRepository
import com.test.notes.utils.OperationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val noteRepository: NoteRepository) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = noteRepository.notes

    private val _status = MutableLiveData<OperationStatus<Boolean>>()
    val status: LiveData<OperationStatus<Boolean>> get() = noteRepository.status

    fun createNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.createNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note)
        }
    }

    fun getNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.getNotes()
        }
    }
}