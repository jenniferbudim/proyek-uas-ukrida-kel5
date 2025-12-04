package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kiptrack.ui.data.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DashboardMahasiswaUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = "Loading...",
    val photoProfile: String = "",

    // Data Keuangan
    val currentSaldo: Long = 8000000L, // Default Awal
    val totalViolations: Long = 0L,
    val nextSemesterAllowance: Long = 0L,

    val graphData: List<Long> = List(12) { 0L },
    val transactionHistory: List<Transaction> = emptyList(),
    val selectedYear: Int = 2025
)

class DashboardMahasiswaViewModel(private val uid: String) : ViewModel() {

    var uiState by mutableStateOf(DashboardMahasiswaUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private var transactionListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    // PAGU AWAL (Hardcode Cluster 1 dulu, idealnya ambil dari collection konfigurasi)
    private val INITIAL_BUDGET = 8000000L

    init {
        if (uid.isNotBlank()) {
            setupRealtimeListeners()
        }
    }

    fun changeYear(increment: Int) {
        val newYear = uiState.selectedYear + increment
        uiState = uiState.copy(selectedYear = newYear)
        generateGraphData(uiState.transactionHistory, newYear)
    }

    private fun setupRealtimeListeners() {
        uiState = uiState.copy(isLoading = true)

        // 1. LISTEN USER (Hanya ambil Nama, Foto, Pelanggaran)
        // Kita tidak ambil saldo dari sini lagi agar tidak bentrok logika
        userListener = db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val nama = snapshot.getString("nama") ?: "Mahasiswa"
                val foto = snapshot.getString("foto_profil") ?: ""
                val pelanggaran = snapshot.getLong("total_penyalahgunaan") ?: 0L
                val semesterDepan = (INITIAL_BUDGET - pelanggaran).coerceAtLeast(0L)

                uiState = uiState.copy(
                    userName = nama,
                    photoProfile = foto,
                    totalViolations = pelanggaran,
                    nextSemesterAllowance = semesterDepan
                )
            }
        }

        // 2. LISTEN TRANSAKSI & HITUNG ULANG SALDO OTOMATIS
        transactionListener = db.collection("users").document(uid)
            .collection("laporan_keuangan")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val rawList = snapshot.documents.map { doc ->
                        Transaction(
                            id = doc.id,
                            date = doc.getString("tanggal") ?: "",
                            amount = doc.getLong("nominal") ?: 0L,
                            description = doc.getString("deskripsi") ?: "",
                            status = doc.getString("status") ?: "MENUNGGU",
                            quantity = doc.getLong("kuantitas")?.toInt() ?: 1,
                            unitPrice = doc.getLong("harga_satuan") ?: 0L,
                            proofImage = doc.getString("bukti_base64") ?: ""
                        )
                    }

                    // A. Sort Tanggal
                    val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
                    val sortedList = rawList.sortedByDescending {
                        try { dateFormat.parse(it.date)?.time ?: 0L } catch (e: Exception) { 0L }
                    }

                    // B. HITUNG TOTAL PENGELUARAN (Semua Status)
                    // Logic: Saldo berkurang walaupun status Menunggu/Ditolak/Disetujui
                    val totalSpent = rawList.sumOf { it.amount }

                    // C. HITUNG SALDO REAL (Pagu Awal - Total Keluar)
                    val realBalance = INITIAL_BUDGET - totalSpent

                    // D. UPDATE UI STATE
                    uiState = uiState.copy(
                        transactionHistory = sortedList,
                        currentSaldo = realBalance, // Gunakan hasil hitungan realtime
                        isLoading = false
                    )

                    // E. UPDATE DATABASE (Auto-Correction)
                    // Kita update field saldo_saat_ini di DB agar sinkron
                    db.collection("users").document(uid)
                        .update("saldo_saat_ini", realBalance)
                        .addOnFailureListener {
                            println("Gagal sync saldo: ${it.message}")
                        }

                    generateGraphData(sortedList, uiState.selectedYear)
                }
            }
    }

    private fun generateGraphData(transactions: List<Transaction>, yearFilter: Int) {
        val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
        val calendar = Calendar.getInstance()
        val monthlyTotals = MutableList(12) { 0L }

        transactions.forEach { trx ->
            try {
                val date = dateFormat.parse(trx.date)
                if (date != null) {
                    calendar.time = date
                    val trxYear = calendar.get(Calendar.YEAR)
                    val trxMonth = calendar.get(Calendar.MONTH)

                    if (trxYear == yearFilter) {
                        monthlyTotals[trxMonth] += trx.amount
                    }
                }
            } catch (e: Exception) { }
        }
        uiState = uiState.copy(graphData = monthlyTotals)
    }

    override fun onCleared() {
        super.onCleared()
        transactionListener?.remove()
        userListener?.remove()
    }
}