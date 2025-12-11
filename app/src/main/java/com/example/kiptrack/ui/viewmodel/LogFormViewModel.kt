package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.utils.NumberUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogFormUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,

    val dateInput: String = SimpleDateFormat("MMM dd/yyyy", Locale.US).format(Date()).uppercase(),
    val description: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val category: String? = null,
    val photoBase64: String = "",

    val totalCalculated: Long = 0L,
    val totalSpelled: String = "Nol Rupiah"
)

class LogFormViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(LogFormUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    fun onDateChange(value: String) {

        try {
            val inputFormat = SimpleDateFormat("dd / MM / yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)

            val date = inputFormat.parse(value)
            if (date != null) {
                val formatted = outputFormat.format(date).uppercase()
                uiState = uiState.copy(dateInput = formatted)
            } else {
                uiState = uiState.copy(dateInput = value)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(dateInput = value)
        }
    }

    fun onDescriptionChange(value: String) { uiState = uiState.copy(description = value) }
    fun onCategoryChange(value: String) { uiState = uiState.copy(category = value) }
    fun onPhotoSelected(base64: String) { uiState = uiState.copy(photoBase64 = base64) }

    fun onQuantityChange(value: String) {
        if (value.all { it.isDigit() }) {
            uiState = uiState.copy(quantity = value)
            recalculateTotal()
        }
    }

    fun onUnitPriceChange(value: String) {
        if (value.all { it.isDigit() }) {
            uiState = uiState.copy(unitPrice = value)
            recalculateTotal()
        }
    }

    private fun recalculateTotal() {
        val qty = uiState.quantity.toLongOrNull() ?: 0L
        val price = uiState.unitPrice.toLongOrNull() ?: 0L
        val total = qty * price

        uiState = uiState.copy(
            totalCalculated = total,
            totalSpelled = NumberUtils.terbilang(total)
        )
    }

    fun submitReport() = viewModelScope.launch {
        if (uiState.dateInput.isBlank() || uiState.description.isBlank() ||
            uiState.quantity.isBlank() || uiState.unitPrice.isBlank() ||
            uiState.category == null) {
            uiState = uiState.copy(errorMessage = "Mohon lengkapi semua data!")
            return@launch
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        try {
            val totalNominal = uiState.totalCalculated
            val qty = uiState.quantity.toIntOrNull() ?: 1
            val price = uiState.unitPrice.toLongOrNull() ?: 0L

            val reportData = hashMapOf(
                "tanggal" to uiState.dateInput,
                "deskripsi" to uiState.description,
                "kategori" to uiState.category,
                "kuantitas" to qty,
                "harga_satuan" to price,
                "nominal" to totalNominal,
                "bukti_base64" to uiState.photoBase64,
                "status" to "MENUNGGU",
                "created_at" to System.currentTimeMillis()
            )

            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(uid)
                val reportRef = userRef.collection("laporan_keuangan").document()

                val snapshot = transaction.get(userRef)
                val currentSaldo = snapshot.getLong("saldo_saat_ini") ?: 0L
                val newSaldo = currentSaldo - totalNominal

                transaction.update(userRef, "saldo_saat_ini", newSaldo)
                transaction.set(reportRef, reportData)
            }.await()

            uiState = uiState.copy(isLoading = false, isSuccess = true)

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Gagal simpan: ${e.message}")
        }
    }
}

class LogFormViewModelFactory(private val uid: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LogFormViewModel(uid) as T
    }
}