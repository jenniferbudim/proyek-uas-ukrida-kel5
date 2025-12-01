package com.example.kiptrack.ui.state

import com.example.kiptrack.ui.screen.UserRole

data class LoginUiState(
    val isGeneralLogin: Boolean = true, // true = General, false = Admin
    val selectedRole: UserRole = UserRole.MAHASISWA,
    val inputId: String = "", // Represents NIM for Mahasiswa, ID for Wali, Username for Admin
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)