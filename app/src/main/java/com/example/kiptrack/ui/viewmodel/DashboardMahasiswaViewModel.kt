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
    val currentSaldo: Long = 0L,
    val totalViolations: Long = 0L,
    val nextSemesterAllowance: Long = 0L,

    val graphData: List<Long> = List(12) { 0L },
    val transactionHistory: List<Transaction> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

class DashboardMahasiswaViewModel(private val uid: String) : ViewModel() {

    var uiState by mutableStateOf(DashboardMahasiswaUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private var transactionListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    private val BASE_DANA = 8000000L

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

        // 1. LISTEN USER
        userListener = db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()

                val nama = data["nama"] as? String ?: "Mahasiswa"
                val foto = data["foto_profil"] as? String ?: ""
                val saldo = (data["saldo_saat_ini"] as? Number)?.toLong() ?: 0L
                val pelanggaran = (data["total_penyalahgunaan"] as? Number)?.toLong() ?: 0L

                val jenjang = data["jenjang"] as? String ?: "S1"
                val semesterNow = (data["semester_berjalan"] as? Number)?.toInt() ?: 1
                val lastUpdate = data["last_update_period"] as? String ?: ""

                val semesterDepanAllowance = (BASE_DANA - pelanggaran).coerceAtLeast(0L)

                uiState = uiState.copy(
                    userName = nama,
                    photoProfile = foto,
                    currentSaldo = saldo,
                    totalViolations = pelanggaran,
                    nextSemesterAllowance = semesterDepanAllowance,
                    isLoading = false
                )

                checkAndPerformSemesterUpdate(
                    currentSem = semesterNow,
                    jenjang = jenjang,
                    lastUpdatePeriod = lastUpdate,
                    nextAllowance = semesterDepanAllowance,
                    currentSaldo = saldo
                )
            }
        }

        // 2. LISTEN TRANSAKSI (UPDATE: Mapping Field Lengkap)
        transactionListener = db.collection("users").document(uid)
            .collection("laporan_keuangan")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val rawList = snapshot.documents.map { doc ->
                        val d = doc.data ?: emptyMap()
                        Transaction(
                            id = doc.id,
                            date = d["tanggal"] as? String ?: "",
                            amount = (d["nominal"] as? Number)?.toLong() ?: 0L,
                            description = d["deskripsi"] as? String ?: "",
                            status = d["status"] as? String ?: "MENUNGGU",

                            // --- TAMBAHAN PENTING UNTUK DETAIL ---
                            category = d["kategori"] as? String ?: "Lainnya",
                            proofImage = d["bukti_base64"] as? String ?: "",
                            unitPrice = (d["harga_satuan"] as? Number)?.toLong() ?: 0L,
                            quantity = (d["kuantitas"] as? Number)?.toInt() ?: 1
                        )
                    }

                    val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
                    val sortedList = rawList.sortedByDescending {
                        try { dateFormat.parse(it.date)?.time ?: 0L } catch (e: Exception) { 0L }
                    }

                    uiState = uiState.copy(transactionHistory = sortedList)
                    generateGraphData(sortedList, uiState.selectedYear)
                }
            }
    }

    private fun checkAndPerformSemesterUpdate(
        currentSem: Int, jenjang: String, lastUpdatePeriod: String, nextAllowance: Long, currentSaldo: Long
    ) {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val currentPeriodKey = when (month) {
            Calendar.JANUARY -> "$year-JAN"
            Calendar.JULY -> "$year-JUL"
            else -> return
        }
        val maxSemester = if (jenjang.contains("D3", true)) 6 else 8

        if (currentPeriodKey != lastUpdatePeriod && currentSem < maxSemester) {
            val updates = hashMapOf<String, Any>(
                "semester_berjalan" to (currentSem + 1),
                "saldo_saat_ini" to (currentSaldo + nextAllowance),
                "total_penyalahgunaan" to 0,
                "last_update_period" to currentPeriodKey
            )
            db.collection("users").document(uid).update(updates)
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
                    if (calendar.get(Calendar.YEAR) == yearFilter) {
                        monthlyTotals[calendar.get(Calendar.MONTH)] += trx.amount
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