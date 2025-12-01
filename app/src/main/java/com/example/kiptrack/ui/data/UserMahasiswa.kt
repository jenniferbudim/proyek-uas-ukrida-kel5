package com.example.kiptrack.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_mahasiswa")
data class UserMahasiswa(
    @PrimaryKey val nim: String,
    val nama: String,
    val password: String
)
