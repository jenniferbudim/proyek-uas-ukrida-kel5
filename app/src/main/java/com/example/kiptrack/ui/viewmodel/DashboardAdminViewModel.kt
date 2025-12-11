package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kiptrack.ui.data.Cluster
import com.example.kiptrack.ui.data.University
import com.example.kiptrack.ui.data.UserAdmin
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
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
    val clusters: List<Cluster> = emptyList(),

    val actionSuccess: String? = null,
    val actionError: String? = null
)

class DashboardAdminViewModel(private val uid: String) : ViewModel() {
    var uiState by mutableStateOf(DashboardAdminUiState(uid = uid))
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
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

        }
    }

    private fun setupRealtimeListeners() {
        uiState = uiState.copy(isLoading = true)

        univListener = db.collection("universitas").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                val uniList = snapshot.documents.map { doc ->
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

        clusterListener = db.collection("konfigurasi").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                val clusterList = mutableListOf<Cluster>()

                fun getSafeLong(doc: com.google.firebase.firestore.DocumentSnapshot, field: String): Long {
                    return try {
                        doc.getLong(field) ?: doc.getString(field)?.toLongOrNull() ?: 0L
                    } catch (e: Exception) { 0L }
                }

                val docNonKed = snapshot.documents.find { it.id == "aturan_biaya_hidup_non-kedokteran" }
                docNonKed?.let { doc ->
                    for (i in 1..5) {
                        val nominal = getSafeLong(doc, "klaster_$i")
                        clusterList.add(Cluster("non_ked_$i", "Cluster $i Non-Kedokteran", nominal))
                    }
                }

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

    fun addUniversity(idUniv: String, namaKampus: String, akreditasi: String, klaster: String) = viewModelScope.launch {
        try {
            val data = hashMapOf(
                "nama_kampus" to namaKampus,
                "akreditasi" to akreditasi,
                "wilayah_klaster" to (klaster.toLongOrNull() ?: 1)
            )

            db.collection("universitas").document(idUniv).set(data).await()
            uiState = uiState.copy(actionSuccess = "Universitas berhasil ditambahkan")
        } catch (e: Exception) {
            uiState = uiState.copy(actionError = "Gagal menambah: ${e.message}")
        }
    }

    fun updateUniversity(uniId: String, newAccreditation: String, newCluster: String) = viewModelScope.launch {
        try {
            val clusterNumber = newCluster.toLongOrNull() ?: 0L
            db.collection("universitas").document(uniId)
                .update(
                    mapOf(
                        "akreditasi" to newAccreditation,
                        "wilayah_klaster" to clusterNumber
                    )
                ).await()
            uiState = uiState.copy(actionSuccess = "Universitas berhasil diupdate")
        } catch (e: Exception) {
            uiState = uiState.copy(actionError = "Gagal update: ${e.message}")
        }
    }

    fun deleteUniversityWithAuth(uniId: String, adminPassword: String) = viewModelScope.launch {
        val user = auth.currentUser
        if (user == null || user.email == null) {
            uiState = uiState.copy(actionError = "Admin tidak terautentikasi")
            return@launch
        }

        try {
            val credential = EmailAuthProvider.getCredential(user.email!!, adminPassword)
            user.reauthenticate(credential).await()

            db.collection("universitas").document(uniId).delete().await()

            uiState = uiState.copy(actionSuccess = "Universitas berhasil dihapus")
        } catch (e: Exception) {
            uiState = uiState.copy(actionError = "Password salah atau gagal menghapus")
        }
    }

    fun updateCluster(clusterId: String, newNominal: Long) = viewModelScope.launch {
        val isKedokteran = clusterId.startsWith("ked_")
        val clusterNum = clusterId.last().toString()
        val docId = if (isKedokteran) "aturan_biaya_hidup_kedokteran" else "aturan_biaya_hidup_non-kedokteran"
        val fieldName = "klaster_$clusterNum"

        try {
            db.collection("konfigurasi").document(docId)
                .update(fieldName, newNominal)
                .await()
            uiState = uiState.copy(actionSuccess = "Cluster berhasil diupdate")
        } catch (e: Exception) {
            uiState = uiState.copy(actionError = "Gagal update: ${e.message}")
        }
    }

    fun onSearchQueryChange(query: String) { uiState = uiState.copy(searchQuery = query) }
    fun onTabSelected(index: Int) { uiState = uiState.copy(selectedTab = index) }
    fun resetActionState() { uiState = uiState.copy(actionSuccess = null, actionError = null) }

    override fun onCleared() {
        super.onCleared()
        univListener?.remove()
        clusterListener?.remove()
    }
}