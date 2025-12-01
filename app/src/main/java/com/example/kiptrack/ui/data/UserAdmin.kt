package com.example.kiptrack.ui.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_admin")
data class UserAdmin(
    @PrimaryKey val id: String,
    val username: String,
    val password: String
)
