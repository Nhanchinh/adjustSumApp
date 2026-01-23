package com.example.adjustsumarizeapp.domain.usecase

import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity
import com.example.adjustsumarizeapp.data.model.SummarizeResponse
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for summarizing text
 * Handles business logic: API call + local caching
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
        
        // If successful and saveToHistory, cache locally
        if (result.isSuccess && saveToHistory) {
            result.getOrNull()?.let { response ->
                try {
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
                        synced = false,
                        modifiedAt = System.currentTimeMillis()
                    )
                    
                    repository.saveLocalHistory(historyEntity)
                } catch (e: Exception) {
                    // Don't fail the whole operation if caching fails
                    e.printStackTrace()
                }
            }
        }
        
        return result
    }
}
