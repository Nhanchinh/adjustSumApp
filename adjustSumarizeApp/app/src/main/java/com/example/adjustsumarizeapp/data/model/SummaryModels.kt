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
 * Summarize Response - khớp với backend SummarizeResponse
 */
data class SummarizeResponse(
    @SerializedName("original_text")
    val originalText: String = "",
    
    @SerializedName("preprocessed_text")
    val preprocessedText: String = "",
    
    @SerializedName("summary")
    val summary: String,
    
    @SerializedName("model_used")
    val modelUsed: String,
    
    @SerializedName("colab_inference_ms")
    val colabInferenceMs: Double,
    
    @SerializedName("colab_inference_s")
    val colabInferenceS: Double = 0.0,
    
    @SerializedName("total_processing_ms")
    val totalProcessingMs: Double,
    
    @SerializedName("total_processing_s")
    val totalProcessingS: Double = 0.0,
    
    @SerializedName("metadata")
    val metadata: Map<String, Any>? = null
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
 * Feedback response from backend
 */
data class FeedbackResponse(
    @SerializedName("rating")
    val rating: String,  // "good", "bad", "neutral"
    
    @SerializedName("comment")
    val comment: String?,
    
    @SerializedName("corrected_summary")
    val correctedSummary: String?,
    
    @SerializedName("feedback_at")
    val feedbackAt: String?,
    
    @SerializedName("human_eval")
    val humanEval: HumanEvalScores?
)

/**
 * Human evaluation scores (1-5)
 */
data class HumanEvalScores(
    @SerializedName("fluency")
    val fluency: Int?,
    
    @SerializedName("coherence")
    val coherence: Int?,
    
    @SerializedName("relevance")
    val relevance: Int?,
    
    @SerializedName("consistency")
    val consistency: Int?
)

/**
 * History Item from API
 */
data class HistoryItemDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("input_text")
    val inputText: String,
    
    @SerializedName("summary")
    val summary: String,
    
    @SerializedName("model_used")
    val modelUsed: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("metrics")
    val metrics: HistoryMetrics,
    
    @SerializedName("feedback")
    val feedback: FeedbackResponse? = null
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
    val pageSize: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int = 1
)

/**
 * Save History Request
 */
data class SaveHistoryRequest(
    @SerializedName("input_text")
    val inputText: String,
    
    @SerializedName("summary")
    val summary: String,
    
    @SerializedName("model_used")
    val modelUsed: String,
    
    @SerializedName("input_words")
    val inputWords: Int = 0,
    
    @SerializedName("output_words")
    val outputWords: Int = 0,
    
    @SerializedName("compression_ratio")
    val compressionRatio: Float = 0f,
    
    @SerializedName("processing_time_ms")
    val processingTimeMs: Int = 0,
    
    @SerializedName("colab_inference_ms")
    val colabInferenceMs: Float? = null
)

/**
 * Colab Health Check Response
 */
data class ColabHealthResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("colab_url")
    val colabUrl: String?,
    
    @SerializedName("gpu_available")
    val gpuAvailable: Boolean?,
    
    @SerializedName("error")
    val error: String?
)

/**
 * Analytics - Model Stats
 */
data class ModelStatsDto(
    @SerializedName("model")
    val model: String,
    
    @SerializedName("count")
    val count: Int,
    
    @SerializedName("avg_compression_ratio")
    val avgCompressionRatio: Double,
    
    @SerializedName("avg_processing_time_ms")
    val avgProcessingTimeMs: Double,
    
    @SerializedName("good_count")
    val goodCount: Int,
    
    @SerializedName("bad_count")
    val badCount: Int,
    
    @SerializedName("neutral_count")
    val neutralCount: Int
)

/**
 * Analytics - Daily Count
 */
data class DailyCountDto(
    @SerializedName("date")
    val date: String,
    
    @SerializedName("count")
    val count: Int
)

/**
 * Analytics Response from GET /history/analytics
 */
data class AnalyticsResponseDto(
    @SerializedName("total_summaries")
    val totalSummaries: Int,
    
    @SerializedName("total_with_feedback")
    val totalWithFeedback: Int,
    
    @SerializedName("feedback_rate")
    val feedbackRate: Double,
    
    @SerializedName("rating_distribution")
    val ratingDistribution: Map<String, Int>,
    
    @SerializedName("model_distribution")
    val modelDistribution: Map<String, Int>,
    
    @SerializedName("model_stats")
    val modelStats: List<ModelStatsDto>,
    
    @SerializedName("daily_counts")
    val dailyCounts: List<DailyCountDto>,
    
    @SerializedName("avg_compression_ratio")
    val avgCompressionRatio: Double,
    
    @SerializedName("avg_processing_time_ms")
    val avgProcessingTimeMs: Double
)

/**
 * Admin - User Public info
 */
data class UserPublicDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("full_name")
    val fullName: String?,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("consent_share_data")
    val consentShareData: Boolean = true
)

/**
 * Request to update user settings (privacy, profile)
 */
/**
 * Request to generate a gold reference summary using Gemini AI
 */
data class GenerateReferenceRequest(
    @SerializedName("text")
    val text: String
)

data class GenerateReferenceResponse(
    @SerializedName("reference_summary")
    val referenceSummary: String,

    @SerializedName("processing_time_ms")
    val processingTimeMs: Int
)

/**
 * Request to update user settings (privacy, profile)
 */
data class UpdateSettingsRequest(
    @SerializedName("consent_share_data")
    val consentShareData: Boolean? = null,

    @SerializedName("full_name")
    val fullName: String? = null
)
