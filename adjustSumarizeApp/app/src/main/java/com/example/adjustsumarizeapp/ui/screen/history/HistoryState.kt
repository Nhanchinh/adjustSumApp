package com.example.adjustsumarizeapp.ui.screen.history

import com.example.adjustsumarizeapp.data.local.entity.SummaryHistoryEntity

/**
 * UI State for History Screen
 */
data class HistoryState(
    val historyItems: List<SummaryHistoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val unsyncedCount: Int = 0
)
