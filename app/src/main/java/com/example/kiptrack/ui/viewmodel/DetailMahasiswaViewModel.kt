package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.data.CategorySummary
import com.example.kiptrack.ui.data.Transaction
import com.example.kiptrack.ui.theme.PieGreen
import com.example.kiptrack.ui.theme.PieOrange
import com.example.kiptrack.ui.theme.PieRed
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// State untuk Detail Mahasiswa
data class DetailMahasiswaUiState(
    val isLoading: Boolean = true,
    val name: String = "Loading...",
    val saldo: Long = 0L,
    val photoProfile: String = "",

    // Statistik
    val totalPengeluaran: Long = 0L,
    val totalPelanggaran: Long = 0L,
    val transactionList: List<Transaction> = emptyList(),

    // Data Grafik
    val graphData: List<Long> = List(12) { 0L },
    val categoryData: List<CategorySummary> = emptyList(),

    // Transaksi yang sedang dipilih untuk di-review (Tab Perincian/Konfirmasi)
    val selectedTransaction: Transaction? = null
)

class DetailMahasiswaViewModel(
    private val adminUid: String,
    private val studentUid: String
) : ViewModel() {

    var uiState by mutableStateOf(DetailMahasiswaUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchStudentData()
        fetchTransactions()
    }

    // 1. Ambil Data Diri Mahasiswa
    private fun fetchStudentData() = viewModelScope.launch {
        try {
            val doc = db.collection("users").document(studentUid).get().await()
            if (doc.exists()) {
                val nama = doc.getString("nama") ?: "Mahasiswa"
                val saldo = doc.getLong("saldo_saat_ini") ?: 0L
                val foto = doc.getString("foto_profil") ?: ""
                val pelanggaran = doc.getLong("total_penyalahgunaan") ?: 0L

                uiState = uiState.copy(
                    name = nama,
                    saldo = saldo,
                    photoProfile = foto,
                    totalPelanggaran = pelanggaran
                )
            }
        } catch (e: Exception) {
            println("Error fetch student: ${e.message}")
        }
    }

    // 2. Ambil Riwayat Transaksi & Hitung Statistik
    private fun fetchTransactions() = viewModelScope.launch {
        try {
            val snapshot = db.collection("users").document(studentUid)
                .collection("laporan_keuangan")
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = snapshot.documents.map { doc ->
                Transaction(
                    id = doc.id,
                    date = doc.getString("tanggal") ?: "",
                    amount = doc.getLong("nominal") ?: 0L,
                    description = doc.getString("deskripsi") ?: "",
                    status = doc.getString("status") ?: "MENUNGGU",
                    quantity = doc.getLong("kuantitas")?.toInt() ?: 1,
                    unitPrice = doc.getLong("harga_satuan") ?: 0L,
                    proofImage = doc.getString("bukti_base64") ?: "",
                    // category field needed for pie chart
                    // Asumsikan di Transaction.kt ada field 'category', kalau belum ada, abaikan dulu logic pie chart
                )
            }

            // Hitung Total Pengeluaran
            val totalSpent = list.sumOf { it.amount }

            // Generate Grafik Line (Bulanan)
            val graph = generateGraphData(list)

            // Generate Pie Chart (Dummy category logic for now or real if data exists)
            val pieData = listOf(
                CategorySummary("Makanan", 50f, PieRed),
                CategorySummary("Transport", 30f, PieGreen),
                CategorySummary("Lainnya", 20f, PieOrange)
            )

            uiState = uiState.copy(
                isLoading = false,
                transactionList = list,
                totalPengeluaran = totalSpent,
                graphData = graph,
                categoryData = pieData,
                // Default select transaksi pertama jika ada
                selectedTransaction = list.firstOrNull()
            )

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false)
        }
    }

    // Helper Grafik
    private fun generateGraphData(list: List<Transaction>): List<Long> {
        val monthlyTotals = MutableList(12) { 0L }
        val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
        list.forEach { trx ->
            try {
                val date = dateFormat.parse(trx.date)
                if (date != null) {
                    // Sederhana: Abaikan tahun, masukkan ke bulan (0-11)
                    val month = date.month // Deprecated but easy for index
                    monthlyTotals[month] += trx.amount
                }
            } catch (e: Exception) {}
        }
        return monthlyTotals
    }

    // 3. Fungsi Select Transaksi (Dipanggil saat klik list)
    fun selectTransaction(transaction: Transaction) {
        uiState = uiState.copy(selectedTransaction = transaction)
    }

    // 4. Admin Action: Approve
    fun approveTransaction(trxId: String) = viewModelScope.launch {
        updateStatus(trxId, "DISETUJUI")
    }

    // 5. Admin Action: Deny
    fun denyTransaction(trxId: String, amount: Long) = viewModelScope.launch {
        // Update Status jadi DITOLAK
        updateStatus(trxId, "DITOLAK")

        // Update Total Pelanggaran di User (+ Nominal)
        try {
            db.runTransaction { transaction ->
                val userRef = db.collection("users").document(studentUid)
                val snapshot = transaction.get(userRef)
                val currentViolations = snapshot.getLong("total_penyalahgunaan") ?: 0L
                transaction.update(userRef, "total_penyalahgunaan", currentViolations + amount)
            }.await()

            // Refresh data UI
            fetchStudentData()
        } catch (e: Exception) {
            println("Error update pelanggaran: ${e.message}")
        }
    }

    private suspend fun updateStatus(trxId: String, status: String) {
        try {
            db.collection("users").document(studentUid)
                .collection("laporan_keuangan").document(trxId)
                .update("status", status)
                .await()
            // Refresh List
            fetchTransactions()
        } catch (e: Exception) { }
    }
}

class DetailMahasiswaViewModelFactory(private val uid: String, private val studentUid: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailMahasiswaViewModel(uid, studentUid) as T
    }
}