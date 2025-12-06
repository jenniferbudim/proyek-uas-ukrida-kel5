package com.example.kiptrack.ui.data

data class UserMahasiswa(
    val uid: String = "",
    val nim: String = "",
    val nama: String = "",

    // Field Data Dinamis
    val universityName: String = "Memuat...",
    val programStudiName: String = "Memuat...",

    val jenjang: String = "S1",
    val semesterBerjalan: String = "1",
    val namaWali: String = "",

    // Foto Profil
    val fotoProfil: String = "",

    // --- FIELD BARU UNTUK SISTEM SEMESTER ---
    // Format contoh: "2025-JAN" atau "2025-JUL"
    val lastUpdatePeriod: String = ""
)