package com.example.adjustsumarizeapp.domain.repository

import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity
import com.example.adjustsumarizeapp.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Summary operations
 * Combines local (Room) and remote (API) data sources
 */
interface SummaryRepository {
    
    // Summarization
    suspend fun summarizeText(text: String, model: String, maxLength: Int): Result<SummarizeResponse>
    
    suspend fun getAvailableModels(): Result<List<ModelInfo>>
    
    suspend fun checkColabHealth(): Result<ColabHealthResponse>
    
    // Evaluation
    suspend fun evaluateSummary(
        prediction: String,
        reference: String,
        calculateBert: Boolean = false
    ): Result<EvaluateResponse>
    
    // History - Local (offline-first)
    fun getLocalHistory(userId: String): Flow<List<SummaryHistoryEntity>>
    
    suspend fun getLocalHistoryById(id: String): SummaryHistoryEntity?
    
    suspend fun saveLocalHistory(history: SummaryHistoryEntity)
    
    suspend fun deleteLocalHistory(id: String)
    
    fun searchLocalHistory(query: String): Flow<List<SummaryHistoryEntity>>
    
    // History - Remote
    suspend fun getRemoteHistory(page: Int = 1, pageSize: Int = 20): Result<HistoryResponse>
    
    suspend fun saveRemoteHistory(
        originalText: String,
        summary: String,
        modelUsed: String,
        inferenceTimeMs: Double?,  // Changed from Int to Double
        metrics: EvaluationMetrics?
    ): Result<HistoryItemDto>
    
    suspend fun deleteRemoteHistory(historyId: String): Result<Boolean>
    
    // Sync
    suspend fun syncHistory(): Result<Unit>
    
    suspend fun getUnsyncedHistory(): List<SummaryHistoryEntity>
    
    suspend fun markAsSynced(id: String)
    
    // Statistics
    suspend fun getHistoryCount(): Int
    
    suspend fun getHistoryCountByModel(model: String): Int
}
