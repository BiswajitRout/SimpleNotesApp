package com.test.notes.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.test.notes.db.converter.DateConverter
import com.test.notes.model.Note

@Database(entities = [Note::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class NoteDB: RoomDatabase() {
    abstract fun noteDao(): NoteDAO
}