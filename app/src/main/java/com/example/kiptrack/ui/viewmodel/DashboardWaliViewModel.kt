package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.data.CategorySummary
import com.example.kiptrack.ui.data.MonthlyData
import com.example.kiptrack.ui.data.UserWali
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color
import java.util.UUID

data class DashboardWaliUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val username: String = "Wali",
    val uid: String = "",
    val currentBalance: Long = 0L,
    val totalExpenditure: Long = 0L,
    val totalViolations: Int = 0,
    val monthlyExpenditure: List<MonthlyData> = emptyList(),
    val categorySummary: List<CategorySummary> = emptyList()
)

class DashboardWaliViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(DashboardWaliUiState(uid = uid))
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        if (uid.isNotBlank()) {
            fetchWaliData()
            loadMockFinancialData()
        } else {
            uiState = uiState.copy(isLoading = false, errorMessage = "UID Invalid")
        }
    }

    private fun fetchWaliData() = viewModelScope.launch {
        try {
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val waliData = UserWali(userDoc.data ?: emptyMap())
                uiState = uiState.copy(
                    username = waliData.nama.ifBlank { "Wali" },
                    isLoading = false
                )
            } else {
                uiState = uiState.copy(isLoading = false)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Error: ${e.message}")
        }
    }

    private fun loadMockFinancialData() {
        val monthlyData = listOf(
            MonthlyData("JAN", 500000L),
            MonthlyData("FEB", 800000L),
            MonthlyData("MAR", 1200000L),
            MonthlyData("APR", 700000L),
            MonthlyData("MAY", 1500000L),
            MonthlyData("JUN", 400000L),
            MonthlyData("JUL", 1000000L),
            MonthlyData("AUG", 900000L),
            MonthlyData("SEP", 1300000L),
            MonthlyData("OCT", 300000L),
        )

        val totalExpenditure = monthlyData.sumOf { it.amount }

        // Updated colors to match the image reference: Red, Teal/Green, Orange
        val categoryData = listOf(
            CategorySummary("Makanan & Minuman", 60f, Color(0xFFD32F2F)), // Red
            CategorySummary("Transportasi", 25f, Color(0xFF00A389)), // Teal/Green
            CategorySummary("Sandang", 15f, Color(0xFFFF9800)), // Orange
        )

        // Match the UI numbers provided in the image
        uiState = uiState.copy(
            currentBalance = 1_000_000L,
            totalExpenditure = 1_500_000L,
            totalViolations = 100_000,
            monthlyExpenditure = monthlyData,
            categorySummary = categoryData
        )
    }
}