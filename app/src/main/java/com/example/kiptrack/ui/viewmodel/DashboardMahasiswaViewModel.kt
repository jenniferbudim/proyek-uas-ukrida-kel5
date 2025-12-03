package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.data.Transaction
import com.example.kiptrack.ui.data.UserMahasiswa
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// State for the Dashboard UI
data class DashboardMahasiswaUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = "User",
    val currentSaldo: Long = 0L,
    val saldoHistory: List<Long> = emptyList(),
    val transactionHistory: List<Transaction> = emptyList()
)

class DashboardMahasiswaViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(DashboardMahasiswaUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var transactionListener: ListenerRegistration? = null

    init {
        if (uid.isNotBlank()) {
            fetchUserData()
            setupTransactionListener()
        } else {
            uiState = uiState.copy(isLoading = false, errorMessage = "UID is invalid.")
        }
    }

    private fun fetchUserData() = viewModelScope.launch {
        try {
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val userData = UserMahasiswa(userDoc.data ?: emptyMap())
                uiState = uiState.copy(
                    userName = userData.nama.ifBlank { "Blessy" }, // Use mock name if blank
                    isLoading = false
                )
            } else {
                uiState = uiState.copy(
                    userName = "Blessy", // Fallback to mock name
                    isLoading = false,
                    errorMessage = "User data not found for UID: $uid"
                )
            }
        } catch (e: Exception) {
            uiState = uiState.copy(
                userName = "Blessy", // Fallback to mock name
                isLoading = false,
                errorMessage = "Gagal memuat data user: ${e.message}"
            )
        }
    }

    private fun setupTransactionListener() {
        // Mock data for Saldo History (10 data points for the graph)
        val mockSaldoHistory = listOf(500000L, 750000L, 600000L, 900000L, 1000000L, 300000L, 700000L, 550000L, 850000L, 200000L)
        val currentSaldo = mockSaldoHistory.lastOrNull() ?: 0L

        // Mock data for Transaction History (Riwayat Pengeluaran)
        val mockTransactions = listOf(
            Transaction(UUID.randomUUID().toString(), "JAN 16/2025", 50000L, "Buku", true),
            Transaction(UUID.randomUUID().toString(), "JAN 16/2025", 20000L, "Alat Tulis", false),
            Transaction(UUID.randomUUID().toString(), "DEC 10/2024", 15000L, "Makan Siang", true),
        )

        uiState = uiState.copy(
            isLoading = false,
            currentSaldo = currentSaldo,
            saldoHistory = mockSaldoHistory,
            transactionHistory = mockTransactions
        )

        // *** REAL FIREBASE SETUP WOULD GO HERE ***
        //
        // val transactionsRef = db.collection("artifacts").document(__app_id)
        //     .collection("users").document(uid)
        //     .collection("transactions")
        //
        // transactionListener = transactionsRef.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
        //    .addSnapshotListener { snapshot, e ->
        //        if (e != null) {
        //            uiState = uiState.copy(errorMessage = "Error fetching transactions: ${e.message}")
        //            return@addSnapshotListener
        //        }
        //
        //        if (snapshot != null && !snapshot.isEmpty) {
        //            val newTransactions = snapshot.documents.map { doc ->
        //                // Conversion logic from Firestore document to Transaction data class
        //                // ...
        //            }
        //            uiState = uiState.copy(transactionHistory = newTransactions)
        //        }
        //    }
    }

    override fun onCleared() {
        super.onCleared()
        transactionListener?.remove() // Remove listener when ViewModel is destroyed
    }
}