package com.example.adjustsumarizeapp.domain.usecase

import com.example.adjustsumarizeapp.data.model.ModelInfo
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import javax.inject.Inject

/**
 * Use case for getting available summarization models
 */
class GetModelsUseCase @Inject constructor(
    private val repository: SummaryRepository
) {
    suspend operator fun invoke(): Result<List<ModelInfo>> {
        return repository.getAvailableModels()
    }
}
