package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Student
import java.io.File

@Composable
fun AddEditStudentDialog(
    initialStudent: Student? = null,
    defaultStandard: String = "10th",
    nextRollNumber: Int = 1,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit,
    onRequestPhoto: () -> Unit,
    pendingPhotoUri: String? = null
) {
    var rollNumberText by remember { mutableStateOf(initialStudent?.rollNumber?.toString() ?: nextRollNumber.toString()) }
    var nameText by remember { mutableStateOf(initialStudent?.name ?: "") }
    var selectedStandard by remember { mutableStateOf(initialStudent?.standard ?: defaultStandard) }
    var sectionText by remember {
        mutableStateOf(
            initialStudent?.section ?: if (defaultStandard in listOf("11th", "12th")) "Science A" else "Div A"
        )
    }
    var contactNumberText by remember { mutableStateOf(initialStudent?.contactNumber ?: "") }
    val photoUri = pendingPhotoUri ?: initialStudent?.photoUri

    val standards = listOf("9th", "10th", "11th", "12th")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("add_edit_student_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialStudent == null) "Add Student" else "Edit Student",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Student Photo Avatar Button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onRequestPhoto() }
                        .testTag("dialog_student_photo"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoUri.isNullOrEmpty()) {
                        val file = File(photoUri)
                        val model = if (file.exists()) file else photoUri
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(model)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Photo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = "Tap to add student photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Standard Selector Chips (9th, 10th, 11th, 12th)
                Text(
                    text = "Select Class / Standard",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    standards.forEach { std ->
                        FilterChip(
                            selected = selectedStandard == std,
                            onClick = {
                                selectedStandard = std
                                if (std in listOf("11th", "12th") && (sectionText == "Div A" || sectionText == "Div B")) {
                                    sectionText = "Science"
                                }
                            },
                            label = { Text(std) },
                            modifier = Modifier.weight(1f).testTag("dialog_chip_$std")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Roll Number & Section in a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rollNumberText,
                        onValueChange = { rollNumberText = it },
                        label = { Text("Roll No.") },
                        modifier = Modifier
                            .weight(0.4f)
                            .testTag("dialog_input_roll"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sectionText,
                        onValueChange = { sectionText = it },
                        label = { Text("Stream / Batch") },
                        placeholder = { Text("e.g. Science / Div A") },
                        modifier = Modifier
                            .weight(0.6f)
                            .testTag("dialog_input_section"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full Name
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Student Full Name *") },
                    placeholder = { Text("e.g. Aarav Patel") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Guardian Contact
                OutlinedTextField(
                    value = contactNumberText,
                    onValueChange = { contactNumberText = it },
                    label = { Text("Guardian Phone (WhatsApp)") },
                    placeholder = { Text("+91 98250 12345") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_phone"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val roll = rollNumberText.toIntOrNull() ?: nextRollNumber
                            val student = (initialStudent ?: Student(
                                rollNumber = roll,
                                name = nameText.trim(),
                                standard = selectedStandard,
                                section = sectionText.trim()
                            )).copy(
                                rollNumber = roll,
                                name = nameText.trim().ifEmpty { "Student #$roll" },
                                standard = selectedStandard,
                                section = sectionText.trim().ifEmpty { "General" },
                                contactNumber = contactNumberText.trim(),
                                photoUri = photoUri
                            )
                            onSave(student)
                        },
                        modifier = Modifier.testTag("dialog_save_student_btn"),
                        enabled = nameText.isNotBlank()
                    ) {
                        Text("Save Student")
                    }
                }
            }
        }
    }
}
