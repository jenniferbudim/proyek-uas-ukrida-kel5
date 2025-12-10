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
import com.example.kiptrack.ui.theme.color1
import com.example.kiptrack.ui.theme.color2
import com.example.kiptrack.ui.theme.color3
import com.example.kiptrack.ui.theme.color4
import com.example.kiptrack.ui.theme.color5
import com.example.kiptrack.ui.theme.color6
import com.example.kiptrack.ui.theme.color7
import com.example.kiptrack.ui.theme.color8
import com.example.kiptrack.ui.theme.color9
import com.google.firebase.firestore.FirebaseFirestore
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

    // Statistik ALL TIME (Sejarah)
    val totalPengeluaranAllTime: Long = 0L,
    val totalPelanggaranAllTime: Long = 0L,

    // Statistik SEMESTER INI (Aktif)
    val pelanggaranSemesterIni: Long = 0L, // Diambil dari DB (total_penyalahgunaan)
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
    private var currentClusterNominal: Long = 8000000L // Default sebelum load config

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

    // --- 1. FETCH DATA MAHASISWA & AUTO UPDATE ---
    private fun fetchFullStudentData() = viewModelScope.launch {
        try {
            val docUser = db.collection("users").document(studentUid).get().await()
            if (!docUser.exists()) return@launch

            val dataUser = docUser.data ?: emptyMap()

            // Ambil Data Semester untuk Cek Auto-Update
            val semesterNow = (dataUser["semester_berjalan"] as? Number)?.toInt() ?: 1
            val jenjang = dataUser["jenjang"] as? String ?: "S1"
            val lastUpdate = dataUser["last_update_period"] as? String ?: ""
            val currentSaldo = (dataUser["saldo_saat_ini"] as? Number)?.toLong() ?: 0L
            val currentViolationsDB = (dataUser["total_penyalahgunaan"] as? Number)?.toLong() ?: 0L

            // Ambil Nominal Cluster (PENTING untuk Top-up Saldo)
            val idUniv = dataUser["id_universitas"] as? String ?: ""
            val idProdi = dataUser["id_prodi"] as? String ?: ""

            if (idUniv.isNotBlank()) {
                val docUniv = db.collection("universitas").document(idUniv).get().await()
                val clusterVal = docUniv.get("wilayah_klaster")?.toString() ?: "1"
                val isKedokteran = idProdi.contains("kedokteran", ignoreCase = true) && !idProdi.contains("non", ignoreCase = true)
                val configDocId = if (isKedokteran) "aturan_biaya_hidup_kedokteran" else "aturan_biaya_hidup_non-kedokteran"

                val docConfig = db.collection("konfigurasi").document(configDocId).get().await()
                currentClusterNominal = docConfig.getLong("klaster_$clusterVal") ?: 8000000L
            }

            // Hitung Estimasi Depan
            val estimasiDepan = (currentClusterNominal - currentViolationsDB).coerceAtLeast(0L)

            // Update State UI
            uiState = uiState.copy(
                name = dataUser["nama"] as? String ?: "Mahasiswa",
                saldo = currentSaldo,
                photoProfile = dataUser["foto_profil"] as? String ?: "",
                pelanggaranSemesterIni = currentViolationsDB, // Dari DB (Active Debt)
                estimasiPendapatanDepan = estimasiDepan
            )

            // --- JALANKAN LOGIKA AUTO-UPDATE SEMESTER (SAMA SEPERTI DASHBOARD MAHASISWA) ---
            checkAndPerformSemesterUpdate(
                currentSem = semesterNow,
                jenjang = jenjang,
                lastUpdatePeriod = lastUpdate,
                nextAllowance = estimasiDepan, // Gunakan estimasi yang baru dihitung
                currentSaldo = currentSaldo
            )

        } catch (e: Exception) {
            println("Error fetch student: ${e.message}")
        }
    }

    // --- LOGIKA AUTO UPDATE (DIPANGGIL OTOMATIS) ---
    private fun checkAndPerformSemesterUpdate(
        currentSem: Int,
        jenjang: String,
        lastUpdatePeriod: String,
        nextAllowance: Long,
        currentSaldo: Long
    ) {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) // 0=Jan, 6=Jul
        val year = calendar.get(Calendar.YEAR)

        // Tentukan Periode Saat Ini
        val currentPeriodKey = when (month) {
            Calendar.JANUARY -> "$year-JAN" // Periode Genap
            Calendar.JULY -> "$year-JUL"    // Periode Ganjil
            else -> return // Bukan bulan kenaikan semester
        }

        // Cek Batas Semester
        val maxSemester = if (jenjang.contains("D3", true)) 6 else 8

        // SYARAT: Belum update di periode ini & Belum lulus
        if (currentPeriodKey != lastUpdatePeriod && currentSem < maxSemester) {
            val updates = hashMapOf<String, Any>(
                "semester_berjalan" to (currentSem + 1),
                "saldo_saat_ini" to (currentSaldo + nextAllowance),
                "total_penyalahgunaan" to 0, // Reset Pelanggaran
                "last_update_period" to currentPeriodKey
            )

            db.collection("users").document(studentUid).update(updates)
                .addOnSuccessListener {
                    // Refresh data agar UI Admin langsung berubah
                    fetchFullStudentData()
                }
        }
    }

    // --- 2. FETCH TRANSAKSI ---
    private fun fetchTransactions() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            val snapshot = db.collection("users").document(studentUid)
                .collection("laporan_keuangan")
                .get()
                .await()

            val categoryMap = mutableMapOf<String, Long>()

            val list = snapshot.documents.map { doc ->
                val data = doc.data ?: emptyMap()
                val amount = (data["nominal"] as? Number)?.toLong() ?: 0L
                val status = data["status"] as? String ?: "MENUNGGU"

                // 1. Kamu sudah mengambil datanya di sini (BENAR)
                val categoryName = data["kategori"] as? String ?: "Lainnya"

                if (status == "DISETUJUI") {
                    val currentTotal = categoryMap[categoryName] ?: 0L
                    categoryMap[categoryName] = currentTotal + amount
                }

                // 2. MASALAHNYA DISINI: Variabel categoryName tidak dimasukkan ke constructor Transaction
                Transaction(
                    id = doc.id,
                    date = data["tanggal"] as? String ?: "",
                    description = data["deskripsi"] as? String ?: "",
                    amount = amount,
                    status = status,

                    // --- PERBAIKAN: Tambahkan baris ini ---
                    category = categoryName,
                    // --------------------------------------

                    quantity = (data["kuantitas"] as? Number)?.toInt() ?: 1,
                    unitPrice = (data["harga_satuan"] as? Number)?.toLong() ?: 0L,
                    proofImage = data["bukti_base64"] as? String ?: ""
                )
            }
            // Sorting Terbaru
            val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
            val sortedList = list.sortedByDescending { trx ->
                try { dateFormat.parse(trx.date)?.time ?: 0L } catch (e: Exception) { 0L }
            }
            allTransactionsCache = sortedList

            // STATISTIK ALL TIME (Dihitung dari riwayat)
            val totalSpent = sortedList.sumOf { it.amount } // Total keluar (semua status)
            val totalViolationsHistory = sortedList.filter { it.status == "DITOLAK" }.sumOf { it.amount } // Total Ditolak Seumur Hidup

            val pieData = generatePieDataBasedOnPrice(categoryMap)

            uiState = uiState.copy(
                isLoading = false,
                totalPengeluaranAllTime = totalSpent,
                totalPelanggaranAllTime = totalViolationsHistory, // Tampilkan sejarah total di kartu atas
                selectedTransaction = sortedList.firstOrNull(),
                categoryData = pieData,
                transactionList = sortedList
            )
            processGraphAndListByYear()

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false)
        }
    }

    private fun generatePieDataBasedOnPrice(categoryMap: Map<String, Long>): List<CategorySummary> {
        val totalAmount = categoryMap.values.sum().toFloat()
        fun getColorForCategory(cat: String): Color {
            return when(cat) {
                "Makanan & Minuman" -> color1
                "Transportasi" -> color2
                "Sandang" -> color3
                "Hunian" -> color4
                "Pendidikan" -> color5
                "Kesehatan" -> color6
                "Belanja Rumah Tangga" -> color7
                "Paket data/Pulsa" -> color8
                "Keuangan" -> color9
                else -> Color.Gray
            }
        }
        return categoryMap.map { (categoryName, amount) ->
            CategorySummary(
                name = categoryName,
                percentage = if (totalAmount > 0) (amount / totalAmount) * 100f else 0f,
                color = getColorForCategory(categoryName)
            )
        }.sortedByDescending { it.percentage }
    }

    private fun processGraphAndListByYear() {
        val yearString = "/${uiState.selectedYear}"
        val monthlyTotals = MutableList(12) { 0L }
        val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
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
        uiState = uiState.copy(graphData = monthlyTotals, transactionList = yearTransactions)
    }

    fun approveTransaction(trxId: String) = viewModelScope.launch {
        updateStatus(trxId, "DISETUJUI")
    }

    fun denyTransaction(trxId: String, amount: Long) = viewModelScope.launch {
        updateStatus(trxId, "DITOLAK", false)
        try {
            // Tambah Pelanggaran & Kurangi Saldo (Opsional, tergantung kebijakan)
            // Sesuai request: Ditolak -> Masuk Pelanggaran
            val userRef = db.collection("users").document(studentUid)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentViolations = snapshot.getLong("total_penyalahgunaan") ?: 0L

                // Update Pelanggaran di DB
                transaction.update(userRef, "total_penyalahgunaan", currentViolations + amount)
            }.await()

            // Refresh Data User (Pelanggaran Semester Ini)
            fetchFullStudentData()
            // Refresh List
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