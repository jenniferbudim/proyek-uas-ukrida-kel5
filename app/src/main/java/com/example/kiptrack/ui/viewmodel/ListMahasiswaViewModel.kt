package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

data class MahasiswaItem(val uid: String, val nama: String, val nim: String)

data class ListMahasiswaUiState(
    val isLoading: Boolean = true,
    val prodiName: String = "Daftar Mahasiswa",
    val mahasiswaList: List<MahasiswaItem> = emptyList()
)

class ListMahasiswaViewModel(private val universityId: String, private val prodiId: String) : ViewModel() {
    var uiState by mutableStateOf(ListMahasiswaUiState())
        private set
    private val db = FirebaseFirestore.getInstance()

    init {
        fetchData()
    }

    private fun fetchData() {
        // 1. Ambil Nama Prodi (untuk Judul)
        db.collection("universitas").document(universityId)
            .collection("prodi").document(prodiId).get()
            .addOnSuccessListener {
                val name = it.getString("nama") ?: "Prodi"
                uiState = uiState.copy(prodiName = name)
            }

        // 2. Ambil List Mahasiswa (Filter by Univ & Prodi)
        db.collection("users")
            .whereEqualTo("role", "mahasiswa")
            .whereEqualTo("id_universitas", universityId)
            .whereEqualTo("id_prodi", prodiId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    MahasiswaItem(
                        uid = doc.id,
                        nama = doc.getString("nama") ?: "Tanpa Nama",
                        nim = doc.getString("nim") ?: "-"
                    )
                }
                uiState = uiState.copy(mahasiswaList = list, isLoading = false)
            }
    }
}

class ListMahasiswaViewModelFactory(private val uId: String, private val pId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListMahasiswaViewModel(uId, pId) as T
    }
}