package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.event.LoginEvent
import com.example.kiptrack.ui.model.UserRole
import com.example.kiptrack.ui.state.LoginUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class LoginSuccessEvent(val role: UserRole, val uid: String)

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _navigationEvent = Channel<LoginSuccessEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

            is LoginEvent.OnLoginSuccess -> { 
                // This event is handled by the UI, not the ViewModel
            } 
        }
    }

    private fun performLogin() {
        val currentId = uiState.inputId.trim()
        val currentPassword = uiState.password.trim()

        if (currentId.isBlank() || currentPassword.isBlank()) {
            uiState = uiState.copy(errorMessage = "Form tidak boleh kosong")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        val emailFormat = "$currentId@kiptrack.com"

        auth.signInWithEmailAndPassword(emailFormat, currentPassword)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    checkUserRole(uid)
                } else {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Gagal mendapatkan UID")
                }
            }
            .addOnFailureListener { exception ->
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Login Gagal: ${exception.localizedMessage ?: "Cek ID dan Password"}"
                )
            }
    }

    private fun checkUserRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val roleDiDatabase = document.getString("role")

                    val roleDiUI = if (!uiState.isGeneralLogin) "admin"
                    else if (uiState.selectedRole == UserRole.MAHASISWA) "mahasiswa"
                    else "wali"

                    if (roleDiDatabase == roleDiUI) {
                        val userRole = UserRole.valueOf(roleDiDatabase.uppercase())

                        viewModelScope.launch {
                            _navigationEvent.send(LoginSuccessEvent(userRole, uid))
                        }

                    } else {
                        auth.signOut()
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = "Akun tidak terdaftar sebagai ${roleDiUI.uppercase()}"
                        )
                    }
                } else {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Data user tidak ditemukan di database.")
                }
            }
            .addOnFailureListener { e ->
                uiState = uiState.copy(isLoading = false, errorMessage = "Gagal koneksi database: ${e.message}")
            }
    }
}
