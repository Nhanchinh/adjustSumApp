package com.example.adjustsumarizeapp.ui.screen.chat

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.adjustsumarizeapp.data.model.ChatMessage
import com.example.adjustsumarizeapp.data.model.MessageType
import com.example.adjustsumarizeapp.utils.clearFocusOnTap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    var showEvaluationDialog by remember { mutableStateOf(false) }
    var selectedMessageForEval by remember { mutableStateOf<ChatMessage?>(null) }
    
    // Auto-scroll to bottom when new message
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clearFocusOnTap()
    ) {
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onCopy = { text ->
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                        android.widget.Toast.makeText(context, "Đã sao chép", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onLike = { viewModel.onMessageFeedback(message.id, com.example.adjustsumarizeapp.data.model.FeedbackType.LIKE) },
                    onDislike = { viewModel.onMessageFeedback(message.id, com.example.adjustsumarizeapp.data.model.FeedbackType.DISLIKE) },
                    onDetailedEval = {
                        selectedMessageForEval = message
                        showEvaluationDialog = true
                    }
                )
            }
            
            // Empty state
            if (state.messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Bắt đầu cuộc trò chuyện",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nhập văn bản để tôi tóm tắt cho bạn",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Input Area
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Compact Model Picker (above input)
                AnimatedVisibility(
                    visible = state.showModelPicker,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Chọn Model",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.availableModels) { modelInfo ->
                                    FilterChip(
                                        selected = state.selectedModel == modelInfo.id,
                                        onClick = { 
                                            viewModel.onModelSelect(modelInfo.id)
                                            viewModel.toggleModelPicker()
                                        },
                                        label = { 
                                            Text(
                                                modelInfo.name,
                                                style = MaterialTheme.typography.labelMedium
                                            ) 
                                        },
                                        leadingIcon = {
                                            if (state.selectedModel == modelInfo.id) {
                                                Icon(
                                                    Icons.Default.CheckCircle, 
                                                    "Selected", 
                                                    Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Model button
                    IconButton(
                        onClick = { viewModel.toggleModelPicker() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            "Model",
                            tint = if (state.showModelPicker)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Text input
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = { viewModel.onInputChange(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { 
                            Text(
                                "Nhập văn bản cần tóm tắt...",
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        enabled = !state.isLoading,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    
                    // Send button
                    FloatingActionButton(
                        onClick = { viewModel.sendMessage() },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (state.inputText.isNotBlank() && !state.isLoading)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            "Send",
                            modifier = Modifier.size(22.dp),
                            tint = if (state.inputText.isNotBlank() && !state.isLoading)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Detailed Evaluation Dialog
    if (showEvaluationDialog && selectedMessageForEval != null) {
        DetailedEvaluationDialog(
            message = selectedMessageForEval!!,
            onDismiss = { showEvaluationDialog = false },
            onSubmit = { evaluation ->
                viewModel.onDetailedEvaluation(selectedMessageForEval!!.id, evaluation)
                showEvaluationDialog = false
                android.widget.Toast.makeText(context, "Đã lưu đánh giá", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onCopy: (String) -> Unit = {},
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onDetailedEval: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) 
            Arrangement.End 
        else 
            Arrangement.Start
    ) {
        if (!message.isFromUser) {
            // Bot avatar
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = "Bot",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (message.isFromUser) 20.dp else 4.dp,
                    bottomEnd = if (message.isFromUser) 4.dp else 20.dp
                ),
                color = when {
                    message.isFromUser -> MaterialTheme.colorScheme.primary
                    message.type == MessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
                    message.type == MessageType.LOADING -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                tonalElevation = if (message.isFromUser) 2.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Loading indicator
                    if (message.type == MessageType.LOADING) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Message text
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                message.isFromUser -> MaterialTheme.colorScheme.onPrimary
                                message.type == MessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                        
                        // Metadata (for summary results)
                        if (message.type == MessageType.SUMMARY_RESULT && message.metadata != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { 
                                        Text(
                                            message.metadata.model?.uppercase() ?: "MODEL",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.height(24.dp)
                                )
                                
                                message.metadata.inferenceTimeMs?.let { time ->
                                    AssistChip(
                                        onClick = {},
                                        label = { 
                                            Text(
                                                "${time.toInt()}ms",  // Convert Double to Int for display
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Speed,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Action buttons
            // User messages: Only Copy
            // Bot summary messages: Copy, Like, Dislike
            if (message.type != MessageType.LOADING && message.type != MessageType.ERROR) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy button (for all messages)
                    IconButton(
                        onClick = { onCopy(message.text) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = if (message.isFromUser)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Like & Dislike buttons (only for bot summary messages)
                    if (!message.isFromUser && message.type == MessageType.SUMMARY_RESULT) {
                        // Like button
                        IconButton(
                            onClick = onLike,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (message.userFeedback?.type == com.example.adjustsumarizeapp.data.model.FeedbackType.LIKE)
                                    Icons.Default.ThumbUp
                                else
                                    Icons.Default.ThumbUpOffAlt,
                                contentDescription = "Like",
                                modifier = Modifier.size(16.dp),
                                tint = if (message.userFeedback?.type == com.example.adjustsumarizeapp.data.model.FeedbackType.LIKE)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Dislike button
                        IconButton(
                            onClick = onDislike,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (message.userFeedback?.type == com.example.adjustsumarizeapp.data.model.FeedbackType.DISLIKE)
                                    Icons.Default.ThumbDown
                                else
                                    Icons.Default.ThumbDownOffAlt,
                                contentDescription = "Dislike",
                                modifier = Modifier.size(16.dp),
                                tint = if (message.userFeedback?.type == com.example.adjustsumarizeapp.data.model.FeedbackType.DISLIKE)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        
                        // More options button (3 dots)
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (message.detailedEvaluation != null)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Đánh giá chi tiết")
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onDetailedEval()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Timestamp
            if (message.type != MessageType.LOADING) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
        
        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Bottom)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Vừa xong"
        diff < 3600000 -> "${diff / 60000} phút trước"
        diff < 86400000 -> "${diff / 3600000} giờ trước"
        else -> java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailedEvaluationDialog(
    message: ChatMessage,
    onDismiss: () -> Unit,
    onSubmit: (com.example.adjustsumarizeapp.data.model.DetailedEvaluation) -> Unit
) {
    var fluency by remember { mutableStateOf(message.detailedEvaluation?.fluency) }
    var coherence by remember { mutableStateOf(message.detailedEvaluation?.coherence) }
    var relevance by remember { mutableStateOf(message.detailedEvaluation?.relevance) }
    var consistency by remember { mutableStateOf(message.detailedEvaluation?.consistency) }
    var comment by remember { mutableStateOf(message.detailedEvaluation?.comment ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Simple Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Đánh giá chi tiết",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Đánh giá chất lượng tóm tắt (1-5 điểm)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close, 
                            "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary Preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Summary đang đánh giá",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                message.text,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Criteria
                    EvaluationCriterion(
                        label = "Fluency",
                        description = "Văn phong tự nhiên, không lỗi ngữ pháp",
                        score = fluency,
                        onScoreChange = { fluency = it }
                    )
                    
                    EvaluationCriterion(
                        label = "Coherence",
                        description = "Các ý kết nối chặt chẽ, logic rõ ràng",
                        score = coherence,
                        onScoreChange = { coherence = it }
                    )
                    
                    EvaluationCriterion(
                        label = "Relevance",
                        description = "Nội dung đúng trọng tâm, không thừa",
                        score = relevance,
                        onScoreChange = { relevance = it }
                    )
                    
                    EvaluationCriterion(
                        label = "Consistency",
                        description = "Không mâu thuẫn, thông tin chính xác",
                        score = consistency,
                        onScoreChange = { consistency = it }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Comment
                    Text(
                        "Nhận xét (tuỳ chọn)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 500) comment = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                "Ghi chú về bản tóm tắt...",
                                style = MaterialTheme.typography.bodySmall
                            ) 
                        },
                        minLines = 3,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Text(
                        "${comment.length}/500",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                // Bottom Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Hủy", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = {
                            onSubmit(
                                com.example.adjustsumarizeapp.data.model.DetailedEvaluation(
                                    fluency = fluency,
                                    coherence = coherence,
                                    relevance = relevance,
                                    consistency = consistency,
                                    comment = comment
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp),
                        enabled = fluency != null || coherence != null || relevance != null || consistency != null,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(
                            "Lưu đánh giá", 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvaluationCriterion(
    label: String,
    description: String,
    score: Int?,
    onScoreChange: (Int?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            if (score != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        "$score/5",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Score Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { value ->
                val isSelected = score != null && score >= value
                Button(
                    onClick = { onScoreChange(value) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "$value",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        
        if (score != null) {
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                onClick = { onScoreChange(null) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.Clear,
                    "Clear",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Xóa điểm",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}
