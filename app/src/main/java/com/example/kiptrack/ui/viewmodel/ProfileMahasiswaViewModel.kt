package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.data.UserMahasiswa
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val isLoading: Boolean = true,
    val studentData: UserMahasiswa = UserMahasiswa(),
    val errorMessage: String? = null
)

class ProfileMahasiswaViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchFullProfile()
    }

    // 1. Fungsi Mengambil Data Profil Lengkap (User -> Univ -> Prodi)
    private fun fetchFullProfile() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            // A. Ambil Data User Utama
            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "User tidak ditemukan")
                return@launch
            }

            // Ambil ID referensi
            val idUniv = userDoc.getString("id_universitas") ?: ""
            val idProdi = userDoc.getString("id_prodi") ?: ""
            val rawFoto = userDoc.getString("foto_profil") ?: ""
            val rawWali = userDoc.getString("nama_wali") ?: "-"
            val rawSemester = userDoc.getLong("semester_berjalan")?.toString() ?: "1"

            // B. Ambil Nama Universitas (Lookup ke collection 'universitas')
            var namaUniv = "Tidak Diketahui"
            if (idUniv.isNotBlank()) {
                val univDoc = db.collection("universitas").document(idUniv).get().await()
                namaUniv = univDoc.getString("nama_kampus") ?: "Tidak Diketahui"
            }

            // C. Ambil Nama Prodi (Lookup ke sub-collection 'prodi')
            var namaProdi = "Tidak Diketahui"
            var jenjang = "S1"
            if (idUniv.isNotBlank() && idProdi.isNotBlank()) {
                val prodiDoc = db.collection("universitas").document(idUniv)
                    .collection("prodi").document(idProdi).get().await()
                namaProdi = prodiDoc.getString("nama") ?: "Tidak Diketahui"
                jenjang = prodiDoc.getString("jenjang") ?: "S1"
            }

            // D. Update State dengan Data Gabungan
            val completeData = UserMahasiswa(
                uid = uid,
                nim = userDoc.getString("nim") ?: "",
                nama = userDoc.getString("nama") ?: "",
                universityName = namaUniv,
                programStudiName = namaProdi,
                jenjang = jenjang,
                semesterBerjalan = "Semester $rawSemester",
                namaWali = rawWali,
                fotoProfil = rawFoto
            )

            uiState = uiState.copy(isLoading = false, studentData = completeData)

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Error: ${e.message}")
        }
    }

    // 2. Fungsi Update Foto Profil
    fun updateProfilePicture(base64Image: String) = viewModelScope.launch {
        try {
            // Update Firestore
            db.collection("users").document(uid)
                .update("foto_profil", base64Image)
                .await()

            // Update UI State Lokal (Biar langsung berubah tanpa loading ulang)
            val updatedData = uiState.studentData.copy(fotoProfil = base64Image)
            uiState = uiState.copy(studentData = updatedData)

        } catch (e: Exception) {
            uiState = uiState.copy(errorMessage = "Gagal upload foto: ${e.message}")
        }
    }
}

class ProfileMahasiswaViewModelFactory(private val uid: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileMahasiswaViewModel(uid) as T
    }
}