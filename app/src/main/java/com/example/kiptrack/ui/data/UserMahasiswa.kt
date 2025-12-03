package com.example.kiptrack.ui.data

data class UserMahasiswa(
    val uid: String = "", // Firestore Document ID (same as Auth UID)
    val nim: String = "", // Student ID
    val nama: String = "", // Student Name (e.g., "Blessy")
    val password: String = "", // Stored password (if needed, but usually avoided in real apps)
    val university: String = "Universitas Kristen Krida Wacana",
    val programStudi: String = "Informatika",
    val jenjang: String = "Sarjana (S1)",
    val semesterBerjalan: String = "Semester 6",
    val namaWali: String = "Ayu Perry"
) {
    // Helper constructor for easy use
    constructor(map: Map<String, Any>) : this(
        uid = map["uid"] as? String ?: "",
        nim = map["nim"] as? String ?: "",
        nama = map["nama"] as? String ?: "",
        password = map["password"] as? String ?: ""
    )
}