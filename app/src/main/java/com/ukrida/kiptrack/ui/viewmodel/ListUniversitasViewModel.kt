package com.ukrida.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProdiItem(
    val id: String,
    val nama: String,
    val jenjang: String,
    val kategori: String
)

data class ListUniversitasUiState(
    val isLoading: Boolean = true,
    val universityName: String = "Loading...",
    val prodiItems: List<ProdiItem> = emptyList(),
    val error: String? = null,
    val actionSuccess: String? = null
)

class ListUniversitasViewModel(private val universityId: String) : ViewModel() {
    var uiState by mutableStateOf(ListUniversitasUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchData()
    }

    private fun fetchData() {
        uiState = uiState.copy(isLoading = true)

        db.collection("universitas").document(universityId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("nama_kampus") ?: "Universitas"
                uiState = uiState.copy(universityName = name)
            }

        db.collection("universitas").document(universityId).collection("prodi")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    uiState = uiState.copy(isLoading = false, error = e.message)
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.map { doc ->
                    ProdiItem(
                        id = doc.id,
                        nama = doc.getString("nama") ?: "",
                        jenjang = doc.getString("jenjang") ?: "S1",
                        kategori = doc.getString("kategori") ?: "non-kedokteran"
                    )
                } ?: emptyList()

                uiState = uiState.copy(
                    prodiItems = list,
                    isLoading = false
                )
            }
    }

    fun addProdi(namaProdi: String, jenjang: String, kategori: String) {
        val cleanNama = namaProdi.lowercase().replace(" ", "_")
        val newDocId = "prodi_${cleanNama}_$universityId"

        val data = hashMapOf(
            "nama" to namaProdi,
            "jenjang" to jenjang,
            "kategori" to kategori
        )

        db.collection("universitas").document(universityId)
            .collection("prodi").document(newDocId)
            .set(data)
            .addOnSuccessListener { uiState = uiState.copy(actionSuccess = "Berhasil menambah Prodi") }
            .addOnFailureListener { e -> uiState = uiState.copy(error = e.message) }
    }

    fun updateProdi(prodiId: String, newName: String, newJenjang: String) = viewModelScope.launch {
        try {
            db.collection("universitas").document(universityId)
                .collection("prodi").document(prodiId)
                .update(
                    mapOf("nama" to newName, "jenjang" to newJenjang)
                ).await()
            uiState = uiState.copy(actionSuccess = "Prodi berhasil diupdate")
        } catch (e: Exception) {
            uiState = uiState.copy(error = "Gagal update: ${e.message}")
        }
    }

    fun deleteProdi(prodiId: String, passwordAdmin: String) = viewModelScope.launch {
        val user = auth.currentUser
        if (user == null || user.email == null) return@launch

        try {
            // Re-auth
            val credential = EmailAuthProvider.getCredential(user.email!!, passwordAdmin)
            user.reauthenticate(credential).await()

            // Delete Firestore Document
            db.collection("universitas").document(universityId)
                .collection("prodi").document(prodiId)
                .delete()
                .await()

            uiState = uiState.copy(actionSuccess = "Prodi berhasil dihapus")
        } catch (e: Exception) {
            uiState = uiState.copy(error = "Password salah atau gagal menghapus")
        }
    }

    fun resetActionState() { uiState = uiState.copy(error = null, actionSuccess = null) }
}

class ListUniversitasViewModelFactory(private val universityId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListUniversitasViewModel(universityId) as T
    }
}