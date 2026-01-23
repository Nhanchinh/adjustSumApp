package com.example.adjustsumarizeapp.data.local.dao

import androidx.room.*
import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Summary History operations
 */
@Dao
interface SummaryHistoryDao {
    
    /**
     * Get all history items, ordered by creation date (newest first)
     */
    @Query("SELECT * FROM summary_history ORDER BY created_at DESC")
    fun getAllHistory(): Flow<List<SummaryHistoryEntity>>
    
    /**
     * Get history items for a specific user
     */
    @Query("SELECT * FROM summary_history WHERE user_id = :userId ORDER BY created_at DESC")
    fun getHistoryByUser(userId: String): Flow<List<SummaryHistoryEntity>>
    
    /**
     * Get a single history item by ID
     */
    @Query("SELECT * FROM summary_history WHERE id = :id")
    suspend fun getHistoryById(id: String): SummaryHistoryEntity?
    
    /**
     * Get unsynced history items
     */
    @Query("SELECT * FROM summary_history WHERE synced = 0 ORDER BY created_at DESC")
    suspend fun getUnsyncedHistory(): List<SummaryHistoryEntity>
    
    /**
     * Insert a new history item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SummaryHistoryEntity)
    
    /**
     * Insert multiple history items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<SummaryHistoryEntity>)
    
    /**
     * Update a history item
     */
    @Update
    suspend fun update(history: SummaryHistoryEntity)
    
    /**
     * Delete a history item
     */
    @Delete
    suspend fun delete(history: SummaryHistoryEntity)
    
    /**
     * Delete a history item by ID
     */
    @Query("DELETE FROM summary_history WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Delete all history
     */
    @Query("DELETE FROM summary_history")
    suspend fun deleteAll()
    
    /**
     * Mark history item as synced
     */
    @Query("UPDATE summary_history SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    /**
     * Get history count
     */
    @Query("SELECT COUNT(*) FROM summary_history")
    suspend fun getCount(): Int
    
    /**
     * Get history count by model
     */
    @Query("SELECT COUNT(*) FROM summary_history WHERE model_used = :model")
    suspend fun getCountByModel(model: String): Int
    
    /**
     * Search history by text content
     */
    @Query("SELECT * FROM summary_history WHERE original_text LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchHistory(query: String): Flow<List<SummaryHistoryEntity>>
}
