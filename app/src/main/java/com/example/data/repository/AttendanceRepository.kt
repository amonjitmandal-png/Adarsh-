package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.SessionProof
import com.example.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AttendanceRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val studentDao = db.studentDao()
    private val attendanceDao = db.attendanceDao()

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        if (studentDao.getStudentCount() == 0) {
            studentDao.insertStudents(AppDatabase.getInitialStudents())
        }
    }

    fun getStudentsByStandard(standard: String): Flow<List<Student>> {
        return studentDao.getStudentsByStandard(standard)
    }

    fun getAllStudents(): Flow<List<Student>> {
        return studentDao.getAllStudents()
    }

    suspend fun insertStudent(student: Student): Long = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.deleteStudent(student)
    }

    fun getAttendanceForDateAndStandard(standard: String, date: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceForDateAndStandard(standard, date)
    }

    fun getAllAttendance(): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAllAttendance()
    }

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAttendanceForStudent(studentId)
    }

    suspend fun saveAttendance(record: AttendanceRecord) = withContext(Dispatchers.IO) {
        attendanceDao.insertOrUpdateAttendance(record)
    }

    suspend fun saveAllAttendance(records: List<AttendanceRecord>) = withContext(Dispatchers.IO) {
        attendanceDao.insertOrUpdateAll(records)
    }

    fun getSessionProof(date: String, standard: String): Flow<SessionProof?> {
        return attendanceDao.getSessionProof(date, standard)
    }

    suspend fun saveSessionProof(proof: SessionProof) = withContext(Dispatchers.IO) {
        attendanceDao.saveSessionProof(proof)
    }
}
