package com.example.kiptrack.ui.data

import java.text.NumberFormat
import java.util.Locale

data class Transaction(
    val id: String = "",
    val date: String = "",
    val amount: Long = 0L,
    val description: String = "",
    val status: String = "", // "MENUNGGU", "DISETUJUI", "DITOLAK"
    val quantity: Int = 1,
    val unitPrice: Long = 0L,
    val proofImage: String = ""
) {
    // Helper untuk menentukan status
    val isApproved: Boolean get() = status == "DISETUJUI"
    val isRejected: Boolean get() = status == "DITOLAK"
    val isPending: Boolean get() = status == "MENUNGGU"

    fun formatAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }
}