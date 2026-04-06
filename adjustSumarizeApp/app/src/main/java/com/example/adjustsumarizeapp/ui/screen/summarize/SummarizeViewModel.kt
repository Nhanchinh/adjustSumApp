package com.example.adjustsumarizeapp.ui.screen.summarize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.domain.repository.SummaryRepository
import com.example.adjustsumarizeapp.domain.usecase.EvaluateSummaryUseCase
import com.example.adjustsumarizeapp.domain.usecase.GetModelsUseCase
import com.example.adjustsumarizeapp.domain.usecase.SummarizeTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val summaryRepository: SummaryRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(SummarizeState())
    val state: StateFlow<SummarizeState> = _state.asStateFlow()

    private var summarizeJob: Job? = null
    private var evaluateJob: Job? = null
    
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
    
    fun onCandidateTextChange(text: String) {
        _state.update { it.copy(candidateText = text, error = null) }
    }
    
    fun toggleEvaluation(show: Boolean) {
        _state.update { it.copy(showEvaluation = show) }
    }
    
    fun toggleBertScore(enabled: Boolean) {
        _state.update { it.copy(calculateBertScore = enabled) }
    }
    
    fun setMode(mode: EvaluationMode) {
        _state.update { 
            it.copy(
                mode = mode,
                error = null,
                // Clear results when switching modes
                summary = "",
                rouge1 = 0.0,
                rouge2 = 0.0,
                rougeL = 0.0,
                bleu = 0.0,
                bertScore = null
            )
        }
    }
    
    fun summarize() {
        val currentState = _state.value
        
        if (currentState.inputText.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập văn bản cần tóm tắt") }
            return
        }

        summarizeJob?.cancel()
        summarizeJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, summary = "") }
            
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

        evaluateJob?.cancel()
        evaluateJob = viewModelScope.launch {
            _state.update { it.copy(isEvaluating = true, error = null) }
            
            val result = evaluateSummaryUseCase(
                prediction = currentState.summary,
                reference = currentState.referenceText,
                calculateBert = currentState.calculateBertScore
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
    
    /**
     * Evaluate-only mode: Compare candidateText with referenceText
     */
    fun evaluateOnly() {
        val currentState = _state.value
        
        if (currentState.candidateText.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập văn bản đã tóm tắt") }
            return
        }
        
        if (currentState.referenceText.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập văn bản tham chiếu") }
            return
        }

        evaluateJob?.cancel()
        evaluateJob = viewModelScope.launch {
            _state.update { it.copy(isEvaluating = true, error = null) }
            
            val result = evaluateSummaryUseCase(
                prediction = currentState.candidateText,
                reference = currentState.referenceText,
                calculateBert = currentState.calculateBertScore
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
    
    fun generateReference() {
        val text = _state.value.inputText
        if (text.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập văn bản gốc trước") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isGeneratingReference = true, error = null) }

            summaryRepository.generateReference(text).onSuccess { response ->
                _state.update {
                    it.copy(
                        referenceText = response.referenceSummary,
                        isGeneratingReference = false,
                        successMessage = "Đã tạo tóm tắt tham chiếu (${response.processingTimeMs}ms)"
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isGeneratingReference = false,
                        error = "Lỗi tạo tham chiếu: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelSummarize() {
        summarizeJob?.cancel()
        summarizeJob = null
        _state.update { it.copy(isLoading = false, error = "Đã hủy tóm tắt") }
    }

    fun cancelEvaluate() {
        evaluateJob?.cancel()
        evaluateJob = null
        _state.update { it.copy(isEvaluating = false, error = "Đã hủy đánh giá") }
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
