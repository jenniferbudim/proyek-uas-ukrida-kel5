package com.example.kiptrack.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_wali")
data class UserWali(
    @PrimaryKey val id: String = "",
    val nama: String = "",
    val password: String = ""
) {
    // Helper constructor for Firestore
    constructor(map: Map<String, Any>) : this(
        id = map["uid"] as? String ?: "",
        nama = map["nama"] as? String ?: "",
        password = map["password"] as? String ?: ""
    )
}