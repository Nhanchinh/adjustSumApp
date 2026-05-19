package com.example.adjustsumarizeapp.ui.screen.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.adjustsumarizeapp.data.model.UserPublicDto

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
        
        // ==================== User Management Section ====================
        UserManagementCard(users = state.users)
        
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

// ==================== User Management Card ====================

@Composable
private fun UserManagementCard(users: List<UserPublicDto>) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header - clickable to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF66BB6A).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = Color(0xFF66BB6A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Quản lý người dùng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${users.size} tài khoản",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng"
                    )
                }
            }
            
            // Summary row - always visible
            if (!isExpanded && users.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val adminCount = users.count { it.role == "admin" }
                    val userCount = users.count { it.role == "user" }
                    val consentCount = users.count { it.consentShareData }
                    
                    SummaryChip(
                        label = "Admin",
                        count = adminCount,
                        color = Color(0xFFEF5350)
                    )
                    SummaryChip(
                        label = "User",
                        count = userCount,
                        color = Color(0xFF42A5F5)
                    )
                    SummaryChip(
                        label = "Chia sẻ data",
                        count = consentCount,
                        color = Color(0xFF66BB6A)
                    )
                }
            }
            
            // Expanded user list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    if (users.isEmpty()) {
                        Text(
                            text = "Chưa có tài khoản nào",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        users.forEachIndexed { index, user ->
                            UserRow(user = user)
                            if (index < users.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, count: Int, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserRow(user: UserPublicDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with initial letter
        val initial = (user.fullName?.firstOrNull() ?: user.email.first()).uppercase()
        val avatarColor = if (user.role == "admin") Color(0xFFEF5350) else Color(0xFF42A5F5)
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = avatarColor
            )
        }
        
        // User info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = user.fullName ?: user.email.substringBefore("@"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Role badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (user.role == "admin") 
                        Color(0xFFEF5350).copy(alpha = 0.1f) 
                    else 
                        Color(0xFF42A5F5).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = user.role.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (user.role == "admin") Color(0xFFEF5350) else Color(0xFF42A5F5),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp
                    )
                }
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Consent indicator
        Icon(
            imageVector = if (user.consentShareData) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = if (user.consentShareData) "Chia sẻ dữ liệu" else "Không chia sẻ",
            tint = if (user.consentShareData) 
                Color(0xFF66BB6A).copy(alpha = 0.7f) 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// ==================== Existing Components ====================

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
