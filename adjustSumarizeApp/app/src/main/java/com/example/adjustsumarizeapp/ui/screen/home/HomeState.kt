package com.example.adjustsumarizeapp.ui.screen.home

import com.example.adjustsumarizeapp.data.model.ColabHealthResponse

data class HomeState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val colabStatus: ColabConnectionStatus = ColabConnectionStatus.UNKNOWN,
    val colabHealth: ColabHealthResponse? = null
)

enum class ColabConnectionStatus {
    UNKNOWN,      // Chưa kiểm tra
    CHECKING,     // Đang kiểm tra
    CONNECTED,    // Đã kết nối
    DISCONNECTED  // Không kết nối được
}
