package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rollNumber: Int,
    val name: String,
    val standard: String, // "9th", "10th", "11th", "12th"
    val section: String,  // e.g. "Science", "Commerce", "General", "Div A"
    val contactNumber: String = "",
    val photoUri: String? = null,
    val active: Boolean = true
)
