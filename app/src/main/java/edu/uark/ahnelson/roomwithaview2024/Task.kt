package edu.uark.ahnelson.roomwithaview2024

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Data class representing a Word entity in the Room database
@Entity(tableName = "task_table") // Defines the table name for the database entity
data class Task(
    // Primary key for the Word entity, auto-generates an ID when a new Word is added
    @PrimaryKey(autoGenerate = true) val id: Long? = null,

    // Column in the database for storing the word, with the specified name "word"
    @ColumnInfo(name = "task") val task: String,

    @ColumnInfo(name = "notes") val notes: String,

    @ColumnInfo(name = "due_date") val dueDate: Long?, // New field for storing the due date

    @ColumnInfo(name = "completed") val completed: Boolean = false

)
