package com.example.adjustsumarizeapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing summary history locally
 */
@Entity(tableName = "summary_history")
data class SummaryHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String?,  // Nullable - backend không trả về trong list
    
    @ColumnInfo(name = "original_text")
    val originalText: String,
    
    @ColumnInfo(name = "summary")
    val summary: String,
    
    @ColumnInfo(name = "model_used")
    val modelUsed: String,
    
    @ColumnInfo(name = "inference_time_ms")
    val inferenceTimeMs: Double?,  // Changed from Int to Double
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Timestamp in milliseconds
    
    // Evaluation metrics (nullable)
    @ColumnInfo(name = "rouge1")
    val rouge1: Double?,
    
    @ColumnInfo(name = "rouge2")
    val rouge2: Double?,
    
    @ColumnInfo(name = "rougeL")
    val rougeL: Double?,
    
    @ColumnInfo(name = "bleu")
    val bleu: Double?,
    
    @ColumnInfo(name = "bert_score")
    val bertScore: Double?,
    
    // Sync status
    @ColumnInfo(name = "synced")
    val synced: Boolean = false,
    
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long = System.currentTimeMillis()
)
