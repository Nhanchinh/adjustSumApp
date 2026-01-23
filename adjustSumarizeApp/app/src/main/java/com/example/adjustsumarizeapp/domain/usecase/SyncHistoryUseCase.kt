package com.example.adjustsumarizeapp.domain.usecase

import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import javax.inject.Inject

/**
 * Use case for syncing history between local and remote
 */
class SyncHistoryUseCase @Inject constructor(
    private val repository: SummaryRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncHistory()
    }
    
    /**
     * Get unsynced items count
     */
    suspend fun getUnsyncedCount(): Int {
        return repository.getUnsyncedHistory().size
    }
}
