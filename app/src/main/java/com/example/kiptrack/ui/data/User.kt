package com.example.kiptrack.ui.data



@Entity(tableName = "tbl_users")
data class User(
    @PrimaryKey val nim: String,
    val nama: String,
    val password: String
)
