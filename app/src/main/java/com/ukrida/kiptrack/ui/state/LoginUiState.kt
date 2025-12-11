package com.ukrida.kiptrack.ui.state

import com.ukrida.kiptrack.ui.data.UserRole

data class LoginUiState(
    val isGeneralLogin: Boolean = true, // true = General, false = Admin
    val selectedRole: UserRole = UserRole.MAHASISWA,
    val inputId: String = "", // Represents NIM for Mahasiswa, ID for Wali, Username for Admin
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)