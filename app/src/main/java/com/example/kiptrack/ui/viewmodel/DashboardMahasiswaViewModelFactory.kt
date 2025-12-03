package com.example.kiptrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory class required to instantiate DashboardMahasiswaViewModel with a specific
 * constructor parameter (the user's UID) which cannot be passed directly by default
 * to the Jetpack Compose viewModel() function.
 */
class DashboardMahasiswaViewModelFactory(private val uid: String) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Ensure the requested class is the DashboardMahasiswaViewModel
        if (modelClass.isAssignableFrom(DashboardMahasiswaViewModel::class.java)) {
            // Instantiate the ViewModel, injecting the required 'uid'
            return DashboardMahasiswaViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class requested: ${modelClass.name}")
    }
}