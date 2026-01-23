package com.example.adjustsumarizeapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model Information
 */
data class ModelInfo(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?
)

/**
 * Summarize Request
 */
data class SummarizeRequest(
    @SerializedName("text")
    val text: String,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("max_length")
    val maxLength: Int = 256
)

/**
 * Summarize Response
 */
data class SummarizeResponse(
    @SerializedName("summary")
    val summary: String,
    
    @SerializedName("model_used")
    val modelUsed: String,
    
    @SerializedName("colab_inference_ms")
    val colabInferenceMs: Double,  // Changed from Int to Double
    
    @SerializedName("total_processing_ms")
    val totalProcessingMs: Double,  // Changed from Int to Double
    
    @SerializedName("input_length")
    val inputLength: Int,
    
    @SerializedName("output_length")
    val outputLength: Int
)

/**
 * Evaluation Metrics
 */
data class EvaluationMetrics(
    @SerializedName("rouge1")
    val rouge1: Double,
    
    @SerializedName("rouge2")
    val rouge2: Double,
    
    @SerializedName("rougeL")
    val rougeL: Double,
    
    @SerializedName("bleu")
    val bleu: Double,
    
    @SerializedName("bert_score")
    val bertScore: Double?,
    
    @SerializedName("processing_time_ms")
    val processingTimeMs: Int
)

/**
 * Evaluate Request
 */
data class EvaluateRequest(
    @SerializedName("prediction")
    val prediction: String,
    
    @SerializedName("reference")
    val reference: String,
    
    @SerializedName("calculate_bert")
    val calculateBert: Boolean = false
)

/**
 * Evaluate Response
 */
data class EvaluateResponse(
    @SerializedName("rouge1")
    val rouge1: Double,
    
    @SerializedName("rouge2")
    val rouge2: Double,
    
    @SerializedName("rougeL")
    val rougeL: Double,
    
    @SerializedName("bleu")
    val bleu: Double,
    
    @SerializedName("bert_score")
    val bertScore: Double?,
    
    @SerializedName("processing_time_ms")
    val processingTimeMs: Int
)

/**
 * History Metrics Response (from backend /history endpoint)
 * Different from EvaluationMetrics!
 */
data class HistoryMetrics(
    @SerializedName("input_words")
    val inputWords: Int,
    
    @SerializedName("output_words")
    val outputWords: Int,
    
    @SerializedName("compression_ratio")
    val compressionRatio: Float,
    
    @SerializedName("processing_time_ms")
    val processingTimeMs: Int,
    
    @SerializedName("colab_inference_ms")
    val colabInferenceMs: Double?
)

/**
 * History Item from API
 */
data class HistoryItemDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("user_id")
    val userId: String?,  // Nullable - backend không trả về trong list
    
    @SerializedName("input_text")  // FIXED: Backend dùng "input_text" không phải "original_text"
    val inputText: String,
    
    @SerializedName("summary")
    val summary: String,
    
    @SerializedName("model_used")
    val modelUsed: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("metrics")
    val metrics: HistoryMetrics  // FIXED: Dùng HistoryMetrics thay vì EvaluationMetrics
)

/**
 * History Response (paginated)
 */
data class HistoryResponse(
    @SerializedName("items")
    val items: List<HistoryItemDto>,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("page_size")
    val pageSize: Int
)

/**
 * Save History Request
 */
data class SaveHistoryRequest(
    @SerializedName("original_text")
    val originalText: String,
    
    @SerializedName("summary")
    val summary: String,
    
    @SerializedName("model_used")
    val modelUsed: String,
    
    @SerializedName("inference_time_ms")
    val inferenceTimeMs: Double?,  // Changed from Int to Double
    
    @SerializedName("metrics")
    val metrics: EvaluationMetrics?
)
