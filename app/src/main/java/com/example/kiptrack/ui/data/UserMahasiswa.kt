package com.example.kiptrack.ui.data

data class UserMahasiswa(
    val uid: String = "",
    val nim: String = "",
    val nama: String = "",

    val universityName: String = "Memuat...",
    val programStudiName: String = "Memuat...",

    val jenjang: String = "S1",
    val semesterBerjalan: String = "1",
    val namaWali: String = "",

    val fotoProfil: String = "",
    val lastUpdatePeriod: String = ""
)