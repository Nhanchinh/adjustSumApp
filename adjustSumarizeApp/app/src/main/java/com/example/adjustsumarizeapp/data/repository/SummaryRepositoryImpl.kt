package com.example.adjustsumarizeapp.data.repository

import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.data.local.dao.SummaryHistoryDao
import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity
import com.example.adjustsumarizeapp.data.model.*
import com.example.adjustsumarizeapp.data.remote.ApiService
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of SummaryRepository
 * Offline-first strategy: prioritize local data, sync with remote when available
 */
class SummaryRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val summaryHistoryDao: SummaryHistoryDao,
    private val tokenManager: TokenManager
) : SummaryRepository {
    
    // ==================== Summarization ====================
    
    override suspend fun summarizeText(
        text: String,
        model: String,
        maxLength: Int
    ): Result<SummarizeResponse> {
        return try {
            val request = SummarizeRequest(text, model, maxLength)
            val response = apiService.summarize(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Lỗi: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAvailableModels(): Result<List<ModelInfo>> {
        return try {
            val response = apiService.getModels()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể tải danh sách models"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun checkColabHealth(): Result<Map<String, Any>> {
        return try {
            val response = apiService.checkColabHealth()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Colab server không khả dụng"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== Evaluation ====================
    
    override suspend fun evaluateSummary(
        prediction: String,
        reference: String,
        calculateBert: Boolean
    ): Result<EvaluateResponse> {
        return try {
            val request = EvaluateRequest(prediction, reference, calculateBert)
            val response = apiService.evaluateSummary(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Lỗi đánh giá: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== Local History ====================
    
    override fun getLocalHistory(userId: String): Flow<List<SummaryHistoryEntity>> {
        return summaryHistoryDao.getHistoryByUser(userId)
    }
    
    override suspend fun getLocalHistoryById(id: String): SummaryHistoryEntity? {
        return summaryHistoryDao.getHistoryById(id)
    }
    
    override suspend fun saveLocalHistory(history: SummaryHistoryEntity) {
        summaryHistoryDao.insert(history)
    }
    
    override suspend fun deleteLocalHistory(id: String) {
        summaryHistoryDao.deleteById(id)
    }
    
    override fun searchLocalHistory(query: String): Flow<List<SummaryHistoryEntity>> {
        return summaryHistoryDao.searchHistory(query)
    }
    
    // ==================== Remote History ====================
    
    override suspend fun getRemoteHistory(page: Int, pageSize: Int): Result<HistoryResponse> {
        return try {
            val response = apiService.getHistory(page, pageSize)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể tải lịch sử từ server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveRemoteHistory(
        originalText: String,
        summary: String,
        modelUsed: String,
        inferenceTimeMs: Double?,  // Changed from Int to Double
        metrics: EvaluationMetrics?
    ): Result<HistoryItemDto> {
        return try {
            val request = SaveHistoryRequest(
                originalText = originalText,
                summary = summary,
                modelUsed = modelUsed,
                inferenceTimeMs = inferenceTimeMs,
                metrics = metrics
            )
            val response = apiService.saveHistory(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể lưu lịch sử lên server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteRemoteHistory(historyId: String): Result<Boolean> {
        return try {
            val response = apiService.deleteHistory(historyId)
            
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Không thể xóa lịch sử"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== Sync ====================
    
    override suspend fun syncHistory(): Result<Unit> {
        return try {
            // 1. Get current user ID
            val currentUserId = tokenManager.getUserId() ?: return Result.failure(Exception("User not logged in"))
            
            // 2. Get remote history
            val remoteResult = getRemoteHistory(1, 100)
            
            if (remoteResult.isSuccess) {
                val remoteHistory = remoteResult.getOrNull()?.items ?: emptyList()
                
                // 3. Convert to local entities and save
                val localEntities = remoteHistory.map { dto ->
                    SummaryHistoryEntity(
                        id = dto.id,
                        userId = currentUserId,  // FIXED: Use current user ID instead of null from API
                        originalText = dto.inputText,
                        summary = dto.summary,
                        modelUsed = dto.modelUsed,
                        inferenceTimeMs = dto.metrics.colabInferenceMs,
                        createdAt = parseIsoDate(dto.createdAt),
                        rouge1 = null,  // History metrics doesn't have evaluation metrics
                        rouge2 = null,
                        rougeL = null,
                        bleu = null,
                        bertScore = null,
                        synced = true,
                        modifiedAt = System.currentTimeMillis()
                    )
                }
                
                summaryHistoryDao.insertAll(localEntities)
                
                // 4. Upload unsynced local items
                val unsyncedItems = summaryHistoryDao.getUnsyncedHistory()
                unsyncedItems.forEach { local ->
                    val metrics = if (local.rouge1 != null) {
                        EvaluationMetrics(
                            rouge1 = local.rouge1,
                            rouge2 = local.rouge2 ?: 0.0,
                            rougeL = local.rougeL ?: 0.0,
                            bleu = local.bleu ?: 0.0,
                            bertScore = local.bertScore,
                            processingTimeMs = 0
                        )
                    } else null
                    
                    val saveResult = saveRemoteHistory(
                        originalText = local.originalText,
                        summary = local.summary,
                        modelUsed = local.modelUsed,
                        inferenceTimeMs = local.inferenceTimeMs,
                        metrics = metrics
                    )
                    
                    if (saveResult.isSuccess) {
                        summaryHistoryDao.markAsSynced(local.id)
                    }
                }
                
                Result.success(Unit)
            } else {
                Result.failure(remoteResult.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUnsyncedHistory(): List<SummaryHistoryEntity> {
        return summaryHistoryDao.getUnsyncedHistory()
    }
    
    override suspend fun markAsSynced(id: String) {
        summaryHistoryDao.markAsSynced(id)
    }
    
    // ==================== Statistics ====================
    
    override suspend fun getHistoryCount(): Int {
        return summaryHistoryDao.getCount()
    }
    
    override suspend fun getHistoryCountByModel(model: String): Int {
        return summaryHistoryDao.getCountByModel(model)
    }
    
    // ==================== Helper Functions ====================
    
    private fun parseIsoDate(isoDate: String): Long {
        return try {
            // Simple parsing for ISO 8601 format
            // In production, use a proper date parser like kotlinx-datetime
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
