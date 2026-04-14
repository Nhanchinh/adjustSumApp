package com.example.adjustsumarizeapp.ui.screen.summarize

import com.example.adjustsumarizeapp.data.model.ModelInfo

/**
 * UI State for Summarize Screen
 */
data class SummarizeState(
    // Mode selection
    val mode: EvaluationMode = EvaluationMode.SUMMARIZE_AND_EVALUATE,
    
    // Summarize mode inputs
    val inputText: String = "",
    val selectedModel: String = "vit5_fin",
    val maxLength: Int = 256,
    val availableModels: List<ModelInfo> = emptyList(),
    
    // Result
    val summary: String = "",
    val inferenceTimeMs: Double = 0.0,  // Changed from Int to Double
    
    // Evaluation inputs
    val showEvaluation: Boolean = false,
    val referenceText: String = "",
    val calculateBertScore: Boolean = false,  // Option to calculate BERTScore (slower)
    
    // Evaluate-only mode inputs
    val candidateText: String = "",  // Văn bản đã tóm tắt (prediction)
    
    // Evaluation results
    val rouge1: Double = 0.0,
    val rouge2: Double = 0.0,
    val rougeL: Double = 0.0,
    val bleu: Double = 0.0,
    val bertScore: Double? = null,
    
    // UI States
    val isLoading: Boolean = false,
    val isEvaluating: Boolean = false,
    val isGeneratingReference: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class EvaluationMode {
    SUMMARIZE_AND_EVALUATE,  // Tóm tắt + Đánh giá
    EVALUATE_ONLY            // Chỉ đánh giá
}
