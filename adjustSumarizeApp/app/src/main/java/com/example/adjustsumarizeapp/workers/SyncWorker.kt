package com.example.adjustsumarizeapp.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.adjustsumarizeapp.domain.usecase.SyncHistoryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker for background sync of history data
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncHistoryUseCase: SyncHistoryUseCase
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val result = syncHistoryUseCase()
            
            if (result.isSuccess) {
                Result.success()
            } else {
                // Retry on failure
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "SyncHistoryWork"
    }
}
