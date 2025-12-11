package com.ukrida.kiptrack.ui.utils

import java.text.NumberFormat
import java.util.Locale

object NumberUtils {

    // Format Rupiah (Rp 40.000)
    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    // Fungsi Utama Terbilang
    fun terbilang(angka: Long): String {
        if (angka == 0L) return "Nol Rupiah"
        return (convert(angka) + " Rupiah").trim()
    }

    private fun convert(angka: Long): String {
        val huruf = arrayOf("", "Satu", "Dua", "Tiga", "Empat", "Lima", "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas")
        var temp = ""

        when {
            angka < 12 -> temp = " " + huruf[angka.toInt()]
            angka < 20 -> temp = convert(angka - 10) + " Belas"
            angka < 100 -> temp = convert(angka / 10) + " Puluh" + convert(angka % 10)
            angka < 200 -> temp = " Seratus" + convert(angka - 100)
            angka < 1000 -> temp = convert(angka / 100) + " Ratus" + convert(angka % 100)
            angka < 2000 -> temp = " Seribu" + convert(angka - 1000)
            angka < 1000000 -> temp = convert(angka / 1000) + " Ribu" + convert(angka % 1000)
            angka < 1000000000 -> temp = convert(angka / 1000000) + " Juta" + convert(angka % 1000000)
            else -> temp = convert(angka / 1000000000) + " Miliar" + convert(angka % 1000000000)
        }
        return temp
    }
}