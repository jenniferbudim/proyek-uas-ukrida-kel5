package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// State untuk Form UI
data class LogFormUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,

    // Field Input
    val dateInput: String = "",
    val description: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val category: String? = null,
    val photoBase64: String = ""
)

class LogFormViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(LogFormUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    // Fungsi Update State dari UI
    fun onDateChange(value: String) { uiState = uiState.copy(dateInput = value) }
    fun onDescriptionChange(value: String) { uiState = uiState.copy(description = value) }
    fun onQuantityChange(value: String) { uiState = uiState.copy(quantity = value) }
    fun onUnitPriceChange(value: String) { uiState = uiState.copy(unitPrice = value) }
    fun onCategoryChange(value: String) { uiState = uiState.copy(category = value) }
    fun onPhotoSelected(base64: String) { uiState = uiState.copy(photoBase64 = base64) }

    // Fungsi Simpan Laporan
    fun submitReport() = viewModelScope.launch {
        // 1. Validasi Input
        if (uiState.dateInput.isBlank() || uiState.description.isBlank() ||
            uiState.quantity.isBlank() || uiState.unitPrice.isBlank() ||
            uiState.category == null) {
            uiState = uiState.copy(errorMessage = "Mohon lengkapi semua data!")
            return@launch
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        try {
            // 2. Hitung Nominal Total
            val qty = uiState.quantity.toIntOrNull() ?: 1
            val price = uiState.unitPrice.toLongOrNull() ?: 0L
            val totalNominal = qty * price

            // 3. Siapkan Data Map untuk Firestore
            val reportData = hashMapOf(
                "tanggal" to uiState.dateInput,
                "deskripsi" to uiState.description,
                "kategori" to uiState.category,
                "kuantitas" to qty,
                "harga_satuan" to price,
                "nominal" to totalNominal,
                "bukti_base64" to uiState.photoBase64,
                "status" to "MENUNGGU" // <-- STATUS AWAL PENDING
            )

            // 4. Kirim ke Sub-Collection
            db.collection("users").document(uid)
                .collection("laporan_keuangan")
                .add(reportData)
                .await()

            // Sukses!
            uiState = uiState.copy(isLoading = false, isSuccess = true)

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Gagal simpan: ${e.message}")
        }
    }
}

// Factory
class LogFormViewModelFactory(private val uid: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LogFormViewModel(uid) as T
    }
}