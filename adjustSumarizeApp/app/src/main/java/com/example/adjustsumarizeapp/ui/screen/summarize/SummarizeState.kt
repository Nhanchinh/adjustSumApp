package com.example.adjustsumarizeapp.ui.screen.summarize

import com.example.adjustsumarizeapp.data.model.ModelInfo

/**
 * UI State for Summarize Screen
 */
data class SummarizeState(
    val inputText: String = "",
    val selectedModel: String = "vit5",
    val maxLength: Int = 256,
    val availableModels: List<ModelInfo> = emptyList(),
    
    // Result
    val summary: String = "",
    val inferenceTimeMs: Double = 0.0,  // Changed from Int to Double
    
    // Evaluation
    val showEvaluation: Boolean = false,
    val referenceText: String = "",
    val rouge1: Double = 0.0,
    val rouge2: Double = 0.0,
    val rougeL: Double = 0.0,
    val bleu: Double = 0.0,
    val bertScore: Double? = null,
    
    // UI States
    val isLoading: Boolean = false,
    val isEvaluating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
