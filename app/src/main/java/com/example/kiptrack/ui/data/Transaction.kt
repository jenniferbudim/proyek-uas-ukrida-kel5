package com.example.kiptrack.ui.data

import java.text.NumberFormat
import java.util.Locale

data class Transaction(
    val id: String = "",
    val date: String = "",
    val amount: Long = 0L,          // Ini adalah 'nominal' (Total)
    val description: String = "",
    val status: String = "",
    // --- TAMBAHAN FIELD BARU ---
    val quantity: Int = 1,          // Default 1
    val unitPrice: Long = 0L,       // Harga Satuan
    val proofImage: String = ""     // String Base64 bukti
) {
    val isApproved: Boolean
        get() = status == "DISETUJUI"

    fun formatAmount(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }
}