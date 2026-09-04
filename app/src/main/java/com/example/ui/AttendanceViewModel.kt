package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.SessionProof
import com.example.data.model.Student
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttendanceSummary(
    val totalStudents: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val percentage: Float = 0f
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AttendanceRepository(application)

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDate: String = dateFormatter.format(Date())

    private val _selectedStandard = MutableStateFlow("10th")
    val selectedStandard: StateFlow<String> = _selectedStandard.asStateFlow()

    private val _selectedDate = MutableStateFlow(todayDate)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSectionFilter = MutableStateFlow("All")
    val selectedSectionFilter: StateFlow<String> = _selectedSectionFilter.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val studentsInStandard: StateFlow<List<Student>> = _selectedStandard
        .flatMapLatest { standard ->
            repository.getStudentsByStandard(standard)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceForCurrentSession: StateFlow<Map<Long, AttendanceRecord>> = combine(
        _selectedStandard,
        _selectedDate
    ) { standard, date ->
        standard to date
    }.flatMapLatest { (standard, date) ->
        repository.getAttendanceForDateAndStandard(standard, date)
    }.combine(MutableStateFlow(Unit)) { records, _ ->
        records.associateBy { it.studentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val sessionProof: StateFlow<SessionProof?> = combine(
        _selectedStandard,
        _selectedDate
    ) { standard, date ->
        standard to date
    }.flatMapLatest { (standard, date) ->
        repository.getSessionProof(date, standard)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAttendanceRecords: StateFlow<List<AttendanceRecord>> = repository.getAllAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStudents: StateFlow<List<Student>> = combine(
        studentsInStandard,
        _searchQuery,
        _selectedSectionFilter
    ) { students, query, section ->
        students.filter { student ->
            val matchesQuery = query.isBlank() ||
                    student.name.contains(query, ignoreCase = true) ||
                    student.rollNumber.toString().contains(query)
            val matchesSection = section == "All" || student.section.contains(section, ignoreCase = true)
            matchesQuery && matchesSection
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<AttendanceSummary> = combine(
        studentsInStandard,
        attendanceForCurrentSession
    ) { students, attendanceMap ->
        val total = students.size
        var present = 0
        var absent = 0
        var late = 0
        students.forEach { student ->
            when (attendanceMap[student.id]?.status) {
                AttendanceStatus.PRESENT.name -> present++
                AttendanceStatus.ABSENT.name -> absent++
                AttendanceStatus.LATE.name -> late++
            }
        }
        val pct = if (total > 0) ((present + (late * 0.5f)) / total.toFloat()) * 100f else 0f
        AttendanceSummary(
            totalStudents = total,
            presentCount = present,
            absentCount = absent,
            lateCount = late,
            percentage = pct
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AttendanceSummary())

    fun setStandard(standard: String) {
        _selectedStandard.value = standard
        _selectedSectionFilter.value = "All"
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSectionFilter(section: String) {
        _selectedSectionFilter.value = section
    }

    fun markStatus(studentId: Long, status: AttendanceStatus, remark: String = "") {
        viewModelScope.launch {
            val record = AttendanceRecord(
                studentId = studentId,
                standard = _selectedStandard.value,
                date = _selectedDate.value,
                status = status.name,
                remark = remark,
                recordedAt = System.currentTimeMillis()
            )
            repository.saveAttendance(record)
        }
    }

    fun markAll(status: AttendanceStatus) {
        val currentStudents = studentsInStandard.value
        val currentDate = _selectedDate.value
        val currentStandard = _selectedStandard.value
        viewModelScope.launch {
            val records = currentStudents.map { student ->
                AttendanceRecord(
                    studentId = student.id,
                    standard = currentStandard,
                    date = currentDate,
                    status = status.name,
                    recordedAt = System.currentTimeMillis()
                )
            }
            repository.saveAllAttendance(records)
        }
    }

    fun saveStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0L) {
                repository.insertStudent(student)
            } else {
                repository.updateStudent(student)
            }
        }
    }

    fun updateStudentPhoto(studentId: Long, photoUri: String) {
        viewModelScope.launch {
            val student = studentsInStandard.value.find { it.id == studentId }
            if (student != null) {
                repository.updateStudent(student.copy(photoUri = photoUri))
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun saveSessionProofPhoto(photoPath: String, notes: String = "") {
        viewModelScope.launch {
            val proof = SessionProof(
                date = _selectedDate.value,
                standard = _selectedStandard.value,
                photoUri = photoPath,
                takenAt = System.currentTimeMillis(),
                notes = notes
            )
            repository.saveSessionProof(proof)
        }
    }

    fun getStudentStats(studentId: Long): Pair<Int, Int> {
        val records = allAttendanceRecords.value.filter { it.studentId == studentId }
        val totalDays = records.size
        val attendedDays = records.count { it.status == AttendanceStatus.PRESENT.name || it.status == AttendanceStatus.LATE.name }
        return Pair(attendedDays, totalDays)
    }
}
