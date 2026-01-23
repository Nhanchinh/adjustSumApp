package com.example.adjustsumarizeapp.domain.usecase

import com.example.adjustsumarizeapp.data.model.EvaluateResponse
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import javax.inject.Inject

/**
 * Use case for evaluating summary quality
 */
class EvaluateSummaryUseCase @Inject constructor(
    private val repository: SummaryRepository
) {
    suspend operator fun invoke(
        prediction: String,
        reference: String,
        calculateBert: Boolean = false
    ): Result<EvaluateResponse> {
        // Validate input
        if (prediction.isBlank() || reference.isBlank()) {
            return Result.failure(Exception("Văn bản tóm tắt và tham chiếu không được để trống"))
        }
        
        return repository.evaluateSummary(prediction, reference, calculateBert)
    }
}
