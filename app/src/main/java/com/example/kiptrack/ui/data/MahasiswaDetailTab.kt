package com.example.kiptrack.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MahasiswaDetailTab(val route: String, val icon: ImageVector, val label: String) {
    object Home : MahasiswaDetailTab("home", Icons.Default.Home, "HOME")
    object Konfirmasi : MahasiswaDetailTab("konfirmasi", Icons.Outlined.MailOutline, "KONFIRMASI")
    object Perincian : MahasiswaDetailTab("perincian", Icons.Outlined.Visibility, "PERINCIAN")
}