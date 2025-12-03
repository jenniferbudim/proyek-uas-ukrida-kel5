package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.data.Cluster
import com.example.kiptrack.ui.data.University
import com.example.kiptrack.ui.data.UserAdmin
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// State for the Admin Dashboard UI
data class DashboardAdminUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val username: String = "Admin",
    val uid: String = "",
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0 = Universitas, 1 = Cluster
    val universities: List<University> = emptyList(),
    val clusters: List<Cluster> = emptyList()
)

class DashboardAdminViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(DashboardAdminUiState(uid = uid))
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        if (uid.isNotBlank()) {
            fetchAdminData()
            loadMockData()
        } else {
            uiState = uiState.copy(isLoading = false, errorMessage = "UID Invalid")
        }
    }

    private fun fetchAdminData() = viewModelScope.launch {
        try {
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val adminData = UserAdmin(userDoc.data ?: emptyMap())
                uiState = uiState.copy(
                    username = adminData.username.ifBlank { "Admin" },
                    isLoading = false
                )
            } else {
                uiState = uiState.copy(isLoading = false)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = "Error: ${e.message}")
        }
    }

    private fun loadMockData() {
        val mockUniversities = listOf(
            University(UUID.randomUUID().toString(), "Universitas Kristen Krida Wacana", "A", "1"),
            University(UUID.randomUUID().toString(), "Universitas Tarumanagara", "A", "1"),
            University(UUID.randomUUID().toString(), "Universitas Trisakti", "B", "2"),
            University(UUID.randomUUID().toString(), "Universitas Bina Nusantara", "Unggul", "1"),
        )

        val mockClusters = listOf(
            Cluster(UUID.randomUUID().toString(), "Cluster 1 Non-Kedokteran", 8000000L),
            Cluster(UUID.randomUUID().toString(), "Cluster 1 Kedokteran", 12000000L),
            Cluster(UUID.randomUUID().toString(), "Cluster 2 Non-Kedokteran", 6000000L),
            Cluster(UUID.randomUUID().toString(), "Cluster 2 Kedokteran", 8000000L),
        )

        uiState = uiState.copy(
            universities = mockUniversities,
            clusters = mockClusters
        )
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onTabSelected(index: Int) {
        uiState = uiState.copy(selectedTab = index)
    }
}