package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kiptrack.ui.event.LoginEvent
import com.example.kiptrack.ui.state.LoginUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    // Inisialisasi Firebase Auth & Firestore
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
        }
    }

    private fun performLogin() {
        val currentId = uiState.inputId.trim()
        val currentPassword = uiState.password.trim()

        // 1. Validasi Input Kosong
        if (currentId.isBlank() || currentPassword.isBlank()) {
            uiState = uiState.copy(errorMessage = "Form tidak boleh kosong")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        // 2. Manipulasi Email (Strategy @kiptrack.com)
        // Kita tambahkan domain palsu agar diterima Firebase Auth
        val emailFormat = "$currentId@kiptrack.com"

        // 3. Eksekusi Login ke Firebase
        auth.signInWithEmailAndPassword(emailFormat, currentPassword)
            .addOnSuccessListener { authResult ->
                // Login Berhasil -> Cek apakah Role di database sesuai dengan yang dipilih di UI
                val uid = authResult.user?.uid
                if (uid != null) {
                    checkUserRole(uid)
                } else {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Gagal mendapatkan UID")
                }
            }
            .addOnFailureListener { exception ->
                // Login Gagal (Password salah / User tidak ditemukan)
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Login Gagal: ${exception.localizedMessage ?: "Cek ID dan Password"}"
                )
            }
    }

    // Fungsi untuk memverifikasi apakah user yang login punya role yang benar
    private fun checkUserRole(uid: String) {
        // Kita ambil data user dari collection 'users'
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val roleDiDatabase = document.getString("role") // "mahasiswa", "wali", atau "admin"

                    // Logika Pengecekan Role UI vs Database
                    val roleDiUI = if (!uiState.isGeneralLogin) "admin"
                    else if (uiState.selectedRole.name == "MAHASISWA") "mahasiswa"
                    else "wali"

                    if (roleDiDatabase == roleDiUI) {
                        // SUKSES SEPENUHNYA
                        uiState = uiState.copy(isLoading = false)
                        println("LOGIN SUKSES! User: $uid sebagai $roleDiDatabase")

                        // TODO: Di sini nanti kita akan trigger navigasi ke Dashboard (Task selanjutnya)

                    } else {
                        // User ada, tapi salah kamar (Misal Admin coba login di tab Mahasiswa)
                        auth.signOut() // Logout paksa
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