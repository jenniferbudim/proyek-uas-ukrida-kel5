package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

data class ListUniversitasUiState(
    val universityName: String = "Loading...",
    val prodiList: List<String> = emptyList(), // List Nama Prodi
    val prodiIds: List<String> = emptyList()   // List ID Prodi untuk navigasi
)

class ListUniversitasViewModel(private val universityId: String) : ViewModel() {
    var uiState by mutableStateOf(ListUniversitasUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchData()
    }

    private fun fetchData() {
        // 1. Ambil Nama Universitas
        db.collection("universitas").document(universityId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("nama_kampus") ?: "Universitas"
                uiState = uiState.copy(universityName = name)
            }

        // 2. Ambil List Prodi (Sub-Collection)
        db.collection("universitas").document(universityId).collection("prodi")
            .get()
            .addOnSuccessListener { snapshot ->
                val names = snapshot.documents.map { it.getString("nama") ?: "" }
                val ids = snapshot.documents.map { it.id }

                uiState = uiState.copy(
                    prodiList = names,
                    prodiIds = ids
                )
            }
    }

    // FUNGSI BARU: TAMBAH PRODI
    fun addProdi(namaProdi: String, jenjang: String, kategori: String) {
        val cleanNama = namaProdi.lowercase().replace(" ", "_")
        // Format ID: prodi_akuntansi_univ_ukrida
        val newDocId = "prodi_${cleanNama}_$universityId"

        val data = hashMapOf(
            "nama" to namaProdi,
            "jenjang" to jenjang,
            "kategori" to kategori
        )

        db.collection("universitas").document(universityId)
            .collection("prodi").document(newDocId)
            .set(data)
            .addOnSuccessListener {
                fetchData() // Refresh list setelah berhasil
            }
            .addOnFailureListener { e ->
                println("Gagal tambah prodi: ${e.message}")
            }
    }
}

class ListUniversitasViewModelFactory(private val universityId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListUniversitasViewModel(universityId) as T
    }
}