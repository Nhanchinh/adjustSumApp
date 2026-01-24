package com.example.adjustsumarizeapp.data.model

/**
 * Chat message model for conversational UI
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val metadata: MessageMetadata? = null,
    val userFeedback: UserFeedback? = null,  // Track user ratings
    val detailedEvaluation: DetailedEvaluation? = null  // Human evaluation scores
)

enum class MessageType {
    TEXT,
    SUMMARY_RESULT,
    EVALUATION_RESULT,
    LOADING,
    ERROR
}

data class MessageMetadata(
    val summary: String? = null,
    val model: String? = null,
    val inferenceTimeMs: Double? = null,  // Changed from Int to Double
    val rouge1: Double? = null,
    val rouge2: Double? = null,
    val rougeL: Double? = null,
    val bleu: Double? = null
)

enum class FeedbackType {
    LIKE,
    DISLIKE,
    NONE
}

data class UserFeedback(
    val type: FeedbackType = FeedbackType.NONE,
    val timestamp: Long = System.currentTimeMillis()
)

data class DetailedEvaluation(
    val fluency: Int? = null,      // 1-5
    val coherence: Int? = null,    // 1-5
    val relevance: Int? = null,    // 1-5
    val consistency: Int? = null,  // 1-5
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
