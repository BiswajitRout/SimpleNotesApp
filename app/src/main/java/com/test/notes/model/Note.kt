package com.test.notes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note (
    @PrimaryKey(autoGenerate = true) val _id: String,
    var description: String,
    var title: String,
    var createdAt: Long,
    var updatedAt: Long)