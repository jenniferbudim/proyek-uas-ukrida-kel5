package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kiptrack.ui.data.UserAdmin
import com.example.kiptrack.ui.data.UserMahasiswa
import com.example.kiptrack.ui.data.UserWali
import com.example.kiptrack.ui.event.LoginEvent
import com.example.kiptrack.ui.screen.UserRole
import com.example.kiptrack.ui.state.LoginUiState

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnIdChange -> {
                uiState = uiState.copy(inputId = event.value, errorMessage = null)
            }
            is LoginEvent.OnPasswordChange -> {
                uiState = uiState.copy(password = event.value, errorMessage = null)
            }
            is LoginEvent.OnRoleSelected -> {
                uiState = uiState.copy(selectedRole = event.role, inputId = "", password = "", errorMessage = null)
            }
            is LoginEvent.OnToggleAdminMode -> {
                // Switch screens and reset inputs
                uiState = uiState.copy(
                    isGeneralLogin = !uiState.isGeneralLogin,
                    inputId = "",
                    password = "",
                    errorMessage = null
                )
            }
            is LoginEvent.OnLoginClicked -> {
                performLogin()
            }
        }
    }

    private fun performLogin() {
        val currentId = uiState.inputId
        val currentPassword = uiState.password

        if (currentId.isBlank() || currentPassword.isBlank()) {
            uiState = uiState.copy(errorMessage = "Form tidak boleh kosong")
            return
        }

        uiState = uiState.copy(isLoading = true)

        // Simulating data processing
        if (uiState.isGeneralLogin) {
            when (uiState.selectedRole) {
                UserRole.MAHASISWA -> {
                    println("Login Request: Mahasiswa (NIM: $currentId)")
                    val potentialUser = UserMahasiswa(
                        nim = currentId,
                        nama = "Mahasiswa Test",
                        password = currentPassword
                    )
                }
                UserRole.ORANG_TUA -> {
                    println("Login Request: Wali (ID: $currentId)")
                    val potentialUser =
                        UserWali(id = currentId, nama = "Wali Test", password = currentPassword)
                }
            }
        } else {
            println("Login Request: Admin (Username: $currentId)")
            val potentialUser =
                UserAdmin(id = "admin_01", username = currentId, password = currentPassword)
        }

        // Reset loading for demo
        uiState = uiState.copy(isLoading = false)
    }
}
