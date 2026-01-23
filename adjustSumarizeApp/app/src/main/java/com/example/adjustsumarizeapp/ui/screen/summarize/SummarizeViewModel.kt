package com.example.adjustsumarizeapp.ui.screen.summarize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.domain.usecase.EvaluateSummaryUseCase
import com.example.adjustsumarizeapp.domain.usecase.GetModelsUseCase
import com.example.adjustsumarizeapp.domain.usecase.SummarizeTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummarizeViewModel @Inject constructor(
    private val summarizeTextUseCase: SummarizeTextUseCase,
    private val getModelsUseCase: GetModelsUseCase,
    private val evaluateSummaryUseCase: EvaluateSummaryUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(SummarizeState())
    val state: StateFlow<SummarizeState> = _state.asStateFlow()
    
    init {
        loadModels()
    }
    
    private fun loadModels() {
        viewModelScope.launch {
            val result = getModelsUseCase()
            result.onSuccess { models ->
                _state.update { it.copy(availableModels = models) }
            }
        }
    }
    
    fun onInputTextChange(text: String) {
        _state.update { it.copy(inputText = text, error = null) }
    }
    
    fun onModelSelect(model: String) {
        _state.update { it.copy(selectedModel = model) }
    }
    
    fun onMaxLengthChange(length: Int) {
        _state.update { it.copy(maxLength = length) }
    }
    
    fun onReferenceTextChange(text: String) {
        _state.update { it.copy(referenceText = text) }
    }
    
    fun toggleEvaluation(show: Boolean) {
        _state.update { it.copy(showEvaluation = show) }
    }
    
    fun summarize() {
        val currentState = _state.value
        
        if (currentState.inputText.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập văn bản cần tóm tắt") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, summary = "") }
            
            // Get user ID from token
            val userId = tokenManager.getUserId() ?: "unknown"
            
            val result = summarizeTextUseCase(
                text = currentState.inputText,
                model = currentState.selectedModel,
                maxLength = currentState.maxLength,
                userId = userId,
                saveToHistory = true
            )
            
            result.onSuccess { response ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        summary = response.summary,
                        inferenceTimeMs = response.colabInferenceMs,
                        successMessage = "Tóm tắt thành công!"
                    )
                }
                
                // Auto-evaluate if reference text is provided
                if (currentState.showEvaluation && currentState.referenceText.isNotBlank()) {
                    evaluateSummary()
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Lỗi không xác định"
                    )
                }
            }
        }
    }
    
    fun evaluateSummary() {
        val currentState = _state.value
        
        if (currentState.summary.isBlank()) {
            _state.update { it.copy(error = "Chưa có bản tóm tắt để đánh giá") }
            return
        }
        
        if (currentState.referenceText.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập văn bản tham chiếu") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isEvaluating = true, error = null) }
            
            val result = evaluateSummaryUseCase(
                prediction = currentState.summary,
                reference = currentState.referenceText,
                calculateBert = false // Set to true if needed, but it's slow
            )
            
            result.onSuccess { response ->
                _state.update {
                    it.copy(
                        isEvaluating = false,
                        rouge1 = response.rouge1,
                        rouge2 = response.rouge2,
                        rougeL = response.rougeL,
                        bleu = response.bleu,
                        bertScore = response.bertScore,
                        successMessage = "Đánh giá hoàn tất!"
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isEvaluating = false,
                        error = error.message ?: "Lỗi đánh giá"
                    )
                }
            }
        }
    }
    
    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
    
    fun clearResults() {
        _state.update {
            it.copy(
                summary = "",
                inferenceTimeMs = 0.0,  // Changed from 0 to 0.0 (Double)
                rouge1 = 0.0,
                rouge2 = 0.0,
                rougeL = 0.0,
                bleu = 0.0,
                bertScore = null
            )
        }
    }
}
