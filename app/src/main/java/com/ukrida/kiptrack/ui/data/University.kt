package com.ukrida.kiptrack.ui.data

data class University(
    val id: String,
    val name: String,
    val accreditation: String,
    val cluster: String,
    val logoUrl: String = ""
)