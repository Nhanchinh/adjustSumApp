package com.example.adjustsumarizeapp.ui.screen.chat

import com.example.adjustsumarizeapp.data.model.ChatMessage
import com.example.adjustsumarizeapp.data.model.ModelInfo

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val selectedModel: String = "phobert_vit5",
    val availableModels: List<ModelInfo> = emptyList(),
    val showModelPicker: Boolean = false,
    val maxLength: Int = 256
)
