package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceRecord
import com.example.data.model.SessionProof
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE standard = :standard AND date = :date")
    fun getAttendanceForDateAndStandard(standard: String, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAllAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId AND date = :date")
    suspend fun deleteAttendanceRecord(studentId: Long, date: String)

    @Query("SELECT * FROM session_proofs WHERE date = :date AND standard = :standard LIMIT 1")
    fun getSessionProof(date: String, standard: String): Flow<SessionProof?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSessionProof(proof: SessionProof)
}
