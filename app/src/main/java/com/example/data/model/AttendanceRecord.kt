package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE
}

@Entity(
    tableName = "attendance_records",
    indices = [Index(value = ["studentId", "date"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val standard: String,
    val date: String, // format: "yyyy-MM-dd"
    val status: String, // "PRESENT", "ABSENT", "LATE"
    val remark: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "session_proofs",
    primaryKeys = ["date", "standard"]
)
data class SessionProof(
    val date: String,
    val standard: String,
    val photoUri: String,
    val takenAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
