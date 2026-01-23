package com.example.adjustsumarizeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.adjustsumarizeapp.data.local.dao.SummaryHistoryDao
import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val token: String? = null
)

@Database(
    entities = [
        UserEntity::class,
        SummaryHistoryEntity::class
    ],
    version = 4,  // Increased version: userId nullable + Int -> Double
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun summaryHistoryDao(): SummaryHistoryDao
    // abstract fun userDao(): UserDao
}
