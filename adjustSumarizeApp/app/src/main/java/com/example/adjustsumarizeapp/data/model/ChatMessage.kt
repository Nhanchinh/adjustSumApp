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
    val metadata: MessageMetadata? = null
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
