package com.test.notes.di

import android.content.Context
import androidx.room.Room
import com.test.notes.db.NoteDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun getDb(@ApplicationContext context: Context): NoteDB {
        return Room.databaseBuilder(context, NoteDB::class.java, "NoteDB")
            .build()
    }
}