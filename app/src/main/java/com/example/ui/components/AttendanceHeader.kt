package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AttendanceSummary
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.LateAmber
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PresentGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHeader(
    selectedStandard: String,
    onStandardSelected: (String) -> Unit,
    selectedDate: String,
    onDateChanged: (String) -> Unit,
    summary: AttendanceSummary,
    hasSessionPhoto: Boolean,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenSessionPhoto: () -> Unit,
    onMarkAllPresent: () -> Unit,
    onMarkAllAbsent: () -> Unit,
    onAddNewStudent: () -> Unit,
    onShareReport: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val standards = listOf("9th", "10th", "11th", "12th")
    var showSearch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "MSc Center Bhavnagar",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Classes 9th • 10th • 11th • 12th Attendance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NavyPrimary,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            ),
            actions = {
                // Search toggle
                IconButton(
                    onClick = { showSearch = !showSearch },
                    modifier = Modifier.testTag("action_search")
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Students")
                }

                // Classroom Photo Proof button
                IconButton(
                    onClick = onOpenSessionPhoto,
                    modifier = Modifier.testTag("action_session_photo")
                ) {
                    Box {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Class Photo Proof")
                        if (hasSessionPhoto) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PresentGreen)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }

                // Share button
                IconButton(
                    onClick = onShareReport,
                    modifier = Modifier.testTag("action_share_report")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share Report")
                }

                // Privacy Policy button
                IconButton(
                    onClick = onOpenPrivacyPolicy,
                    modifier = Modifier.testTag("action_privacy_policy")
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Privacy Policy")
                }
            }
        )

        // Class Selector Tabs (9th, 10th, 11th, 12th)
        val selectedIndex = standards.indexOf(selectedStandard).coerceAtLeast(0)
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            standards.forEachIndexed { index, std ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onStandardSelected(std) },
                    text = {
                        Text(
                            text = "Class $std",
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_standard_$std")
                )
            }
        }

        // Search Bar (expandable)
        AnimatedVisibility(visible = showSearch) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search by student name or roll number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.HighlightOff, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_students_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Date Picker & Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous day button
            IconButton(
                onClick = { onDateChanged(adjustDate(selectedDate, -1)) },
                modifier = Modifier.testTag("prev_date_btn")
            ) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Day")
            }

            // Current date display
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDateDisplay(selectedDate),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Next day button
            IconButton(
                onClick = { onDateChanged(adjustDate(selectedDate, 1)) },
                modifier = Modifier.testTag("next_date_btn")
            ) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Day")
            }
        }

        // Attendance Metric Summary Cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("attendance_summary_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatPill("Total", "${summary.totalStudents}", MaterialTheme.colorScheme.onSurface)
                    StatPill("Present", "${summary.presentCount}", PresentGreen)
                    StatPill("Absent", "${summary.absentCount}", AbsentRed)
                    StatPill("Late", "${summary.lateCount}", LateAmber)
                    StatPill("Rate", "${summary.percentage.toInt()}%", MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (summary.percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (summary.percentage >= 75f) PresentGreen else AbsentRed,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                )
            }
        }

        // Action Toolbar: Mark All Present, Mark All Absent, Add Student
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All Present Shortcut
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onMarkAllPresent() }
                    .testTag("mark_all_present_btn"),
                color = PresentGreen.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PresentGreen.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PresentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "All Present",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PresentGreen
                    )
                }
            }

            // All Absent Shortcut
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onMarkAllAbsent() }
                    .testTag("mark_all_absent_btn"),
                color = AbsentRed.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AbsentRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HighlightOff,
                        contentDescription = null,
                        tint = AbsentRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "All Absent",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AbsentRed
                    )
                }
            }

            // Add Student Button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAddNewStudent() }
                    .testTag("add_student_shortcut_btn"),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Student",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun adjustDate(dateStr: String, days: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return try {
        val date = sdf.parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, days)
        sdf.format(cal.time)
    } catch (e: Exception) {
        dateStr
    }
}

fun formatDateDisplay(dateStr: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    return try {
        val date = sdf.parse(dateStr) ?: return dateStr
        val today = sdf.format(java.util.Date())
        if (dateStr == today) {
            "Today (${displayFormat.format(date)})"
        } else {
            displayFormat.format(date)
        }
    } catch (e: Exception) {
        dateStr
    }
}
