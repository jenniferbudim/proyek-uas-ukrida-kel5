package com.example.kiptrack.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Mock file to provide the necessary icons for the profile screen,
 * assuming the back arrow in the button is similar to the standard Logout icon.
 */
@Composable
fun LogoutIcon(modifier: Modifier = Modifier, tint: Color) {
    Icon(
        imageVector = Icons.Default.Logout,
        contentDescription = "Keluar",
        tint = tint,
        modifier = modifier
    )
}