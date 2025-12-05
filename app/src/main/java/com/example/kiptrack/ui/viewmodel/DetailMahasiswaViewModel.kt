package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
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
import java.util.Calendar
import java.util.Locale

data class DetailMahasiswaUiState(
    val isLoading: Boolean = true,
    val name: String = "Loading...",
    val saldo: Long = 0L,
    val photoProfile: String = "",

    val totalPengeluaranAllTime: Long = 0L,
    val totalPelanggaranAllTime: Long = 0L,

    val pelanggaranSemesterIni: Long = 0L,
    val estimasiPendapatanDepan: Long = 0L,

    val transactionList: List<Transaction> = emptyList(),
    val graphData: List<Long> = List(12) { 0L },
    val categoryData: List<CategorySummary> = emptyList(),

    val selectedTransaction: Transaction? = null,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

class DetailMahasiswaViewModel(
    private val adminUid: String,
    private val studentUid: String
) : ViewModel() {

    var uiState by mutableStateOf(DetailMahasiswaUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private var allTransactionsCache: List<Transaction> = emptyList()
    private var currentClusterNominal: Long = 0L

    init {
        fetchFullStudentData()
        fetchTransactions()
    }

    fun nextYear() {
        uiState = uiState.copy(selectedYear = uiState.selectedYear + 1)
        processGraphAndListByYear()
    }

    fun previousYear() {
        uiState = uiState.copy(selectedYear = uiState.selectedYear - 1)
        processGraphAndListByYear()
    }

    fun selectTransaction(trx: Transaction) {
        uiState = uiState.copy(selectedTransaction = trx)
    }

    // --- 1. FETCH DATA MAHASISWA ---
    private fun fetchFullStudentData() = viewModelScope.launch {
        try {
            val docUser = db.collection("users").document(studentUid).get().await()
            if (!docUser.exists()) return@launch

            val dataUser = docUser.data ?: emptyMap()
            val idUniv = dataUser["id_universitas"] as? String ?: ""
            val idProdi = dataUser["id_prodi"] as? String ?: ""

            uiState = uiState.copy(
                name = dataUser["nama"] as? String ?: "Mahasiswa",
                saldo = (dataUser["saldo_saat_ini"] as? Number)?.toLong() ?: 0L,
                photoProfile = dataUser["foto_profil"] as? String ?: "",
                totalPelanggaranAllTime = (dataUser["total_penyalahgunaan"] as? Number)?.toLong() ?: 0L
            )

            if (idUniv.isNotBlank()) {
                val docUniv = db.collection("universitas").document(idUniv).get().await()
                val clusterVal = docUniv.get("wilayah_klaster")?.toString() ?: "1"
                val isKedokteran = idProdi.contains("kedokteran", ignoreCase = true) && !idProdi.contains("non", ignoreCase = true)
                val configDocId = if (isKedokteran) "aturan_biaya_hidup_kedokteran" else "aturan_biaya_hidup_non-kedokteran"
                val docConfig = db.collection("konfigurasi").document(configDocId).get().await()
                val nominalBantuan = docConfig.getLong("klaster_$clusterVal") ?: 0L

                currentClusterNominal = nominalBantuan
                recalculateEstimation()
            }
        } catch (e: Exception) {
            println("Error fetch student: ${e.message}")
        }
    }

    // --- 2. FETCH TRANSAKSI ---
    private fun fetchTransactions() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            val snapshot = db.collection("users").document(studentUid)
                .collection("laporan_keuangan")
                .get() // Get all data first
                .await()

            val categoryMap = mutableMapOf<String, Long>()

            val rawList = snapshot.documents.map { doc ->
                val data = doc.data ?: emptyMap()
                val amount = (data["nominal"] as? Number)?.toLong() ?: 0L
                val status = data["status"] as? String ?: "MENUNGGU"
                val categoryName = data["kategori"] as? String ?: "Lainnya"

                // --- PIE CHART LOGIC: Sum ALL transactions by category ---
                val currentTotal = categoryMap[categoryName] ?: 0L
                categoryMap[categoryName] = currentTotal + amount

                Transaction(
                    id = doc.id,
                    date = data["tanggal"] as? String ?: "",
                    description = data["deskripsi"] as? String ?: "",
                    amount = amount,
                    status = status,
                    quantity = (data["kuantitas"] as? Number)?.toInt() ?: 1,
                    unitPrice = (data["harga_satuan"] as? Number)?.toLong() ?: 0L,
                    proofImage = data["bukti_base64"] as? String ?: ""
                )
            }

            // --- SORTING LOGIC: Newest First ---
            val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
            val sortedList = rawList.sortedByDescending { trx ->
                try {
                    dateFormat.parse(trx.date)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }

            allTransactionsCache = sortedList

            // Total Pengeluaran (Semua Status)
            val totalSpent = sortedList.sumOf { it.amount }
            // Total Pelanggaran (Hanya DITOLAK)
            val totalViolations = sortedList.filter { it.status == "DITOLAK" }.sumOf { it.amount }

            calculateCurrentSemesterStats(sortedList)

            // Generate Pie Data
            val pieData = generatePieDataBasedOnPrice(categoryMap)

            uiState = uiState.copy(
                isLoading = false,
                totalPengeluaranAllTime = totalSpent,
                totalPelanggaranAllTime = totalViolations,
                selectedTransaction = sortedList.firstOrNull(),
                categoryData = pieData,
                transactionList = sortedList // Ensure UI gets the sorted list initially
            )

            processGraphAndListByYear()

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false)
        }
    }

    // --- HELPER: GENERATE PIE DATA ---
    private fun generatePieDataBasedOnPrice(categoryMap: Map<String, Long>): List<CategorySummary> {
        val totalAmount = categoryMap.values.sum().toFloat()

        fun getColorForCategory(cat: String): Color {
            return when(cat) {
                "Makanan & Minuman" -> PieRed
                "Transportasi" -> PieGreen
                "Sandang" -> PieOrange
                "Hunian" -> Color(0xFF5C6BC0)
                "Pendidikan" -> Color(0xFFAB47BC)
                "Kesehatan" -> Color(0xFFEF5350)
                else -> Color.Gray
            }
        }

        return categoryMap.map { (categoryName, amount) ->
            val percentage = if (totalAmount > 0) {
                (amount / totalAmount) * 100f
            } else 0f

            CategorySummary(
                name = categoryName,
                percentage = percentage,
                color = getColorForCategory(categoryName)
            )
        }.sortedByDescending { it.percentage }
    }

    private fun calculateCurrentSemesterStats(allTrx: List<Transaction>) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val isSemesterGenap = currentMonth <= 5
        val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
        var violationSemesterIni = 0L

        allTrx.filter { it.status == "DITOLAK" }.forEach { trx ->
            try {
                val date = dateFormat.parse(trx.date)
                if (date != null) {
                    calendar.time = date
                    val trxYear = calendar.get(Calendar.YEAR)
                    val trxMonth = calendar.get(Calendar.MONTH)

                    if (trxYear == currentYear) {
                        if (isSemesterGenap && trxMonth <= 5) {
                            violationSemesterIni += trx.amount
                        } else if (!isSemesterGenap && trxMonth > 5) {
                            violationSemesterIni += trx.amount
                        }
                    }
                }
            } catch (e: Exception) {}
        }

        uiState = uiState.copy(pelanggaranSemesterIni = violationSemesterIni)
        recalculateEstimation()
    }

    private fun recalculateEstimation() {
        if (currentClusterNominal > 0) {
            val estimasi = (currentClusterNominal - uiState.pelanggaranSemesterIni).coerceAtLeast(0L)
            uiState = uiState.copy(estimasiPendapatanDepan = estimasi)
        }
    }

    private fun processGraphAndListByYear() {
        val yearString = "/${uiState.selectedYear}"
        val monthlyTotals = MutableList(12) { 0L }
        val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)

        // Filter transactions for the selected year
        val yearTransactions = allTransactionsCache.filter { it.date.endsWith(yearString) }

        yearTransactions.forEach { trx ->
            try {
                val date = dateFormat.parse(trx.date)
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    monthlyTotals[calendar.get(Calendar.MONTH)] += trx.amount
                }
            } catch (e: Exception) {}
        }

        uiState = uiState.copy(
            graphData = monthlyTotals,
            transactionList = yearTransactions
        )
    }

    fun approveTransaction(trxId: String) = viewModelScope.launch {
        updateStatus(trxId, "DISETUJUI")
    }

    fun denyTransaction(trxId: String, amount: Long) = viewModelScope.launch {
        updateStatus(trxId, "DITOLAK", false)
        try {
            val userRef = db.collection("users").document(studentUid)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentViolations = snapshot.getLong("total_penyalahgunaan") ?: 0L
                transaction.update(userRef, "total_penyalahgunaan", currentViolations + amount)
            }.await()
            fetchFullStudentData()
            fetchTransactions()
        } catch (e: Exception) {
            fetchTransactions()
        }
    }

    private suspend fun updateStatus(trxId: String, status: String, refresh: Boolean = true) {
        try {
            db.collection("users").document(studentUid)
                .collection("laporan_keuangan").document(trxId)
                .update("status", status)
                .await()
            if (refresh) fetchTransactions()
        } catch (e: Exception) { }
    }
}

class DetailMahasiswaViewModelFactory(private val uid: String, private val studentUid: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailMahasiswaViewModel(uid, studentUid) as T
    }
}