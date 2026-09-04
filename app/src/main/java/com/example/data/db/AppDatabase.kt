package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.SessionProof
import com.example.data.model.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Student::class, AttendanceRecord::class, SessionProof::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "msc_bhavnagar_attendance.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default sample students for 9th, 10th, 11th, 12th standards
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).studentDao().insertStudents(getInitialStudents())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInitialStudents(): List<Student> {
            return listOf(
                // 9th Standard (Class 9)
                Student(rollNumber = 1, name = "Aarav Patel", standard = "9th", section = "Div A", contactNumber = "+91 98251 12345"),
                Student(rollNumber = 2, name = "Diya Shah", standard = "9th", section = "Div A", contactNumber = "+91 98252 23456"),
                Student(rollNumber = 3, name = "Harsh Vora", standard = "9th", section = "Div A", contactNumber = "+91 98253 34567"),
                Student(rollNumber = 4, name = "Kavya Gohil", standard = "9th", section = "Div B", contactNumber = "+91 98254 45678"),
                Student(rollNumber = 5, name = "Meet Trivedi", standard = "9th", section = "Div B", contactNumber = "+91 98255 56789"),
                Student(rollNumber = 6, name = "Riddhi Mehta", standard = "9th", section = "Div B", contactNumber = "+91 98256 67890"),

                // 10th Standard (Class 10 - Board)
                Student(rollNumber = 1, name = "Ananya Bhatt", standard = "10th", section = "Board Batch", contactNumber = "+91 94280 11223"),
                Student(rollNumber = 2, name = "Devansh Joshi", standard = "10th", section = "Board Batch", contactNumber = "+91 94280 22334"),
                Student(rollNumber = 3, name = "Jhanvi Dave", standard = "10th", section = "Board Batch", contactNumber = "+91 94280 33445"),
                Student(rollNumber = 4, name = "Manan Rathod", standard = "10th", section = "Board Batch", contactNumber = "+91 94280 44556"),
                Student(rollNumber = 5, name = "Pooja Parmar", standard = "10th", section = "Board Batch", contactNumber = "+91 94280 55667"),
                Student(rollNumber = 6, name = "Yashrajsinh Jadeja", standard = "10th", section = "Board Batch", contactNumber = "+91 94280 66778"),

                // 11th Standard (Class 11)
                Student(rollNumber = 1, name = "Bhargav Makwana", standard = "11th", section = "Science A", contactNumber = "+91 99790 10101"),
                Student(rollNumber = 2, name = "Isha Kothari", standard = "11th", section = "Science A", contactNumber = "+91 99790 20202"),
                Student(rollNumber = 3, name = "Karan Solanki", standard = "11th", section = "Science B", contactNumber = "+91 99790 30303"),
                Student(rollNumber = 4, name = "Nisha Chauhan", standard = "11th", section = "Commerce", contactNumber = "+91 99790 40404"),
                Student(rollNumber = 5, name = "Pranay Sanghavi", standard = "11th", section = "Commerce", contactNumber = "+91 99790 50505"),
                Student(rollNumber = 6, name = "Tanvi Oza", standard = "11th", section = "Commerce", contactNumber = "+91 99790 60606"),

                // 12th Standard (Class 12 - Board)
                Student(rollNumber = 1, name = "Ayush Doshi", standard = "12th", section = "Science HSC", contactNumber = "+91 97230 77112"),
                Student(rollNumber = 2, name = "Drashti Pandya", standard = "12th", section = "Science HSC", contactNumber = "+91 97230 77223"),
                Student(rollNumber = 3, name = "Jaydeep Mori", standard = "12th", section = "Science HSC", contactNumber = "+91 97230 77334"),
                Student(rollNumber = 4, name = "Khushi Zala", standard = "12th", section = "Commerce HSC", contactNumber = "+91 97230 77445"),
                Student(rollNumber = 5, name = "Rajveer Chudasama", standard = "12th", section = "Commerce HSC", contactNumber = "+91 97230 77556"),
                Student(rollNumber = 6, name = "Vidhi Vyas", standard = "12th", section = "Commerce HSC", contactNumber = "+91 97230 77667")
            )
        }
    }
}
