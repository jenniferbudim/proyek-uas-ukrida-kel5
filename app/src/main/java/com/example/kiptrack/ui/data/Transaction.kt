package com.example.kiptrack.ui.data

import java.text.NumberFormat
import java.util.Locale

// Data structure for expense history (Riwayat Pengeluaran)
data class Transaction(
    val id: String,
    val date: String,          // e.g., "JAN 16/2025"
    val amount: Long,          // Amount in Rupiah (e.g., 50000)
    val description: String,   // e.g., "Buku", "Alat Tulis"
    val isApproved: Boolean    // True for checkmark, false for warning
) {
    fun formatAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }
}