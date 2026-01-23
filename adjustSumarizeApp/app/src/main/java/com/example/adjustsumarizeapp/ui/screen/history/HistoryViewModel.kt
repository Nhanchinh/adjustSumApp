package com.example.adjustsumarizeapp.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.domain.usecase.GetHistoryUseCase
import com.example.adjustsumarizeapp.domain.usecase.SyncHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val syncHistoryUseCase: SyncHistoryUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()
    
    init {
        loadHistory()
        checkUnsyncedCount()
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val userId = tokenManager.getUserId()
            
            if (userId == null) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Không tìm thấy thông tin user. Vui lòng đăng nhập lại."
                    )
                }
                return@launch
            }
            
            getHistoryUseCase(userId).collect { historyItems ->
                _state.update {
                    it.copy(
                        historyItems = historyItems,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun syncHistory() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null) }
            
            val result = syncHistoryUseCase()
            
            result.onSuccess {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        successMessage = "Đồng bộ thành công!",
                        unsyncedCount = 0
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSyncing = false,
                        error = error.message ?: "Lỗi đồng bộ"
                    )
                }
            }
        }
    }
    
    fun deleteHistory(id: String) {
        viewModelScope.launch {
            val result = getHistoryUseCase.delete(id)
            
            result.onSuccess {
                _state.update {
                    it.copy(successMessage = "Đã xóa lịch sử")
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(error = error.message ?: "Lỗi xóa lịch sử")
                }
            }
        }
    }
    
    fun searchHistory(query: String) {
        _state.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            loadHistory()
        } else {
            val userId = tokenManager.getUserId() ?: return
            
            viewModelScope.launch {
                getHistoryUseCase.search(query).collect { historyItems ->
                    _state.update { it.copy(historyItems = historyItems) }
                }
            }
        }
    }
    
    fun refreshHistory() {
        loadHistory()
        checkUnsyncedCount()
    }
    
    private fun checkUnsyncedCount() {
        viewModelScope.launch {
            val count = syncHistoryUseCase.getUnsyncedCount()
            _state.update { it.copy(unsyncedCount = count) }
        }
    }
    
    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
