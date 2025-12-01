package com.example.kiptrack.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_wali")
data class UserWali(
    @PrimaryKey val id: String,
    val nama: String,
    val password: String
)
