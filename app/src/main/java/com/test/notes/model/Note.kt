package com.test.notes.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "note")
data class Note (
    @PrimaryKey(autoGenerate = true) val _id: Int,
    var description: String,
    var title: String,
    var image: String,
    var isEdited: Boolean,
    var createdAt: Date,
    var updatedAt: Date)