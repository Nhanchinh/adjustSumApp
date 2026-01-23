package com.example.adjustsumarizeapp.domain.usecase

import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting summary history
 * Uses offline-first strategy
 */
class GetHistoryUseCase @Inject constructor(
    private val repository: SummaryRepository
) {
    /**
     * Get local history as Flow for real-time updates
     */
    operator fun invoke(userId: String): Flow<List<SummaryHistoryEntity>> {
        return repository.getLocalHistory(userId)
    }
    
    /**
     * Get single history item
     */
    suspend fun getById(id: String): SummaryHistoryEntity? {
        return repository.getLocalHistoryById(id)
    }
    
    /**
     * Delete history item (local and remote)
     */
    suspend fun delete(id: String): Result<Unit> {
        return try {
            // Delete locally first
            repository.deleteLocalHistory(id)
            
            // Try to delete from remote (don't fail if network error)
            try {
                repository.deleteRemoteHistory(id)
            } catch (e: Exception) {
                // Ignore network errors
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search history
     */
    fun search(query: String): Flow<List<SummaryHistoryEntity>> {
        return repository.searchLocalHistory(query)
    }
}
