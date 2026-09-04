package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedLight
import com.example.ui.theme.LateAmber
import com.example.ui.theme.LateAmberLight
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenLight
import java.io.File

@Composable
fun StudentCard(
    student: Student,
    attendanceRecord: AttendanceRecord?,
    overallStats: Pair<Int, Int>, // attended, total
    onStatusChange: (AttendanceStatus) -> Unit,
    onPhotoClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currentStatus = attendanceRecord?.status
    var menuExpanded by remember { mutableStateOf(false) }

    // Status colors
    val cardBorderColor = when (currentStatus) {
        AttendanceStatus.PRESENT.name -> PresentGreen.copy(alpha = 0.6f)
        AttendanceStatus.ABSENT.name -> AbsentRed.copy(alpha = 0.6f)
        AttendanceStatus.LATE.name -> LateAmber.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    val cardBg = when (currentStatus) {
        AttendanceStatus.PRESENT.name -> PresentGreenLight.copy(alpha = 0.35f)
        AttendanceStatus.ABSENT.name -> AbsentRedLight.copy(alpha = 0.35f)
        AttendanceStatus.LATE.name -> LateAmberLight.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Student Photo with Photo trigger
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onPhotoClick() }
                        .testTag("student_photo_${student.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!student.photoUri.isNullOrEmpty()) {
                        val file = File(student.photoUri)
                        val model = if (file.exists()) file else student.photoUri
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(model)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Photo of ${student.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Initials fallback
                        val initials = student.name.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                        Text(
                            text = initials.ifEmpty { "S" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Mini camera badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Update photo",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Student Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "#${student.rollNumber}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${student.standard} • ${student.section}" +
                                if (student.contactNumber.isNotBlank()) " • 📞 ${student.contactNumber}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Attendance statistics badge
                    val (attended, total) = overallStats
                    val percentage = if (total > 0) (attended * 100) / total else 100
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Record: $attended/$total days ($percentage%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (percentage >= 75) PresentGreen else AbsentRed
                        )
                    }
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("student_menu_${student.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Student options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Photo") },
                            leadingIcon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onPhotoClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Student") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Student", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance Status Action Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Present Button
                val isPresent = currentStatus == AttendanceStatus.PRESENT.name
                AttendanceActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("present_btn_${student.id}"),
                    label = "Present",
                    isSelected = isPresent,
                    activeColor = PresentGreen,
                    activeBg = PresentGreenLight,
                    icon = Icons.Default.Check,
                    onClick = { onStatusChange(AttendanceStatus.PRESENT) }
                )

                // Absent Button
                val isAbsent = currentStatus == AttendanceStatus.ABSENT.name
                AttendanceActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("absent_btn_${student.id}"),
                    label = "Absent",
                    isSelected = isAbsent,
                    activeColor = AbsentRed,
                    activeBg = AbsentRedLight,
                    icon = Icons.Default.Close,
                    onClick = { onStatusChange(AttendanceStatus.ABSENT) }
                )

                // Late Button
                val isLate = currentStatus == AttendanceStatus.LATE.name
                AttendanceActionButton(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("late_btn_${student.id}"),
                    label = "Late",
                    isSelected = isLate,
                    activeColor = LateAmber,
                    activeBg = LateAmberLight,
                    icon = Icons.Default.Schedule,
                    onClick = { onStatusChange(AttendanceStatus.LATE) }
                )
            }
        }
    }
}

@Composable
fun AttendanceActionButton(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
