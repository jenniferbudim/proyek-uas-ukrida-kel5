package com.example.kiptrack.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_admin")
data class UserAdmin(
    @PrimaryKey val id: String = "",
    val username: String = "",
    val password: String = ""
) {
    // Helper constructor for Firestore (Same logic as UserMahasiswa)
    constructor(map: Map<String, Any>) : this(
        id = map["uid"] as? String ?: "",
        username = map["username"] as? String ?: "",
        password = map["password"] as? String ?: ""
    )
}