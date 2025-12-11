package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.data.CategorySummary
import com.example.kiptrack.ui.data.MonthlyData
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

data class DashboardWaliUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val username: String = "Wali",
    val studentName: String = "Mahasiswa",

    val currentBalance: Long = 0L,
    val totalExpenditure: Long = 0L,
    val totalViolations: Long = 0L,

    val monthlyExpenditure: List<MonthlyData> = emptyList(),
    val categorySummary: List<CategorySummary> = emptyList(),

    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),

    val allTransactions: List<Transaction> = emptyList()
)

class DashboardWaliViewModel(private val waliUid: String) : ViewModel() {
    var uiState by mutableStateOf(DashboardWaliUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        if (waliUid.isNotBlank()) {
            fetchData()
        }
    }

    fun nextYear() {
        val newYear = uiState.selectedYear + 1
        uiState = uiState.copy(selectedYear = newYear)
        processGraphData(uiState.allTransactions, newYear)
    }

    fun previousYear() {
        val newYear = uiState.selectedYear - 1
        uiState = uiState.copy(selectedYear = newYear)
        processGraphData(uiState.allTransactions, newYear)
    }

    private fun fetchData() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            val waliDoc = db.collection("users").document(waliUid).get().await()
            if (!waliDoc.exists()) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Data Wali tidak ditemukan")
                return@launch
            }

            val namaWali = waliDoc.getString("nama") ?: "Wali"
            val studentUid = waliDoc.getString("id_mahasiswa") ?: ""

            if (studentUid.isBlank()) {
                uiState = uiState.copy(isLoading = false, username = namaWali, errorMessage = "Belum terhubung dengan mahasiswa")
                return@launch
            }

            val studentDoc = db.collection("users").document(studentUid).get().await()
            val studentName = studentDoc.getString("nama") ?: "Mahasiswa"
            val currentBalance = studentDoc.getLong("saldo_saat_ini") ?: 0L

            val trxSnapshot = db.collection("users").document(studentUid)
                .collection("laporan_keuangan")
                .get()
                .await()

            val categoryMap = mutableMapOf<String, Long>()
            val transactionList = trxSnapshot.documents.map { doc ->
                val data = doc.data ?: emptyMap()
                val amount = (data["nominal"] as? Number)?.toLong() ?: 0L
                val category = data["kategori"] as? String ?: "Lainnya"

                val currentCatTotal = categoryMap[category] ?: 0L
                categoryMap[category] = currentCatTotal + amount

                Transaction(
                    id = doc.id,
                    date = data["tanggal"] as? String ?: "",
                    amount = amount,
                    category = category,
                    status = data["status"] as? String ?: "MENUNGGU",
                    description = data["deskripsi"] as? String ?: ""
                )
            }

            val totalSpent = transactionList.sumOf { it.amount } // Semua status
            val totalViolations = transactionList.filter { it.status == "DITOLAK" }.sumOf { it.amount } // Hanya yang ditolak

            val pieData = generatePieData(categoryMap)

            uiState = uiState.copy(
                isLoading = false,
                username = namaWali,
                studentName = studentName,
                currentBalance = currentBalance,
                totalExpenditure = totalSpent,
                totalViolations = totalViolations,
                categorySummary = pieData,
                allTransactions = transactionList
            )

            processGraphData(transactionList, uiState.selectedYear)

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Error: ${e.message}")
        }
    }

    private fun processGraphData(transactions: List<Transaction>, year: Int) {
        val dateFormat = SimpleDateFormat("MMM dd/yyyy", Locale.US)
        val monthlyTotals = MutableList(12) { 0L }
        val monthNames = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

        transactions.forEach { trx ->
            try {
                val date = dateFormat.parse(trx.date)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    if (cal.get(Calendar.YEAR) == year) {
                        val monthIndex = cal.get(Calendar.MONTH)
                        monthlyTotals[monthIndex] += trx.amount
                    }
                }
            } catch (e: Exception) { }
        }

        val monthlyDataList = monthlyTotals.mapIndexed { index, total ->
            MonthlyData(monthNames[index], total)
        }

        uiState = uiState.copy(monthlyExpenditure = monthlyDataList)
    }

    private fun generatePieData(categoryMap: Map<String, Long>): List<CategorySummary> {
        val total = categoryMap.values.sum().toFloat()
        fun getColor(cat: String): Color {
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

        return categoryMap.map { (cat, amount) ->
            CategorySummary(
                name = cat,
                percentage = if (total > 0) (amount / total) * 100f else 0f,
                color = getColor(cat)
            )
        }.sortedByDescending { it.percentage }
    }
}