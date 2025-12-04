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
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DashboardAdminUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val username: String = "Admin",
    val uid: String = "",
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val universities: List<University> = emptyList(),
    val clusters: List<Cluster> = emptyList()
)

class DashboardAdminViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(DashboardAdminUiState(uid = uid))
        private set

    private val db = FirebaseFirestore.getInstance()
    private var univListener: ListenerRegistration? = null
    private var clusterListener: ListenerRegistration? = null

    init {
        if (uid.isNotBlank()) {
            fetchAdminData()
            setupRealtimeListeners()
        }
    }

    private fun fetchAdminData() = viewModelScope.launch {
        try {
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val adminData = UserAdmin(userDoc.data ?: emptyMap())
                uiState = uiState.copy(username = adminData.username.ifBlank { "Admin" })
            }
        } catch (e: Exception) {
            // Ignore error fetching admin name
        }
    }

    private fun setupRealtimeListeners() {
        uiState = uiState.copy(isLoading = true)

        // 1. Listen Universitas
        univListener = db.collection("universitas").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                val uniList = snapshot.documents.map { doc ->
                    // PERBAIKAN UTAMA DI SINI:
                    // Baca field 'wilayah_klaster' dengan aman (String atau Number)
                    val rawCluster = doc.get("wilayah_klaster")
                    val clusterValue = rawCluster?.toString() ?: "0"

                    University(
                        id = doc.id,
                        name = doc.getString("nama_kampus") ?: "",
                        accreditation = doc.getString("akreditasi") ?: "-",
                        cluster = clusterValue
                    )
                }
                uiState = uiState.copy(universities = uniList, isLoading = false)
            }
        }

        // 2. Listen Clusters
        clusterListener = db.collection("konfigurasi").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                val clusterList = mutableListOf<Cluster>()

                // Helper function untuk ambil angka dengan aman
                fun getSafeLong(doc: com.google.firebase.firestore.DocumentSnapshot, field: String): Long {
                    return try {
                        doc.getLong(field) ?: doc.getString(field)?.toLongOrNull() ?: 0L
                    } catch (e: Exception) { 0L }
                }

                // Ambil Non-Kedokteran
                val docNonKed = snapshot.documents.find { it.id == "aturan_biaya_hidup_non-kedokteran" }
                docNonKed?.let { doc ->
                    for (i in 1..5) {
                        val nominal = getSafeLong(doc, "klaster_$i")
                        clusterList.add(Cluster("non_ked_$i", "Cluster $i Non-Kedokteran", nominal))
                    }
                }

                // Ambil Kedokteran
                val docKed = snapshot.documents.find { it.id == "aturan_biaya_hidup_kedokteran" }
                docKed?.let { doc ->
                    for (i in 1..5) {
                        val nominal = getSafeLong(doc, "klaster_$i")
                        clusterList.add(Cluster("ked_$i", "Cluster $i Kedokteran", nominal))
                    }
                }

                uiState = uiState.copy(clusters = clusterList)
            }
        }
    }

    // --- FITUR UPDATE UNIVERSITAS ---
    fun updateUniversity(uniId: String, newAccreditation: String, newCluster: String) = viewModelScope.launch {
        try {
            // Saat update, kita paksa simpan sebagai Number (Long) agar konsisten
            val clusterNumber = newCluster.toLongOrNull() ?: 0L

            db.collection("universitas").document(uniId)
                .update(
                    mapOf(
                        "akreditasi" to newAccreditation,
                        "wilayah_klaster" to clusterNumber // Simpan sebagai angka
                    )
                ).await()
        } catch (e: Exception) {
            println("Update Univ Error: ${e.message}")
        }
    }

    // --- FITUR HAPUS UNIVERSITAS ---
    fun deleteUniversity(uniId: String) = viewModelScope.launch {
        try {
            db.collection("universitas").document(uniId).delete().await()
        } catch (e: Exception) {
            println("Delete Univ Error: ${e.message}")
        }
    }

    // --- FITUR UPDATE CLUSTER (NOMINAL) ---
    fun updateCluster(clusterId: String, newNominal: Long) = viewModelScope.launch {
        val isKedokteran = clusterId.startsWith("ked_")
        val clusterNum = clusterId.last().toString()

        val docId = if (isKedokteran) "aturan_biaya_hidup_kedokteran" else "aturan_biaya_hidup_non-kedokteran"
        val fieldName = "klaster_$clusterNum"

        try {
            db.collection("konfigurasi").document(docId)
                .update(fieldName, newNominal)
                .await()
        } catch (e: Exception) {
            println("Update Cluster Error: ${e.message}")
        }
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onTabSelected(index: Int) {
        uiState = uiState.copy(selectedTab = index)
    }

    override fun onCleared() {
        super.onCleared()
        univListener?.remove()
        clusterListener?.remove()
    }
}