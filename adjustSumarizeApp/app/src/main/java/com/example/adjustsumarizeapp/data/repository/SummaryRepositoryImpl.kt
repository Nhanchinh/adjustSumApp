package com.example.adjustsumarizeapp.data.repository

import android.util.Log
import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.data.local.dao.SummaryHistoryDao
import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity
import com.example.adjustsumarizeapp.data.model.*
import com.example.adjustsumarizeapp.data.remote.ApiService
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
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
    
    override suspend fun checkColabHealth(): Result<ColabHealthResponse> {
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
            // Calculate word counts
            val inputWords = originalText.trim().split("\\s+".toRegex()).size
            val outputWords = summary.trim().split("\\s+".toRegex()).size
            
            // Calculate compression ratio
            val compressionRatio = if (inputWords > 0) {
                ((outputWords.toFloat() / inputWords.toFloat()) * 100)
            } else {
                0f
            }
            
            val request = SaveHistoryRequest(
                inputText = originalText,
                summary = summary,
                modelUsed = modelUsed,
                inputWords = inputWords,
                outputWords = outputWords,
                compressionRatio = compressionRatio,
                processingTimeMs = inferenceTimeMs?.toInt() ?: 0,
                colabInferenceMs = inferenceTimeMs?.toFloat()
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
            
            // 2. Upload unsynced local items FIRST (before pulling remote)
            val unsyncedItems = summaryHistoryDao.getUnsyncedHistory()
            Log.d("SyncHistory", "Found ${unsyncedItems.size} unsynced items to push")
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
                
                // Delete local item regardless of success/failure:
                // - Success: remote now has it with a real MongoDB ID, pull will re-create it
                // - Failure (422 etc.): stale data with invalid model, no point keeping it
                summaryHistoryDao.deleteById(local.id)
                if (saveResult.isFailure) {
                    Log.w("SyncHistory", "Failed to push item ${local.id} (model=${local.modelUsed}), removed stale local data: ${saveResult.exceptionOrNull()?.message}")
                }
            }
            
            // 3. Fetch ALL remote history (handle pagination)
            val allRemoteItems = mutableListOf<HistoryItemDto>()
            var currentPage = 1
            val pageSize = 100
            
            while (true) {
                val remoteResult = getRemoteHistory(currentPage, pageSize)
                if (remoteResult.isFailure) {
                    return Result.failure(remoteResult.exceptionOrNull() ?: Exception("Sync failed"))
                }
                
                val response = remoteResult.getOrNull() ?: break
                allRemoteItems.addAll(response.items)
                
                // Check if there are more pages
                if (currentPage >= response.totalPages || response.items.isEmpty()) {
                    break
                }
                currentPage++
            }
            
            Log.d("SyncHistory", "Fetched ${allRemoteItems.size} items from server")
            
            // 4. Convert remote items to local entities and save
            val localEntities = allRemoteItems.map { dto ->
                SummaryHistoryEntity(
                    id = dto.id,
                    userId = currentUserId,
                    originalText = dto.inputText,
                    summary = dto.summary,
                    modelUsed = dto.modelUsed,
                    inferenceTimeMs = dto.metrics.colabInferenceMs,
                    createdAt = parseIsoDate(dto.createdAt),
                    rouge1 = null,
                    rouge2 = null,
                    rougeL = null,
                    bleu = null,
                    bertScore = null,
                    synced = true,
                    modifiedAt = System.currentTimeMillis()
                )
            }
            
            summaryHistoryDao.insertAll(localEntities)
            
            // 5. CLEANUP: Delete local synced items that no longer exist on server
            //    (e.g., deleted via web or other clients)
            val remoteIds = allRemoteItems.map { it.id }
            if (remoteIds.isNotEmpty()) {
                summaryHistoryDao.deleteSyncedNotIn(currentUserId, remoteIds)
            } else {
                // Server has 0 records → delete all synced local items
                summaryHistoryDao.deleteAllSyncedByUser(currentUserId)
            }
            
            Log.d("SyncHistory", "Sync complete. Remote: ${allRemoteItems.size} items")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SyncHistory", "Sync failed", e)
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
    
    // ==================== Analytics & Admin ====================
    
    override suspend fun getAnalytics(): Result<AnalyticsResponseDto> {
        return try {
            val response = apiService.getAnalytics()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể tải analytics: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAdminUsers(): Result<List<UserPublicDto>> {
        return try {
            val response = apiService.getAdminUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể tải danh sách users: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== AI Reference ====================

    override suspend fun generateReference(text: String): Result<GenerateReferenceResponse> {
        return try {
            val request = GenerateReferenceRequest(text = text)
            val response = apiService.generateReference(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Không thể tạo tóm tắt tham chiếu"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Settings ====================

    override suspend fun updateSettings(consentShareData: Boolean?, fullName: String?): Result<UserPublicDto> {
        return try {
            val request = UpdateSettingsRequest(consentShareData = consentShareData, fullName = fullName)
            val response = apiService.updateSettings(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Không thể cập nhật cài đặt: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Helper Functions ====================
    
    /**
     * Parse ISO 8601 date string from backend to epoch milliseconds.
     * Backend sends dates like: "2026-05-19T10:30:00+07:00" or "2026-05-19T03:30:00"
     */
    private fun parseIsoDate(isoDate: String): Long {
        // Try multiple ISO 8601 formats the backend might send
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",   // 2026-05-19T10:30:00.123456+07:00
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",      // 2026-05-19T10:30:00.123+07:00
            "yyyy-MM-dd'T'HH:mm:ssXXX",          // 2026-05-19T10:30:00+07:00
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",      // 2026-05-19T10:30:00.123456 (no TZ)
            "yyyy-MM-dd'T'HH:mm:ss.SSS",         // 2026-05-19T10:30:00.123 (no TZ)
            "yyyy-MM-dd'T'HH:mm:ss"              // 2026-05-19T10:30:00 (no TZ)
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                // If the format doesn't include timezone info, assume UTC+7 (Vietnam)
                if (!format.contains("X")) {
                    sdf.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
                }
                val date = sdf.parse(isoDate)
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {
                // Try next format
            }
        }
        
        Log.w("SyncHistory", "Failed to parse date: $isoDate, using current time")
        return System.currentTimeMillis()
    }
}
