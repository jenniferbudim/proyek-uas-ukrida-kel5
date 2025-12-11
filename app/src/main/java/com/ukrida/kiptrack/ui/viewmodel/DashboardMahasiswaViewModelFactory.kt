package com.ukrida.kiptrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DashboardMahasiswaViewModelFactory(private val uid: String) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardMahasiswaViewModel::class.java)) {
            return DashboardMahasiswaViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class requested: ${modelClass.name}")
    }
}