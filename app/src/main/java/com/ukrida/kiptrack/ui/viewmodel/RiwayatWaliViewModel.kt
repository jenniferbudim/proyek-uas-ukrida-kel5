package com.ukrida.kiptrack.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ukrida.kiptrack.ui.data.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// State khusus untuk layar Rincian
data class RiwayatWaliUiState(
    val isLoading: Boolean = true,
    val currentBalance: Long = 0L,
    val totalExpenditure: Long = 0L,
    val totalViolationsAmount: Long = 0L,
    val transactionList: List<Transaction> = emptyList()
)

class RiwayatWaliViewModel(private val waliUid: String) : ViewModel() {
    var uiState by mutableStateOf(RiwayatWaliUiState())
        private set
    private val db = FirebaseFirestore.getInstance()

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            // 1. Get Student ID from Wali
            val waliDoc = db.collection("users").document(waliUid).get().await()
            val studentUid = waliDoc.getString("id_mahasiswa") ?: ""
            if (studentUid.isBlank()) {
                uiState = uiState.copy(isLoading = false)
                return@launch
            }

            // 2. Get Balance
            val studentDoc = db.collection("users").document(studentUid).get().await()
            val saldo = studentDoc.getLong("saldo_saat_ini") ?: 0L

            // 3. Get Transactions
            val snapshot = db.collection("users").document(studentUid)
                .collection("laporan_keuangan")
                .get().await()

            val list = snapshot.documents.map { doc ->
                val d = doc.data ?: emptyMap()

                val img = d["bukti_base64"] as? String ?: ""

                Transaction(
                    id = doc.id,
                    date = d["tanggal"] as? String ?: "",
                    amount = (d["nominal"] as? Number)?.toLong() ?: 0L,
                    category = d["kategori"] as? String ?: "Lainnya",
                    status = d["status"] as? String ?: "MENUNGGU",
                    description = d["deskripsi"] as? String ?: "",

                    // Masukkan data gambar ke objek Transaction
                    proofImage = img
                )
            }

            // Sort Date Descending
            val fmt = SimpleDateFormat("MMM dd/yyyy", Locale.US)
            val sortedList = list.sortedByDescending {
                try { fmt.parse(it.date)?.time ?: 0L } catch(e:Exception){0L}
            }

            val totalExp = sortedList.sumOf { it.amount }
            val totalViol = sortedList.filter { it.status == "DITOLAK" }.sumOf { it.amount }

            uiState = uiState.copy(
                isLoading = false,
                currentBalance = saldo,
                totalExpenditure = totalExp,
                totalViolationsAmount = totalViol,
                transactionList = sortedList
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false)
        }
    }
}

class RiwayatWaliViewModelFactory(private val uid: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RiwayatWaliViewModel(uid) as T
    }
}