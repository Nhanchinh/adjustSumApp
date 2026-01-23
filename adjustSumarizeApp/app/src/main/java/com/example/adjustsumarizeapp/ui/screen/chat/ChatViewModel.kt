package com.example.adjustsumarizeapp.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adjustsumarizeapp.data.local.TokenManager
import com.example.adjustsumarizeapp.data.model.ChatMessage
import com.example.adjustsumarizeapp.data.model.MessageMetadata
import com.example.adjustsumarizeapp.data.model.MessageType
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
class ChatViewModel @Inject constructor(
    private val summarizeTextUseCase: SummarizeTextUseCase,
    private val getModelsUseCase: GetModelsUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    
    init {
        loadModels()
        addWelcomeMessage()
    }
    
    private fun loadModels() {
        viewModelScope.launch {
            val result = getModelsUseCase()
            result.onSuccess { models ->
                _state.update { it.copy(availableModels = models) }
            }
        }
    }
    
    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            text = "👋 Xin chào! Tôi là trợ lý AI tóm tắt văn bản.\n\n" +
                    "Bạn có thể:\n" +
                    "• Gửi văn bản cần tóm tắt\n" +
                    "• Chọn model bằng icon ⚙️\n" +
                    "• Xem lịch sử trong tab History\n\n" +
                    "Hãy thử gửi một đoạn văn dài để bắt đầu! 📝",
            isFromUser = false,
            type = MessageType.TEXT
        )
        _state.update { it.copy(messages = listOf(welcomeMessage)) }
    }
    
    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }
    
    fun onModelSelect(model: String) {
        _state.update { 
            it.copy(
                selectedModel = model,
                showModelPicker = false
            )
        }
        
        // Add confirmation message
        val confirmMessage = ChatMessage(
            text = "✓ Đã chọn model: ${model.uppercase()}",
            isFromUser = false,
            type = MessageType.TEXT
        )
        _state.update { 
            it.copy(messages = it.messages + confirmMessage)
        }
    }
    
    fun toggleModelPicker() {
        _state.update { it.copy(showModelPicker = !it.showModelPicker) }
    }
    
    fun sendMessage() {
        val currentState = _state.value
        val text = currentState.inputText.trim()
        
        if (text.isBlank()) return
        
        // Add user message
        val userMessage = ChatMessage(
            text = text,
            isFromUser = true,
            type = MessageType.TEXT
        )
        
        _state.update { 
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isLoading = true
            )
        }
        
        // Add loading message
        val loadingMessage = ChatMessage(
            text = "Đang xử lý...",
            isFromUser = false,
            type = MessageType.LOADING
        )
        
        _state.update { 
            it.copy(messages = it.messages + loadingMessage)
        }
        
        // Process summarization
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: "unknown"
            
            val result = summarizeTextUseCase(
                text = text,
                model = currentState.selectedModel,
                maxLength = currentState.maxLength,
                userId = userId,
                saveToHistory = true
            )
            
            // Remove loading message
            _state.update { 
                it.copy(
                    messages = it.messages.filterNot { msg -> msg.type == MessageType.LOADING },
                    isLoading = false
                )
            }
            
            result.onSuccess { response ->
                val summaryMessage = ChatMessage(
                    text = response.summary,
                    isFromUser = false,
                    type = MessageType.SUMMARY_RESULT,
                    metadata = MessageMetadata(
                        summary = response.summary,
                        model = response.modelUsed,
                        inferenceTimeMs = response.colabInferenceMs
                    )
                )
                
                _state.update { 
                    it.copy(messages = it.messages + summaryMessage)
                }
            }.onFailure { error ->
                val errorMessage = ChatMessage(
                    text = "❌ Lỗi: ${error.message ?: "Không thể tóm tắt văn bản"}",
                    isFromUser = false,
                    type = MessageType.ERROR
                )
                
                _state.update { 
                    it.copy(messages = it.messages + errorMessage)
                }
            }
        }
    }
    
    fun clearChat() {
        _state.update { 
            it.copy(messages = emptyList())
        }
        addWelcomeMessage()
    }
}
