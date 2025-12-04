package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kiptrack.ui.data.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DashboardMahasiswaUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = "Loading...",
    val currentSaldo: Long = 0L,
    val photoProfile: String = "",

    // Data Grafik: List 12 Angka (Jan-Des)
    val graphData: List<Long> = List(12) { 0L },

    val transactionHistory: List<Transaction> = emptyList(),

    // State Tahun yang dipilih (Default 2025)
    val selectedYear: Int = 2025
)

class DashboardMahasiswaViewModel(private val uid: String) : ViewModel() {

    var uiState by mutableStateOf(DashboardMahasiswaUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private var transactionListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    init {
        if (uid.isNotBlank()) {
            setupRealtimeListeners()
        }
    }

    // Fungsi untuk Ganti Tahun (Dipanggil dari UI saat tombol panah diklik)
    fun changeYear(increment: Int) {
        val newYear = uiState.selectedYear + increment
        uiState = uiState.copy(selectedYear = newYear)
        // Hitung ulang grafik dengan tahun baru
        generateGraphData(uiState.transactionHistory, newYear)
    }

    private fun setupRealtimeListeners() {
        uiState = uiState.copy(isLoading = true)

        // 1. LISTEN USER (Profil, Saldo, & Foto)
        userListener = db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val nama = snapshot.getString("nama") ?: "Mahasiswa"
                val saldo = snapshot.getLong("saldo_saat_ini") ?: 0L

                // --- TAMBAHAN BARU: AMBIL FOTO ---
                val foto = snapshot.getString("foto_profil") ?: ""

                // Update State (Nama, Saldo, dan Foto)
                uiState = uiState.copy(
                    userName = nama,
                    currentSaldo = saldo,
                    photoProfile = foto // Masukkan ke UI State
                )
            }
        }

        // 2. LISTEN TRANSAKSI
        transactionListener = db.collection("users").document(uid)
            .collection("laporan_keuangan")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val transactionList = snapshot.documents.map { doc ->
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

                    uiState = uiState.copy(transactionHistory = transactionList, isLoading = false)
                    // Generate Grafik awal (sesuai tahun yang dipilih saat ini)
                    generateGraphData(transactionList, uiState.selectedYear)
                }
            }
    }

    private fun generateGraphData(transactions: List<Transaction>, yearFilter: Int) {
        val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US) // Contoh: JAN 25/2025
        val calendar = Calendar.getInstance()

        // Siapkan array kosong 12 bulan (Index 0=Jan, 11=Des)
        val monthlyTotals = MutableList(12) { 0L }

        transactions.filter { it.status == "DISETUJUI" }.forEach { trx ->
            try {
                val date = dateFormat.parse(trx.date)
                if (date != null) {
                    calendar.time = date
                    val trxYear = calendar.get(Calendar.YEAR)
                    val trxMonth = calendar.get(Calendar.MONTH) // 0 to 11

                    // Hanya masukkan data jika tahunnya cocok
                    if (trxYear == yearFilter) {
                        monthlyTotals[trxMonth] += trx.amount
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }

        uiState = uiState.copy(graphData = monthlyTotals)
    }

    override fun onCleared() {
        super.onCleared()
        transactionListener?.remove()
        userListener?.remove()
    }
}