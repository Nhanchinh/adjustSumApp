package com.example.adjustsumarizeapp.domain.usecase

import android.util.Log
import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity
import com.example.adjustsumarizeapp.data.model.SummarizeResponse
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for summarizing text
 * Handles business logic: API call + save to remote + local caching
 */
class SummarizeTextUseCase @Inject constructor(
    private val repository: SummaryRepository
) {
    suspend operator fun invoke(
        text: String,
        model: String,
        maxLength: Int,
        userId: String,
        saveToHistory: Boolean = true
    ): Result<SummarizeResponse> {
        // Validate input
        if (text.isBlank()) {
            return Result.failure(Exception("Văn bản không được để trống"))
        }
        
        if (text.length < 10) {
            return Result.failure(Exception("Văn bản quá ngắn (tối thiểu 10 ký tự)"))
        }
        
        // Call repository
        val result = repository.summarizeText(text, model, maxLength)
        
        // If successful and saveToHistory, save to server FIRST then cache locally
        if (result.isSuccess && saveToHistory) {
            result.getOrNull()?.let { response ->
                try {
                    // 1. Try saving to remote server first → get real MongoDB ID
                    val remoteResult = repository.saveRemoteHistory(
                        originalText = text,
                        summary = response.summary,
                        modelUsed = response.modelUsed,
                        inferenceTimeMs = response.colabInferenceMs,
                        metrics = null
                    )
                    
                    if (remoteResult.isSuccess) {
                        // 2a. Remote save succeeded → save locally with server's real ID
                        val serverItem = remoteResult.getOrNull()!!
                        val historyEntity = SummaryHistoryEntity(
                            id = serverItem.id,  // Use real MongoDB ObjectId
                            userId = userId,
                            originalText = text,
                            summary = response.summary,
                            modelUsed = response.modelUsed,
                            inferenceTimeMs = response.colabInferenceMs,
                            createdAt = System.currentTimeMillis(),
                            rouge1 = null,
                            rouge2 = null,
                            rougeL = null,
                            bleu = null,
                            bertScore = null,
                            synced = true,  // Already on server
                            modifiedAt = System.currentTimeMillis()
                        )
                        repository.saveLocalHistory(historyEntity)
                        Log.d("SummarizeUseCase", "Saved to server and local with ID: ${serverItem.id}")
                    } else {
                        // 2b. Remote save failed → save locally only (will sync later)
                        Log.w("SummarizeUseCase", "Remote save failed, saving locally only: ${remoteResult.exceptionOrNull()?.message}")
                        val historyEntity = SummaryHistoryEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            originalText = text,
                            summary = response.summary,
                            modelUsed = response.modelUsed,
                            inferenceTimeMs = response.colabInferenceMs,
                            createdAt = System.currentTimeMillis(),
                            rouge1 = null,
                            rouge2 = null,
                            rougeL = null,
                            bleu = null,
                            bertScore = null,
                            synced = false,  // Not on server yet
                            modifiedAt = System.currentTimeMillis()
                        )
                        repository.saveLocalHistory(historyEntity)
                    }
                } catch (e: Exception) {
                    // Don't fail the whole operation if history saving fails
                    Log.e("SummarizeUseCase", "Error saving history", e)
                }
            }
        }
        
        return result
    }
}

