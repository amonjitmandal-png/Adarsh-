package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoHelper {
    suspend fun saveImageUriToInternalStorage(context: Context, sourceUri: Uri, prefix: String = "student"): String? =
        withContext(Dispatchers.IO) {
            try {
                val directory = File(context.filesDir, "attendance_photos").apply {
                    if (!exists()) mkdirs()
                }
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val targetFile = File(directory, "${prefix}_${timeStamp}.jpg")

                val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
                inputStream?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, prefix: String = "student"): String? =
        withContext(Dispatchers.IO) {
            try {
                val directory = File(context.filesDir, "attendance_photos").apply {
                    if (!exists()) mkdirs()
                }
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val targetFile = File(directory, "${prefix}_${timeStamp}.jpg")

                FileOutputStream(targetFile).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }
                targetFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
