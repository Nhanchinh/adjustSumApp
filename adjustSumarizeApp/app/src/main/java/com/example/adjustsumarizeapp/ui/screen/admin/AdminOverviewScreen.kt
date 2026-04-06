package com.example.adjustsumarizeapp.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminOverviewScreen(
    viewModel: AdminOverviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tổng quan Admin",
                style = MaterialTheme.typography.headlineMedium
            )
            
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        
        // Loading
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Statistics Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Tổng tóm tắt",
                value = state.totalSummaries.toString(),
                icon = Icons.Default.Assessment,
                color = Color(0xFF42A5F5),
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "Users",
                value = state.totalUsers.toString(),
                icon = Icons.Default.People,
                color = Color(0xFF66BB6A),
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Có feedback",
                value = state.totalWithFeedback.toString(),
                icon = Icons.Default.ThumbUp,
                color = Color(0xFFAB47BC),
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "Tỉ lệ feedback",
                value = "${String.format("%.0f", state.feedbackRate)}%",
                icon = Icons.Default.Percent,
                color = Color(0xFFFFA726),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Model Stats Card (dynamic from API)
        if (state.modelStats.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Thống kê Models",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    state.modelStats.forEachIndexed { index, stats ->
                        ModelStatsRow(stats)
                        if (index < state.modelStats.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        
        // Rating Distribution
        if (state.ratingDistribution.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Phân bố đánh giá",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    state.ratingDistribution.forEach { (rating, count) ->
                        val (label, color) = when (rating) {
                            "good" -> "Tốt" to Color(0xFF66BB6A)
                            "bad" -> "Kém" to Color(0xFFEF5350)
                            else -> "Trung bình" to Color(0xFFFFA726)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(label)
                            }
                            Text(
                                "$count lượt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Server Status Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Server Status",
                    style = MaterialTheme.typography.titleMedium
                )
                
                InfoRow(
                    "Colab",
                    if (state.colabStatus == "connected") "Connected" else "Disconnected"
                )
                InfoRow(
                    "GPU",
                    when (state.gpuAvailable) {
                        true -> "Available"
                        false -> "Not Available"
                        null -> "Unknown"
                    }
                )
                InfoRow(
                    "Avg Response",
                    "${String.format("%.0f", state.avgProcessingTimeMs)}ms"
                )
            }
        }
        
        // Error State
        val error = state.error
        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModelStatsRow(stats: com.example.adjustsumarizeapp.data.model.ModelStatsDto) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stats.model,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${stats.count} lượt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Good: ${stats.goodCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF66BB6A)
            )
            Text(
                "Bad: ${stats.badCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFEF5350)
            )
            Text(
                "Avg: ${String.format("%.0f", stats.avgProcessingTimeMs)}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
