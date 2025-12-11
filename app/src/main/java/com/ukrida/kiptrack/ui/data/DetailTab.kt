package com.ukrida.kiptrack.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

enum class DetailTab(val icon: ImageVector, val label: String) {
    Home(Icons.Filled.Home, "Home"),
    Konfirmasi(Icons.Outlined.Email, "Konfirmasi"),
    Perincian(Icons.Outlined.Visibility, "Perincian")
}