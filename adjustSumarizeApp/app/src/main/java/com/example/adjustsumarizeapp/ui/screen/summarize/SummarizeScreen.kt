package com.example.adjustsumarizeapp.ui.screen.summarize

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.adjustsumarizeapp.utils.clearFocusOnTap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarizeScreen(
    viewModel: SummarizeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    
    // Show Snackbar for messages
    LaunchedEffect(state.error, state.successMessage) {
        if (state.error != null || state.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .clearFocusOnTap()
    ) {
        // Title
        Text(
            text = "Tóm tắt văn bản",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Input Text Field
        OutlinedTextField(
            value = state.inputText,
            onValueChange = { viewModel.onInputTextChange(it) },
            label = { Text("Nhập văn bản cần tóm tắt") },
            placeholder = { Text("Nhập văn bản dài ít nhất 10 ký tự...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 6,
            trailingIcon = {
                if (state.inputText.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onInputTextChange("") }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model Selection
        Text("Chọn model:", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        var expandedModels by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedModels,
            onExpandedChange = { expandedModels = it }
        ) {
            OutlinedTextField(
                value = state.selectedModel.uppercase(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Model") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModels) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expandedModels,
                onDismissRequest = { expandedModels = false }
            ) {
                listOf("vit5", "phobert_vit5", "qwen").forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model.uppercase()) },
                        onClick = {
                            viewModel.onModelSelect(model)
                            expandedModels = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Max Length Slider
        Text("Độ dài tối đa: ${state.maxLength}", style = MaterialTheme.typography.titleSmall)
        Slider(
            value = state.maxLength.toFloat(),
            onValueChange = { viewModel.onMaxLengthChange(it.toInt()) },
            valueRange = 50f..512f,
            steps = 45,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Evaluation Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = state.showEvaluation,
                onCheckedChange = { viewModel.toggleEvaluation(it) }
            )
            Text("Đánh giá chất lượng")
        }
        
        // Reference Text (if evaluation enabled)
        if (state.showEvaluation) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.referenceText,
                onValueChange = { viewModel.onReferenceTextChange(it) },
                label = { Text("Văn bản tham chiếu") },
                placeholder = { Text("Nhập văn bản tóm tắt mẫu để so sánh...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Summarize Button
        Button(
            onClick = { viewModel.summarize() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && state.inputText.isNotBlank()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (state.isLoading) "Đang xử lý..." else "Tóm tắt")
        }
        
        // Error Message
        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // Success Message
        if (state.successMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = state.successMessage!!,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // Summary Result
        if (state.summary.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Kết quả tóm tắt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("${state.inferenceTimeMs.toInt()}ms") }  // Convert Double to Int for display
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = state.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Evaluation Results
        if (state.showEvaluation && (state.rouge1 > 0 || state.isEvaluating)) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Kết quả đánh giá",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (state.isEvaluating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        MetricRow("ROUGE-1", state.rouge1)
                        MetricRow("ROUGE-2", state.rouge2)
                        MetricRow("ROUGE-L", state.rougeL)
                        MetricRow("BLEU", state.bleu)
                        state.bertScore?.let {
                            MetricRow("BERTScore", it)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MetricRow(name: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium)
        Text(
            String.format("%.4f", value),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
