package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.ui.components.AddEditStudentDialog
import com.example.ui.components.AttendanceHeader
import com.example.ui.components.PhotoPickerDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.SessionProofDialog
import com.example.ui.components.StudentCard
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.PresentGreen
import com.example.util.PhotoHelper
import kotlinx.coroutines.launch

enum class PhotoTarget {
    STUDENT_PHOTO_PICKER,
    STUDENT_ADD_EDIT_DIALOG,
    SESSION_PROOF
}

@Composable
fun MainScreen(
    viewModel: AttendanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedStandard by viewModel.selectedStandard.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val students by viewModel.filteredStudents.collectAsState()
    val allStudentsInStandard by viewModel.studentsInStandard.collectAsState()
    val attendanceMap by viewModel.attendanceForCurrentSession.collectAsState()
    val sessionProof by viewModel.sessionProof.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Dialog States
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var studentForPhotoPicker by remember { mutableStateOf<Student?>(null) }
    var studentForAddEdit by remember { mutableStateOf<Student?>(null) }
    var isAddingStudent by remember { mutableStateOf(false) }
    var pendingStudentPhotoUri by remember { mutableStateOf<String?>(null) }
    var showSessionProofDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    // Active photo target
    var activePhotoTarget by remember { mutableStateOf(PhotoTarget.STUDENT_PHOTO_PICKER) }

    // Android Photo Picker launcher (zero permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                when (activePhotoTarget) {
                    PhotoTarget.STUDENT_PHOTO_PICKER -> {
                        studentForPhotoPicker?.let { student ->
                            val path = PhotoHelper.saveImageUriToInternalStorage(context, uri, "student_${student.id}")
                            if (path != null) {
                                viewModel.updateStudentPhoto(student.id, path)
                                snackbarHostState.showSnackbar("Photo updated for ${student.name}")
                            }
                        }
                    }
                    PhotoTarget.STUDENT_ADD_EDIT_DIALOG -> {
                        val path = PhotoHelper.saveImageUriToInternalStorage(context, uri, "new_student")
                        pendingStudentPhotoUri = path
                    }
                    PhotoTarget.SESSION_PROOF -> {
                        val path = PhotoHelper.saveImageUriToInternalStorage(context, uri, "session_${selectedStandard}_${selectedDate}")
                        if (path != null) {
                            viewModel.saveSessionProofPhoto(path)
                            snackbarHostState.showSnackbar("Class session photo saved")
                        }
                    }
                }
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            scope.launch {
                when (activePhotoTarget) {
                    PhotoTarget.STUDENT_PHOTO_PICKER -> {
                        studentForPhotoPicker?.let { student ->
                            val path = PhotoHelper.saveBitmapToInternalStorage(context, bitmap, "student_${student.id}")
                            if (path != null) {
                                viewModel.updateStudentPhoto(student.id, path)
                                snackbarHostState.showSnackbar("Camera photo saved for ${student.name}")
                            }
                        }
                    }
                    PhotoTarget.STUDENT_ADD_EDIT_DIALOG -> {
                        val path = PhotoHelper.saveBitmapToInternalStorage(context, bitmap, "new_student")
                        pendingStudentPhotoUri = path
                    }
                    PhotoTarget.SESSION_PROOF -> {
                        val path = PhotoHelper.saveBitmapToInternalStorage(context, bitmap, "session_${selectedStandard}_${selectedDate}")
                        if (path != null) {
                            viewModel.saveSessionProofPhoto(path)
                            snackbarHostState.showSnackbar("Class session photo captured")
                        }
                    }
                }
            }
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is needed to take a live photo")
            }
        }
    }

    fun requestCameraCapture(target: PhotoTarget) {
        activePhotoTarget = target
        val hasCamPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasCamPermission) {
            cameraLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun requestPhotoPick(target: PhotoTarget) {
        activePhotoTarget = target
        photoPickerLauncher.launch(
            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AttendanceHeader(
                selectedStandard = selectedStandard,
                onStandardSelected = { viewModel.setStandard(it) },
                selectedDate = selectedDate,
                onDateChanged = { viewModel.setDate(it) },
                summary = summary,
                hasSessionPhoto = sessionProof != null,
                onOpenPrivacyPolicy = { showPrivacyPolicy = true },
                onOpenSessionPhoto = { showSessionProofDialog = true },
                onMarkAllPresent = {
                    viewModel.markAll(AttendanceStatus.PRESENT)
                    scope.launch { snackbarHostState.showSnackbar("All students marked Present for $selectedStandard") }
                },
                onMarkAllAbsent = {
                    viewModel.markAll(AttendanceStatus.ABSENT)
                    scope.launch { snackbarHostState.showSnackbar("All students marked Absent for $selectedStandard") }
                },
                onAddNewStudent = {
                    isAddingStudent = true
                    pendingStudentPhotoUri = null
                },
                onShareReport = {
                    shareAttendanceReport(context, selectedStandard, selectedDate, summary, allStudentsInStandard, attendanceMap)
                },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    isAddingStudent = true
                    pendingStudentPhotoUri = null
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Student") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_student")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (students.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty()) "No students found matching '$searchQuery'" else "No students enrolled in Class $selectedStandard yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add students with their photos to start taking daily attendance for MSc Center Bhavnagar.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            isAddingStudent = true
                            pendingStudentPhotoUri = null
                        },
                        modifier = Modifier.testTag("empty_add_student_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Student to $selectedStandard")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("students_list"),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        val record = attendanceMap[student.id]
                        val stats = viewModel.getStudentStats(student.id)

                        StudentCard(
                            student = student,
                            attendanceRecord = record,
                            overallStats = stats,
                            onStatusChange = { newStatus ->
                                viewModel.markStatus(student.id, newStatus)
                            },
                            onPhotoClick = {
                                studentForPhotoPicker = student
                            },
                            onEditClick = {
                                studentForAddEdit = student
                                pendingStudentPhotoUri = student.photoUri
                            },
                            onDeleteClick = {
                                studentToDelete = student
                            }
                        )
                    }
                }
            }
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicy = false }
        )
    }

    // Photo Picker Dialog for existing student
    studentForPhotoPicker?.let { student ->
        PhotoPickerDialog(
            student = student,
            onDismiss = { studentForPhotoPicker = null },
            onPickFromGallery = {
                requestPhotoPick(PhotoTarget.STUDENT_PHOTO_PICKER)
            },
            onTakePhoto = {
                requestCameraCapture(PhotoTarget.STUDENT_PHOTO_PICKER)
            },
            onRemovePhoto = {
                viewModel.updateStudentPhoto(student.id, "")
                scope.launch { snackbarHostState.showSnackbar("Photo removed") }
            }
        )
    }

    // Add or Edit Student Dialog
    if (isAddingStudent || studentForAddEdit != null) {
        val editingStudent = studentForAddEdit
        val nextRoll = (allStudentsInStandard.maxOfOrNull { it.rollNumber } ?: 0) + 1

        AddEditStudentDialog(
            initialStudent = editingStudent,
            defaultStandard = selectedStandard,
            nextRollNumber = nextRoll,
            pendingPhotoUri = pendingStudentPhotoUri,
            onDismiss = {
                isAddingStudent = false
                studentForAddEdit = null
                pendingStudentPhotoUri = null
            },
            onSave = { savedStudent ->
                viewModel.saveStudent(savedStudent)
                isAddingStudent = false
                studentForAddEdit = null
                pendingStudentPhotoUri = null
                scope.launch {
                    snackbarHostState.showSnackbar("Student ${savedStudent.name} saved successfully")
                }
            },
            onRequestPhoto = {
                activePhotoTarget = PhotoTarget.STUDENT_ADD_EDIT_DIALOG
                // Show choice: pick photo
                requestPhotoPick(PhotoTarget.STUDENT_ADD_EDIT_DIALOG)
            }
        )
    }

    // Session Proof Dialog (Classroom photo verification)
    if (showSessionProofDialog) {
        SessionProofDialog(
            standard = selectedStandard,
            date = selectedDate,
            currentProof = sessionProof,
            onDismiss = { showSessionProofDialog = false },
            onTakePhoto = {
                requestCameraCapture(PhotoTarget.SESSION_PROOF)
            },
            onPickFromGallery = {
                requestPhotoPick(PhotoTarget.SESSION_PROOF)
            },
            onSaveNotes = { notes ->
                sessionProof?.let { current ->
                    viewModel.saveSessionProofPhoto(current.photoUri, notes)
                } ?: viewModel.saveSessionProofPhoto("", notes)
            }
        )
    }

    // Delete Student Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Remove Student?") },
            text = { Text("Are you sure you want to remove ${student.name} (Roll #${student.rollNumber}) from Class ${student.standard}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        studentToDelete = null
                        scope.launch { snackbarHostState.showSnackbar("Student removed") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun shareAttendanceReport(
    context: Context,
    standard: String,
    date: String,
    summary: AttendanceSummary,
    students: List<Student>,
    attendanceMap: Map<Long, com.example.data.model.AttendanceRecord>
) {
    val absentStudents = students.filter { attendanceMap[it.id]?.status == AttendanceStatus.ABSENT.name }
    val presentStudents = students.filter { attendanceMap[it.id]?.status == AttendanceStatus.PRESENT.name }
    val lateStudents = students.filter { attendanceMap[it.id]?.status == AttendanceStatus.LATE.name }

    val report = buildString {
        append("🏫 *MSc Center Bhavnagar - Attendance Report*\n")
        append("📅 *Date:* $date\n")
        append("🎓 *Class:* $standard\n")
        append("━━━━━━━━━━━━━━━━━━━━\n")
        append("👥 Total Enrolled: ${summary.totalStudents}\n")
        append("✅ Present: ${summary.presentCount}\n")
        append("❌ Absent: ${summary.absentCount}\n")
        append("⏳ Late: ${summary.lateCount}\n")
        append("📊 Attendance Rate: ${summary.percentage.toInt()}%\n")
        append("━━━━━━━━━━━━━━━━━━━━\n\n")

        if (absentStudents.isNotEmpty()) {
            append("❌ *ABSENT STUDENTS (${absentStudents.size}):*\n")
            absentStudents.forEachIndexed { i, s ->
                val phone = if (s.contactNumber.isNotBlank()) " | Ph: ${s.contactNumber}" else ""
                append("${i + 1}. Roll #${s.rollNumber} - ${s.name} (${s.section})$phone\n")
            }
            append("\n")
        }

        if (lateStudents.isNotEmpty()) {
            append("⏳ *LATE ARRIVALS (${lateStudents.size}):*\n")
            lateStudents.forEachIndexed { i, s ->
                append("${i + 1}. Roll #${s.rollNumber} - ${s.name}\n")
            }
            append("\n")
        }

        append("📌 Verified by Center Admin • MSc Center Bhavnagar")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "MSc Center Bhavnagar - $standard Attendance ($date)")
        putExtra(Intent.EXTRA_TEXT, report)
    }

    context.startActivity(Intent.createChooser(intent, "Share Attendance Report via"))
}
