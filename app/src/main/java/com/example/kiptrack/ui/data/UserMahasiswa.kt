package com.example.kiptrack.ui.data

data class UserMahasiswa(
    val uid: String = "",
    val nim: String = "",
    val nama: String = "",

    // Field Data Dinamis (Nanti diisi oleh ViewModel setelah fetch DB)
    val universityName: String = "Memuat...",
    val programStudiName: String = "Memuat...",

    val jenjang: String = "S1", // Default, bisa diambil dari Prodi nanti
    val semesterBerjalan: String = "1",
    val namaWali: String = "",

    // Foto Profil (Base64 String)
    val fotoProfil: String = ""
)