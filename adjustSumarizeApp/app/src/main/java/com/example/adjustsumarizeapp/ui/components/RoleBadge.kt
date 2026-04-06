package com.example.adjustsumarizeapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Role Badge Component
 * Hiển thị badge phân biệt Admin/User
 */
@Composable
fun RoleBadge(
    role: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, text) = when (role.lowercase()) {
        "admin" -> Color(0xFFFFA726) to "Admin"  // Amber
        else -> Color(0xFF42A5F5) to "User"      // Blue
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
